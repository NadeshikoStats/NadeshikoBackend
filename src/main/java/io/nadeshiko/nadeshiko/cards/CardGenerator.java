package io.nadeshiko.nadeshiko.cards;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

	public byte[] generateCard(CardGame game, String name) throws Exception {

		BufferedImage card;
		Graphics graphics;

		// Read the template from the resources
		try (InputStream templateStream =  CardGenerator.class.
			getResourceAsStream("/cards/templates/" + game.name() + ".png")) {

			byte[] cardTemplateBytes;

			if (templateStream != null) {
				cardTemplateBytes = templateStream.readAllBytes();
				card = createImageFromBytes(cardTemplateBytes);
				graphics = card.getGraphics();
			} else {
				Nadeshiko.logger.error("Failed reading card template for {}!", game.name());
				return null;
			}
		}

		// Fetch the player's stats
		String statsString = HTTPUtil.get("http://localhost:2000/stats?name=" + name).response();
		JsonObject statsResponse = JsonParser.parseString(statsString).getAsJsonObject();
		JsonObject profileObject = statsResponse.getAsJsonObject("profile");

		// Ensure the player is valid and fetching stats succeeded
		if (!statsResponse.has("success") || !statsResponse.get("success").getAsBoolean()) {
			Nadeshiko.logger.error("Failed generating {} card for {}!", game.name(), name);
			return statsResponse.toString().getBytes();
		}

		// Get the player render
		byte[] playerBytes = HTTPUtil.getRaw("https://visage.surgeplay.com/bust/333/" + name + ".png",
			new HashMap<>() {{
				put("User-Agent", "nadeshiko.io (+https://nadeshiko.io; contact@nadeshiko.io)");
			}}).response();
		BufferedImage playerImage = createImageFromBytes(playerBytes);

		// Draw the player
		graphics.drawImage(playerImage, 138, 165, null);

		// Draw the name tag
		int width = MinecraftRenderer.width(graphics, profileObject.get("tagged_name").getAsString(), 40);
		graphics.setColor(new Color(0, 0, 0, 128));
		graphics.fillRect(300 - (width / 2) - 10, 83, width + 20, 50);

		MinecraftRenderer.drawCenterString(graphics,
			profileObject.get("tagged_name").getAsString(), 300, 120, 40);

		// Populate the template using the game's provider
		game.getProvider().getDeclaredConstructor().newInstance().
			generate(card, statsResponse.getAsJsonObject("stats"));

		return getBytesFromImage(card);
	}

	public BufferedImage createImageFromBytes(byte[] imageData) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		return ImageIO.read(inputStream);
	}

	public byte[] getBytesFromImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", outputStream);
		return outputStream.toByteArray();
	}

	private void registerFont(String filename) throws Exception {

		try (InputStream fontStream = CardGenerator.class.getResourceAsStream(filename)) {

			// Ensure the font exists
			if (fontStream == null) {
				Nadeshiko.logger.error("Tried to register non-existent font {}!", filename);
				return;
			}

			Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		}
	}
}
