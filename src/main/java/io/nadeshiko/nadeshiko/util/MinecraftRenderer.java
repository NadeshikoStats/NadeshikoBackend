package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class MinecraftRenderer {

	public void drawCenterString(Graphics graphics, String string, int x, int y, int size) {
		int width = width(graphics, string, size);
		drawString(graphics, string, x - (width / 2), y, size);
	}

	public void drawString(Graphics graphics, String string, int x, int y, int size) {

		Font minecraftFont = new Font("Minecraft Regular", Font.PLAIN, size);
		Font minecraftBold = new Font("Minecraft Bold", Font.BOLD, size);
		graphics.setFont(minecraftFont);

		char[] array = string.toCharArray();

		Color currentColor = Color.WHITE;
		Color currentShadowColor = Color.DARK_GRAY;

		for (int character = 0; character < string.length(); character++) {

			while (array[character] == MinecraftColors.SECTION) {
				char nextChar = array[character + 1];

				if (nextChar == 'l') {
					graphics.setFont(minecraftBold);
				} else {
					currentColor = MinecraftColors.getColorFromCode(nextChar);
					currentShadowColor = MinecraftColors.getShadowColorFromCode(nextChar);
				}

				character += 2;
			}

			// Draw the character's shadow
			graphics.setColor(currentShadowColor);
			graphics.drawString(Character.toString(array[character]), x + size / 8 - 1, y + size / 8 - 1);

			// Draw the character
			graphics.setColor(currentColor);
			graphics.drawString(Character.toString(array[character]), x, y);


			x += graphics.getFontMetrics().charWidth(array[character]);
		}
	}

	public int width(Graphics graphics, String string, int size) {

		Font minecraftFont = new Font("Minecraft Regular", Font.PLAIN, size);
		int width = 0;

		char[] array = string.toCharArray();

		for (int character = 0; character < string.length(); character++) {

			if (array[character] == MinecraftColors.SECTION) {
				character += 2;
			}

			width += graphics.getFontMetrics(minecraftFont).charWidth(array[character]);
		}

		return width;
	}

}
