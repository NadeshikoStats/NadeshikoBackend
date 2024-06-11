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

package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

import java.awt.*;

/**
 * A basic Minecraft font text renderer
 * @author chloe
 */
@UtilityClass
public class MinecraftRenderer {

	/**
	 * Draw a centered string in a custom font, using {@code graphics}' current font
	 * @param graphics The {@link Graphics} instance to draw to
	 * @param string The text to draw
	 * @param x The x-position to draw the text at
	 * @param y The y-position to draw the text at
	 */
	public void drawCenterCustomString(Graphics graphics, String string, int x, int y) {
		int width = customWidth(graphics, string);
		drawCustomString(graphics, string, x - (width / 2), y);
	}

	/**
	 * Draw a centered string in the Minecraft font
	 * @param graphics The {@link Graphics} instance to draw to
	 * @param string The text to draw
	 * @param x The x-position to draw the text at
	 * @param y The y-position to draw the text at
	 * @param size The size to draw the text at
	 */
	public void drawCenterMinecraftString(Graphics graphics, String string, int x, int y, int size) {
		int width = minecraftWidth(graphics, string, size);
		drawMinecraftString(graphics, string, x - (width / 2), y, size);
	}

	/**
	 * Draw a string in a custom font, using {@code graphics}' current font
	 * @param graphics The {@link Graphics} instance to draw to
	 * @param string The text to draw
	 * @param x The x-position to draw the text at
	 * @param y The y-position to draw the text at
	 */
	public void drawCustomString(Graphics graphics, String string, int x, int y) {

		Font unifont = new Font("Unifont", Font.PLAIN, graphics.getFont().getSize());

		char[] array = string.toCharArray();

		Color currentColor = graphics.getColor();

		// Iterate over characters in the string
		for (int character = 0; character < string.length(); character++) {

			while (array[character] == MinecraftColors.SECTION) {
				char nextChar = array[character + 1];

				currentColor = MinecraftColors.getColorFromCode(nextChar);
				character += 2;
			}

			// Custom font
			if (array[character] < 128) {

				// Draw the character
				graphics.setColor(currentColor);
				graphics.drawString(Character.toString(array[character]), x, y);
				x += graphics.getFontMetrics().charWidth(array[character]);
			}

			// Unifont fallback
			else {
				Font currentFont = graphics.getFont();

				// Draw the character
				graphics.setFont(unifont);
				graphics.setColor(currentColor);
				graphics.drawString(Character.toString(array[character]), x, y + 2);
				x += graphics.getFontMetrics().charWidth(array[character]);

				graphics.setFont(currentFont);
			}
		}
	}

	/**
	 * Draw a string in the Minecraft font
	 * @param graphics The {@link Graphics} instance to draw to
	 * @param string The text to draw
	 * @param x The x-position to draw the text at
	 * @param y The y-position to draw the text at
	 * @param size The size to draw the text at
	 */
	public void drawMinecraftString(Graphics graphics, String string, int x, int y, int size) {

		Font minecraftFont = new Font("Minecraft Regular", Font.PLAIN, size);
		Font minecraftBold = new Font("Minecraft Bold", Font.BOLD, size);
		Font unifont = new Font("Unifont", Font.PLAIN, size);
		graphics.setFont(minecraftFont);

		char[] array = string.toCharArray();

		Color currentColor = MinecraftColors.getColorFromCode('f');
		Color currentShadowColor = MinecraftColors.getShadowColorFromCode('f');

		// Iterate over characters in the string
		for (int character = 0; character < string.length(); character++) {

			while (array[character] == MinecraftColors.SECTION) {
				char nextChar = array[character + 1];

				if (nextChar == 'l') {
					graphics.setFont(minecraftBold);
				} else if (nextChar == 'r') {
					graphics.setFont(minecraftFont);
					currentColor = MinecraftColors.getColorFromCode('f');
					currentShadowColor = MinecraftColors.getShadowColorFromCode('f');
				} else if ((nextChar >= '0' && nextChar <= '9') || (nextChar >= 'a' && nextChar <= 'f')) {
					graphics.setFont(minecraftFont); // Color codes remove bold
					currentColor = MinecraftColors.getColorFromCode(nextChar);
					currentShadowColor = MinecraftColors.getShadowColorFromCode(nextChar);
				}

				character += 2;
			}

			// Minecraft font
			if (array[character] < 128) {

				// Draw the character's shadow
				graphics.setColor(currentShadowColor);
				graphics.drawString(Character.toString(array[character]),
					x + size / 8 - 1, y + size / 8 - 1);

				// Draw the character
				graphics.setColor(currentColor);
				graphics.drawString(Character.toString(array[character]), x, y);
				x += graphics.getFontMetrics().charWidth(array[character]);
			}

			// Unifont fallback
			else {
				Font currentFont = graphics.getFont();

				// Draw the character's shadow
				graphics.setFont(unifont);
				graphics.setColor(currentShadowColor);
				graphics.drawString(Character.toString(array[character]),
					x + size / 8 - 1, y + size / 8 + 1);

				// Draw the character
				graphics.setColor(currentColor);
				graphics.drawString(Character.toString(array[character]), x, y + 2);
				x += graphics.getFontMetrics().charWidth(array[character]);

				graphics.setFont(currentFont);
			}
		}
	}

	/**
	 * Calculate the pixel width of the provided string
	 * @param graphics The {@link Graphics} instance
	 * @param string The string to calculate the width of
	 * @return The pixel width of {@code string} in {@code graphics}' current font
	 */
	public int customWidth(Graphics graphics, String string) {

		int width = 0;
		char[] array = string.toCharArray();

		// Iterate over characters in the string
		for (int character = 0; character < string.length(); character++) {

			// Skip the next two characters if we find a section symbol
			if (array[character] == MinecraftColors.SECTION) {
				character += 2;
			}

			// Add this character's width
			width += graphics.getFontMetrics().charWidth(array[character]);
		}

		return width;
	}

	/**
	 * Calculate the pixel width of the provided string in the Minecraft font
	 * @param graphics The {@link Graphics} instance
	 * @param string The string to calculate the width of
	 * @param size The size of the font to consider
	 * @return The pixel width of {@code string} in the Minecraft font at the provided size
	 */
	public int minecraftWidth(Graphics graphics, String string, int size) {

		Font minecraftFont = new Font("Minecraft Regular", Font.PLAIN, size);
		int width = 0;
		char[] array = string.toCharArray();

		// Iterate over characters in the string
		for (int character = 0; character < string.length(); character++) {

			// Skip the next two characters if we find a section symbol
			if (array[character] == MinecraftColors.SECTION) {
				character += 2;
			}

			// Add this character's width
			width += graphics.getFontMetrics(minecraftFont).charWidth(array[character]);
		}

		return width;
	}

}
