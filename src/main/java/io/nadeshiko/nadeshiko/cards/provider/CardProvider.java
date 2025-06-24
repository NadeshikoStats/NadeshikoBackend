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

package io.nadeshiko.nadeshiko.cards.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.awt.Font;

@Getter
public abstract class CardProvider {

	private Color color = Color.WHITE;

	protected final Font plain15 = new Font("Inter Medium", Font.PLAIN, 15);
	protected final Font bold15 = new Font("Inter Medium", Font.BOLD, 15);
	protected final Font plain16 = new Font("Inter Medium", Font.PLAIN, 16);
	protected final Font bold16 = new Font("Inter Medium", Font.BOLD, 16);
	protected final Font plain18 = new Font("Inter Medium", Font.PLAIN, 18);
	protected final Font bold18 = new Font("Inter Medium", Font.BOLD, 18);
	protected final Font plain20 = new Font("Inter Medium", Font.PLAIN, 20);
	protected final Font bold20 = new Font("Inter Medium", Font.BOLD, 20);
	protected final Font bold38 = new Font("Inter Medium", Font.BOLD, 38);

	@Builder
	public static class DrawStatOptions {
		@NonNull String label;
		@NonNull String value;
		int x;
		int y;
		@Builder.Default int padding = 10;
		@Builder.Default Color valueColor = Color.WHITE;
		Font valueFont;
		Font labelFont;
		float fontSize;
		@Builder.Default boolean centered = false;
	}

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
		this.drawProgress(g, x, y, maxWidth, 8, progress, this.color);
	}

	protected void drawProgress(Graphics2D g, int x, int y, int maxWidth, int height, double progress) {
		this.drawProgress(g, x, y, maxWidth, height, progress, this.color);
	}

	protected void drawProgress(Graphics2D g, int x, int y, int maxWidth, int height, double progress, Color color) {
		Color originalColor = g.getColor();
		g.setColor(color);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRoundRect(x, y, (int) (progress * maxWidth), height, height, height);

		g.setColor(originalColor);
	}

	protected void drawTabularString(Graphics2D g, String str, int x, int y) {
		g.drawString(convertToTabularDigits(str), x, y);
	}
	
	protected String convertToTabularDigits(String str) {
		StringBuilder tabularStr = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (c >= '0' && c <= '9') {
				// Convert to tabular digits (U+e071 to U+e07a)
				tabularStr.append((char) (0xe071 + (c - '0')));
			} else {
				tabularStr.append(c);
			}
		}
		return tabularStr.toString();
	}
	
	protected void drawRightAlignedString(Graphics2D g, String str, int x, int y, boolean tabular) {
		String renderStr = tabular ? convertToTabularDigits(str) : str;
		FontMetrics metrics = g.getFontMetrics();
		int stringWidth = metrics.stringWidth(renderStr);
		int newX = x - stringWidth;
		
		g.drawString(renderStr, newX, y);
	}
	
	protected void drawCenterAlignedString(Graphics2D g, String str, int x, int y, boolean tabular) {
		String renderStr = tabular ? convertToTabularDigits(str) : str;
		FontMetrics metrics = g.getFontMetrics();
		int stringWidth = metrics.stringWidth(renderStr);
		int newX = x - stringWidth / 2;
		
		g.drawString(renderStr, newX, y);
	}

	protected void drawStat(Graphics2D g, DrawStatOptions options) {
		Font fontForValue = options.valueFont != null ? options.valueFont : bold16;
		Font fontForLabel = options.labelFont != null ? options.labelFont : plain16;

		if (options.fontSize > 0) {
			fontForValue = fontForValue.deriveFont(options.fontSize);
			fontForLabel = fontForLabel.deriveFont(options.fontSize);
		}

		int startX = options.x;

		if (options.centered) {
			g.setFont(fontForLabel);
			int labelWidth = g.getFontMetrics().stringWidth(options.label);

			g.setFont(fontForValue);
			int valueWidth = g.getFontMetrics().stringWidth(options.value);

			int totalWidth = labelWidth + options.padding + valueWidth;
			startX = options.x - totalWidth / 2;
		}

		// Draw label
		g.setColor(new Color(138, 138, 138));
		g.setFont(fontForLabel);
		g.drawString(options.label, startX, options.y);

		int labelWidth = g.getFontMetrics().stringWidth(options.label);

		// Draw value
		g.setColor(options.valueColor);
		g.setFont(fontForValue);
		g.drawString(options.value, startX + labelWidth + options.padding, options.y);
	}

	public abstract void generate(BufferedImage image, JsonObject data, JsonObject stats);

	/**
	 * @deprecated Use {@link #generate(BufferedImage, JsonObject, JsonObject)} instead
	 */
	@Deprecated
	public void generate(BufferedImage image, JsonObject stats) {
		// Default implementation for backward compatibility
		generate(image, new JsonObject(), stats);
	}
}
