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

	public CardProvider(CardGame game) {
		try (InputStream stream = CardProvider.class.getResourceAsStream("/cards/templates/colors.json")) {

			// Ensure colors.json exists
			if (stream == null) {
				Nadeshiko.logger.error("cards/templates/colors.json was not found!");
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
			Nadeshiko.logger.error("Error while creating CardProvider for {}!", game.name());
		}
	}

	public abstract void generate(BufferedImage image);
}
