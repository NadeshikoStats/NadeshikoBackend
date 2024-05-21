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
	protected final Font mediumLight = new Font("Inter Medium", Font.PLAIN, 20);
	protected final Font mediumBold = new Font("Inter Medium", Font.BOLD, 20);

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

	protected void drawProgress(Graphics2D g, int x, int y, int maxWidth, double progress) {
		this.drawProgress(g, x, y, maxWidth, 8, progress);
	}

	protected void drawProgress(Graphics2D g, int x, int y, int maxWidth, int height, double progress) {
		Color originalColor = g.getColor();
		g.setColor(this.color);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRoundRect(x, y, (int) (progress * maxWidth), height, height, height);

		g.setColor(originalColor);
	}

	public abstract void generate(BufferedImage image, JsonObject stats);
}
