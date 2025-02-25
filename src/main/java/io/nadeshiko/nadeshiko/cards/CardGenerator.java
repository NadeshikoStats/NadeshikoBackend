/*
 * This file is a part of the nadeshiko project. nadeshiko is free software, licensed under the MIT license.
 *
 * Usage of these works (including, yet not limited to, reuse, modification, copying, distribution, and selling) is
 * permitted, provided that the relevant copyright notice and permission notice (as specified in LICENSE) shall be
 * included in all copies or substantial portions of this software.
 *
 * These works are provided "AS IS" with absolutely no warranty of any kind, either expressed or implied.
 *
 * You should have received a copy of the MIT License alongside this software; refer to LICENSE for information.
 * If not, refer to https://mit-license.org.
 */

package io.nadeshiko.nadeshiko.cards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.provider.impl.SkyBlockGeneralCardProvider;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.ImageUtil;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.1.0
 * @author chloe
 */
public class CardGenerator {

	public CardGenerator() {
		try {
			registerFont("/cards/fonts/Minecraft.otf");
			registerFont("/cards/fonts/MinecraftBold.otf");
			registerFont("/cards/fonts/Unifont.otf");
			registerFont("/cards/fonts/Inter-Bold.ttf");
			registerFont("/cards/fonts/Inter-Medium.ttf");
		} catch (Exception e) {
			Nadeshiko.logger.error("Exception while registering fonts!", e);
		}
	}

	public byte[] generateCard(CardGame game, JsonObject data) throws Exception {

		String name = data.get("name").getAsString();
		String badge = Nadeshiko.INSTANCE.getStatsCache().get(name, true).get("badge").getAsString();
		boolean hasBadge = !badge.isEmpty() && !badge.equals("NONE");

		BufferedImage card;
		Graphics graphics;
		AffineTransform originalTransform = null;

		// Get size from the data
		String sizeStr = data.has("size") ? data.get("size").getAsString().toUpperCase() : "FULL";
		CardGame.CardSize size;
		try {
			size = CardGame.CardSize.valueOf(sizeStr);
		} catch (IllegalArgumentException e) {
			size = CardGame.CardSize.FULL;
		}

		// Read the template from the resources
		String templatePath = "/cards/templates/" + game.name() + 
			(size == CardGame.CardSize.TINY ? "_TINY" : "") + ".png"; // Tiny cards use the same file name but with _TINY
		
		try (InputStream templateStream = CardGenerator.class.getResourceAsStream(templatePath)) {
			byte[] cardTemplateBytes;

			if (templateStream != null) {
				cardTemplateBytes = templateStream.readAllBytes();
				card = ImageUtil.createImageFromBytes(cardTemplateBytes);
				graphics = card.getGraphics();
				
				if (size == CardGame.CardSize.TINY) {
					Graphics2D g2d = (Graphics2D) graphics;
					// Save the original transform
					originalTransform = g2d.getTransform();
					// scale to two-thirds, fix antialiasing (which we need because weird scaling)
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2d.scale(0.66, 0.66);
					g2d.translate(40, -80);
				}
			} else {
				// if tiny template doesn't exist
				if (size == CardGame.CardSize.TINY) {
					templatePath = "/cards/templates/" + game.name() + ".png";
					try (InputStream fallbackStream = CardGenerator.class.getResourceAsStream(templatePath)) {
						if (fallbackStream != null) {
							cardTemplateBytes = fallbackStream.readAllBytes();
							card = ImageUtil.createImageFromBytes(cardTemplateBytes);
							graphics = card.getGraphics();
						} else {
							Nadeshiko.INSTANCE.alert("Failed reading card template for %s!", game.name());
							return null;
						}
					}
				} else {
					Nadeshiko.INSTANCE.alert("Failed reading card template for %s!", game.name());
					return null;
				}
			}
		}

		// Fetch the player's stats
		JsonObject statsResponse = Nadeshiko.INSTANCE.getStatsCache().get(name, true);
		JsonObject profileObject = statsResponse.getAsJsonObject("profile");

		// Ensure the player is valid and fetching stats succeeded
		if (!statsResponse.has("success") || !statsResponse.get("success").getAsBoolean()) {
			Nadeshiko.INSTANCE.alert("Failed generating %s card for %s!", game.name(), name);
			return statsResponse.toString().getBytes();
		}

		// If this is a SkyBlock card, fetch SkyBlock data early to check ironman status
		JsonObject skyblockProfileData = null;
		if (game == CardGame.SKYBLOCK_GENERAL) {
			try {
				JsonObject skyblockProfiles = JsonParser.parseString(HTTPUtil.get("https://sky.shiiyu.moe/api/v2/profile/" +
					name).response()).getAsJsonObject().getAsJsonObject("profiles");

				// Find active profile
				for (Map.Entry<String, JsonElement> entry : skyblockProfiles.entrySet()) {
					JsonObject entryObject = entry.getValue().getAsJsonObject();
					if (entryObject.has("current") && entryObject.get("current").getAsBoolean()) {
						skyblockProfileData = entryObject.getAsJsonObject("data");
						break;
					}
				}
			} catch (Exception e) {
				Nadeshiko.INSTANCE.alert("Failed fetching SkyBlock data for %s!", name);
			}
		}

		// Add glow, if applicable
		if (hasBadge) {

			// Read the glow overlay from the resources
			try (InputStream glowStream = CardGenerator.class.
				getResourceAsStream("/cards/badge/" + badge.split("-")[0] + "-overlay.png")) {

				if (glowStream != null) {
					byte[] glowBytes = glowStream.readAllBytes();
					BufferedImage glowImage = ImageUtil.createImageFromBytes(glowBytes);

					// Draw the glow
					graphics.drawImage(glowImage, 0, 0, null);
				} else {
					Nadeshiko.INSTANCE.alert("Failed reading badge glow file for {}!", badge);
					return null;
				}
			}
		}

		// Get the player render
		byte[] playerBytes = HTTPUtil.getRaw("https://visage.surgeplay.com/bust/333/" + name + ".png",
			new HashMap<>() {{
				put("User-Agent", "nadeshiko.io (+https://nadeshiko.io; contact@nadeshiko.io)");
			}}).response();
		BufferedImage playerImage = ImageUtil.createImageFromBytes(playerBytes);

		// Draw the player
		graphics.drawImage(playerImage, 138, 165, null);

		// Draw the name tag
		String displayName = profileObject.get("tagged_name").getAsString();
		
		// Get game mode and determine icon
		String gameMode = null;
		int gameModeIconWidth = 0;
		String gameModeIconPath = null;
		if (skyblockProfileData != null && 
			skyblockProfileData.has("profile") && 
			skyblockProfileData.getAsJsonObject("profile").has("game_mode")) {
			
			gameMode = skyblockProfileData.getAsJsonObject("profile").get("game_mode").getAsString();
			switch (gameMode) {
				case "ironman":
					gameModeIconWidth = 40;
					gameModeIconPath = "/cards/skyblock/IRONMAN2.png";
					break;
				case "island":
					gameModeIconWidth = 22;
					gameModeIconPath = "/cards/skyblock/STRANDED2.png";
					break;
				case "bingo":
					gameModeIconWidth = 38;
					gameModeIconPath = "/cards/skyblock/BINGO2.png";
					break;
			}
		}

		int width = MinecraftRenderer.minecraftWidth(graphics, displayName, 40);
		int textX = 300;
		int nameplateYOffset = size == CardGame.CardSize.TINY ? 20 : 0; // nameplate closer to the player in tiny cards

		if (hasBadge) {
			width += 34 + 10;
			textX -= (34 + 10) / 2;
		}
		if (gameModeIconPath != null) {
			width += gameModeIconWidth + 10;
			textX -= (gameModeIconWidth + 10) / 2;
		}

		graphics.setColor(new Color(0, 0, 0, 128));
		graphics.fillRect(300 - (width / 2) - 10, 83 + nameplateYOffset, width + 20, 50);

		int nameWidth = MinecraftRenderer.minecraftWidth(graphics, displayName, 40);
		MinecraftRenderer.drawCenterMinecraftString(graphics,
			displayName, textX, 120 + nameplateYOffset, 40);

		// Add the badge, if applicable
		if (hasBadge) {
			// Read the badge from the resources
			try (InputStream glowStream = CardGenerator.class.
				getResourceAsStream("/cards/badge/" + badge + ".png")) {

				if (glowStream != null) {
					byte[] badgeBytes = glowStream.readAllBytes();
					BufferedImage badgeImage = ImageUtil.createImageFromBytes(badgeBytes);

					// Draw the badge
					graphics.drawImage(badgeImage, textX + (nameWidth / 2) + 10, 91 + nameplateYOffset, null);
				} else {
					Nadeshiko.INSTANCE.alert("Failed reading badge file for %s!", badge);
					return null;
				}
			}
		}

		// Add the game mode icon if applicable
		if (gameModeIconPath != null) {
			try (InputStream iconStream = CardGenerator.class.
				getResourceAsStream(gameModeIconPath)) {

				if (iconStream != null) {
					byte[] iconBytes = iconStream.readAllBytes();
					BufferedImage iconImage = ImageUtil.createImageFromBytes(iconBytes);

					// Draw the game mode icon after the badge if present
					int iconX = textX + (nameWidth / 2) + 10;
					if (hasBadge) {
						iconX += 34 + 10; // Add space after badge
					}
					graphics.drawImage(iconImage, iconX, 86 + nameplateYOffset, null);
				} else {
					Nadeshiko.INSTANCE.alert("Failed reading game mode icon for %s!", gameMode);
					return null;
				}
			}
		}

		// Reset transform before game-specific stats
		if (size == CardGame.CardSize.TINY) {
			((Graphics2D) graphics).setTransform(originalTransform);
		}

		if (game == CardGame.SKYBLOCK_GENERAL) {
			// Pass already fetched SkyBlock data
			((SkyBlockGeneralCardProvider) game.getProvider()).generate(card, data, statsResponse, skyblockProfileData);
		} else {
			game.getProvider().generate(card, data, statsResponse);
		}

		return ImageUtil.getBytesFromImage(card);
	}

	private void registerFont(String filename) throws Exception {

		try (InputStream fontStream = CardGenerator.class.getResourceAsStream(filename)) {

			// Ensure the font exists
			if (fontStream == null) {
				Nadeshiko.INSTANCE.alert("Tried to register non-existent font %s!", filename);
				return;
			}

			Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		}
	}
}
