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
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
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
		JsonObject statsResponse = Nadeshiko.INSTANCE.getStatsCache().get(name, true);

		// Cards for invalid usernames
		if (!statsResponse.get("success").getAsBoolean()) {
			if (statsResponse.has("cause") && statsResponse.get("cause").getAsString().contains("No player by the name")) {
				return this.generatePlayerNotFoundCard(name);
			}
		}

		String badge = statsResponse.get("badge").getAsString();
		boolean hasBadge = !badge.isEmpty() && !badge.equals("NONE");

		BufferedImage card;
		Graphics graphics;
		AffineTransform originalTransform = null;

		// Get size from the data
		String sizeStr = data.has("size") ? data.get("size").getAsString().toUpperCase() : "FULL";
		CardGame.CardSize size;

		if (sizeStr.equals("LEGACY_FULL")) {
			size = CardGame.CardSize.FULL;
			data.addProperty("size", "FULL"); // This is deprecated, but thousands of people on Hypixel Forums have this param
		} else {
			if (sizeStr.equals("FULL")) {
				sizeStr = "FORUMS";
			}
			try {
				size = CardGame.CardSize.valueOf(sizeStr);
			} catch (IllegalArgumentException e) {
				size = CardGame.CardSize.FORUMS;
			}
			data.addProperty("size", size.name()); // Normalize
		}

		// Read the template from the resources
		String templatePath = "/cards/templates/" + game.name() + size.getTemplateSuffix();

		// Handle fallback for TINY and FORUMS templates
		if (size == CardGame.CardSize.TINY || size == CardGame.CardSize.FORUMS) {
			try (InputStream stream = CardGenerator.class.getResourceAsStream(templatePath)) {
				if (stream == null) {
					size = CardGame.CardSize.FULL; // Fallback to FULL size
					templatePath = "/cards/templates/" + game.name() + size.getTemplateSuffix();
				}
			}
		}
		
		try (InputStream templateStream = CardGenerator.class.getResourceAsStream(templatePath)) {
			byte[] cardTemplateBytes;

			if (templateStream != null) {
				cardTemplateBytes = templateStream.readAllBytes();
				card = ImageUtil.createImageFromBytes(cardTemplateBytes);
				graphics = card.getGraphics();
				
				if (size.getMainContentScale() != 1.0) {
					Graphics2D g2d = (Graphics2D) graphics;
					// Save the original transform
					originalTransform = g2d.getTransform();
					// scaling and translation
					g2d.scale(size.getMainContentScale(), size.getMainContentScale());
					g2d.translate(size.getMainContentTranslateX(), size.getMainContentTranslateY());
				}
				
				// anti aliasing
				if (size.isUseAntialiasing()) {
					Graphics2D g2d = (Graphics2D) graphics;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
					g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				}
			} else {
				Nadeshiko.INSTANCE.alert("Failed reading card template for %s!", game.name());
				return null;
			}
		}

		JsonObject profileObject = statsResponse.has("profile") && !statsResponse.get("profile").isJsonNull()
				? statsResponse.getAsJsonObject("profile")
				: null;
		// try to fix null errors
		if (profileObject == null) {
			Nadeshiko.logger.warn("Stats cache returned response without profile for {}. Invalidating and re-fetching.", name);
			Nadeshiko.INSTANCE.getStatsCache().invalidate(name);
			statsResponse = Nadeshiko.INSTANCE.getStatsCache().get(name, true); // one more attempt (cache has been cleared)
			if (statsResponse.has("profile") && !statsResponse.get("profile").isJsonNull()) {
				profileObject = statsResponse.getAsJsonObject("profile");
			} else {
				Nadeshiko.logger.error("Still failed to obtain Hypixel profile for {} after invalidating cache. Full response: {}", name, statsResponse);
				throw new RuntimeException("Failed to obtain Hypixel profile for " + name);
			}
		}

		// Get the player's SkyBlock profile
		JsonObject skyblockProfile = null;
		if (game == CardGame.SKYBLOCK_GENERAL) {
			HTTPUtil.Response profileResponse = HTTPUtil.get("https://playerdb.co/api/player/minecraft/" + name);
			JsonObject minecraftProfile = JsonParser.parseString(profileResponse.response()).getAsJsonObject();
			
			if (minecraftProfile == null || !minecraftProfile.has("data") || 
			minecraftProfile.get("code").getAsString().equals("minecraft.invalid_username")) {
				throw new RuntimeException("Could not find player " + name);
			}
			
			String uuid = minecraftProfile.getAsJsonObject("data").getAsJsonObject("player").get("raw_id").getAsString();
			JsonObject skyblockData = Nadeshiko.INSTANCE.getSkyBlockCache().get(uuid, null);
			
			if (!skyblockData.get("success").getAsBoolean()) {
				throw new RuntimeException("Failed to fetch SkyBlock data: " + skyblockData.get("cause").getAsString());
			}

			skyblockProfile = skyblockData.getAsJsonObject("skyblock_profile");
			if (skyblockProfile == null) {
				throw new RuntimeException("No SkyBlock profile found");
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

		String playerRenderUrl = size.getPlayerRenderUrl(name);
		int playerX = size.getPlayerX();
		int playerY = size.getPlayerY();

		// Get the player render
		byte[] playerBytes = HTTPUtil.getRaw(playerRenderUrl,
			new HashMap<>() {{
				put("User-Agent", "nadeshiko.io (+https://nadeshiko.io; contact@nadeshiko.io)");
			}}).response();
		BufferedImage playerImage = ImageUtil.createImageFromBytes(playerBytes);

		// Draw the player
		graphics.drawImage(playerImage, playerX, playerY, null);

		// Draw the name tag
		String displayName = profileObject.get("tagged_name").getAsString();
		
		// Get game mode and determine icon
		String gameMode = null;
		int gameModeIconWidth = 0;
		String gameModeIconPath = null;
		if (game == CardGame.SKYBLOCK_GENERAL && skyblockProfile != null && 
			skyblockProfile.has("profile") && 
			skyblockProfile.getAsJsonObject("profile").has("game_mode")) {
			
			gameMode = skyblockProfile.getAsJsonObject("profile").get("game_mode").getAsString();
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

		BufferedImage nameplateImage = this.generateNameplateImage(
			size, displayName, badge, hasBadge, gameMode, gameModeIconPath, gameModeIconWidth
		);

		int nameplateX = size.getNameplateX();
		int nameplateY = 83 + size.getNameplateYOffset();
		graphics.drawImage(nameplateImage, nameplateX - (nameplateImage.getWidth() / 2), nameplateY, null);

		// Reset transform before game-specific stats
		if (originalTransform != null) {
			((Graphics2D) graphics).setTransform(originalTransform);
		}

		CardProvider provider = game.getProvider();
		if (game == CardGame.SKYBLOCK_GENERAL) {
			((SkyBlockGeneralCardProvider) provider).generate(card, data, statsResponse, skyblockProfile);
		} else {
			provider.generate(card, data, statsResponse);
		}

		return ImageUtil.getBytesFromImage(card);
	}

	private byte[] generatePlayerNotFoundCard(String name) throws Exception {
		// The text to be drawn
		String line1 = "⚠ No player by the name of \"" + name + "\" was found.";
		String line2Part1 = "Change this card's username or use a UUID → ";
		String line2Part2 = "nadeshiko.io";
		String line2 = line2Part1 + line2Part2;

		BufferedImage DUMMY_IMG = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = DUMMY_IMG.createGraphics();
		g2d.setFont(new Font("Inter Medium", Font.PLAIN, 14));
		FontMetrics fm = g2d.getFontMetrics();
		int width = Math.max(fm.stringWidth(line1), fm.stringWidth(line2)) + 40; // padding
		g2d.dispose();

		int height = 80;

		BufferedImage card = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = card.createGraphics();
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, width, height);

		g.setFont(new Font("Inter Medium", Font.PLAIN, 14));

		// anti aliasing
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);


		// center text
		FontMetrics metrics = g.getFontMetrics();
		g.setColor(new Color(255, 0, 0));
		g.drawString(line1, (width - metrics.stringWidth(line1)) / 2, (height - metrics.getHeight()) / 2 + metrics.getAscent() - 8);
		

		// all this is needed because nadeshiko.io needs to be rendered in white
		int line2Y = (height - metrics.getHeight()) / 2 + metrics.getAscent() + 12;
		int line2Part1Width = metrics.stringWidth(line2Part1);
		int totalWidthLine2 = metrics.stringWidth(line2);
		int line2StartX = (width - totalWidthLine2) / 2;
		g.drawString(line2Part1, line2StartX, line2Y);

		g.setColor(new Color(255, 255, 255));
		g.drawString(line2Part2, line2StartX + line2Part1Width, line2Y);

		g.dispose();
		return ImageUtil.getBytesFromImage(card);
	}

	private BufferedImage generateNameplateImage(
		CardGame.CardSize size, String displayName, String badge, boolean hasBadge,
		String gameMode, String gameModeIconPath, int gameModeIconWidth) throws Exception {


		BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D dummyGraphics = dummyImage.createGraphics();

		int fontSize = size.getNameplateFontSize();
		int nameWidth = MinecraftRenderer.minecraftWidth(dummyGraphics, displayName, fontSize);
		int totalWidth = nameWidth;

		if (hasBadge) {
			totalWidth += 34 + 10;
		}
		if (gameModeIconPath != null) {
			totalWidth += gameModeIconWidth + 10;
		}

		int plateWidth = totalWidth + 20;
		int plateHeight = 50;

		BufferedImage nameplate = new BufferedImage(plateWidth, plateHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = nameplate.createGraphics();

		// Apply antialiasing
		if (size.isUseAntialiasing()) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		}

		int textX = plateWidth / 2;
		if (hasBadge) {
			textX -= (34 + 10) / 2;
		}
		if (gameModeIconPath != null) {
			textX -= (gameModeIconWidth + 10) / 2;
		}

		// background
		g.setColor(new Color(0, 0, 0, 128));
		g.fillRect(0, 0, plateWidth, plateHeight);

		MinecraftRenderer.drawCenterMinecraftString(g, displayName, textX, 37, fontSize);

		// badge
		if (hasBadge) {
			try (InputStream badgeStream = CardGenerator.class.getResourceAsStream("/cards/badge/" + badge + ".png")) {
				if (badgeStream != null) {
					byte[] badgeBytes = badgeStream.readAllBytes();
					BufferedImage badgeImage = ImageUtil.createImageFromBytes(badgeBytes);
					g.drawImage(badgeImage, textX + (nameWidth / 2) + 10, 8, null);
				}
			}
		}

		// game icon
		if (gameModeIconPath != null) {
			try (InputStream iconStream = CardGenerator.class.getResourceAsStream(gameModeIconPath)) {
				if (iconStream != null) {
					byte[] iconBytes = iconStream.readAllBytes();
					BufferedImage iconImage = ImageUtil.createImageFromBytes(iconBytes);
					int iconX = textX + (nameWidth / 2) + 10;
					if (hasBadge) {
						iconX += 34 + 10;
					}
					g.drawImage(iconImage, iconX, 3, null);
				}
			}
		}

		g.dispose();
		dummyGraphics.dispose();

		// scale nameplate
		double scale = size.getNameplateScale();
		if (scale != 1.0) {
			int newWidth = (int) (plateWidth * scale);
			int newHeight = (int) (plateHeight * scale);
			Image scaledImage = nameplate.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			BufferedImage scaledNameplate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = scaledNameplate.createGraphics();
			g2d.drawImage(scaledImage, 0, 0, null);
			g2d.dispose();
			return scaledNameplate;
		}

		return nameplate;
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
