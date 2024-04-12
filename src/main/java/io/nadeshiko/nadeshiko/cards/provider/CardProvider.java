package io.nadeshiko.nadeshiko.cards.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public abstract class CardProvider {

	private Color color = Color.WHITE;

	protected final Font smallLight = new Font("Inter Medium", Font.PLAIN, 18);
	protected final Font smallBold = new Font("Inter Medium", Font.BOLD, 18);

	public CardProvider(CardGame game) {
		try (InputStream stream = CardProvider.class.getResourceAsStream("/cards/templates/colors.json")) {

			// Ensure colors.json exists
			if (stream == null) {
				Nadeshiko.INSTANCE.alert("cards/templates/colors.json was not found!");
				return;
			}

			JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

			// Ensure the game color is defined in colors.json
			if (!json.has(game.name())) {
				Nadeshiko.logger.warn("Game color of {} was not set! Falling back...", game.name());
				return;
			}

			color = Color.decode(json.get(game.name()).getAsString());

		} catch (IOException e) {
			Nadeshiko.INSTANCE.alert("Error while creating CardProvider for %s!", game.name());
		}
	}

	protected void drawProgress(Graphics g, int x, int y, int max, double progress) {
		Color originalColor = g.getColor();
		g.setColor(this.color);

		g.fillRoundRect(x, y, (int) (progress * max), 8, 8, 8);

		g.setColor(originalColor);
	}

	public abstract void generate(BufferedImage image, JsonObject stats);
}
