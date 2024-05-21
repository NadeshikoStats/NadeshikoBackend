/*
 * This file is a part of the Nadeshiko project. Nadeshiko is free software, licensed under the MIT license.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.ImageUtil;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;

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
			Nadeshiko.logger.error("Exception while registering fonts!");
			e.printStackTrace();
		}
	}

	public byte[] generateCard(CardGame game, JsonObject data) throws Exception {

		String name = data.get("name").getAsString();

		BufferedImage card;
		Graphics graphics;

		// Read the template from the resources
		try (InputStream templateStream =  CardGenerator.class.
			getResourceAsStream("/cards/templates/" + game.name() + ".png")) {

			byte[] cardTemplateBytes;

			if (templateStream != null) {
				cardTemplateBytes = templateStream.readAllBytes();
				card = ImageUtil.createImageFromBytes(cardTemplateBytes);
				graphics = card.getGraphics();
			} else {
				Nadeshiko.INSTANCE.alert("Failed reading card template for %s!", game.name());
				return null;
			}
		}

		// Fetch the player's stats
		String statsString = HTTPUtil.get("http://localhost:2000/stats?name=" + name).response();
		JsonObject statsResponse = JsonParser.parseString(statsString).getAsJsonObject();
		JsonObject profileObject = statsResponse.getAsJsonObject("profile");

		// Ensure the player is valid and fetching stats succeeded
		if (!statsResponse.has("success") || !statsResponse.get("success").getAsBoolean()) {
			Nadeshiko.INSTANCE.alert("Failed generating %s card for %s!", game.name(), name);
			return statsResponse.toString().getBytes();
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
		int width = MinecraftRenderer.minecraftWidth(graphics, profileObject.get("tagged_name").getAsString(), 40);
		graphics.setColor(new Color(0, 0, 0, 128));
		graphics.fillRect(300 - (width / 2) - 10, 83, width + 20, 50);

		MinecraftRenderer.drawCenterMinecraftString(graphics,
			profileObject.get("tagged_name").getAsString(), 300, 120, 40);

		// Populate the template using the game's provider
		game.getProvider().generate(card, statsResponse);

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
