package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.HashMap;

/**
 * Utility class for the handling and conversion of Minecraft color and formatting codes
 * @author chloe
 */
@UtilityClass
public class MinecraftColors {

	public final char SECTION = '§';

	public final HashMap<String, String> nameToCode = new HashMap<>() {{
		put("BLACK", SECTION + "0");
		put("DARK_BLUE", SECTION + "1");
		put("DARK_GREEN", SECTION + "2");
		put("DARK_AQUA", SECTION + "3");
		put("DARK_RED", SECTION + "4");
		put("DARK_PURPLE", SECTION + "5");
		put("GOLD", SECTION + "6");
		put("GRAY", SECTION + "7");
		put("DARK_GRAY", SECTION + "8");
		put("BLUE", SECTION + "9");
		put("GREEN", SECTION + "a");
		put("AQUA", SECTION + "b");
		put("RED", SECTION + "c");
		put("LIGHT_PURPLE", SECTION + "d");
		put("YELLOW", SECTION + "e");
		put("WHITE", SECTION + "f");
	}};

	public final HashMap<Character, Color> codeToColor = new HashMap<>() {{
		put('0', new Color(0, 0, 0)); // Black
		put('1', new Color(0, 0, 170)); // Dark Blue
		put('2', new Color(0, 170, 0)); // Dark Green
		put('3', new Color(0, 170, 170)); // Dark Aqua
		put('4', new Color(170, 0, 0)); // Dark Red
		put('5', new Color(170, 0, 170)); // Dark Purple
		put('6', new Color(255, 170, 0)); // Gold
		put('7', new Color(170, 170, 170)); // Gray
		put('8', new Color(85, 85, 85)); // Dark Gray
		put('9', new Color(85, 85, 255)); // Blue
		put('a', new Color(85, 255, 85)); // Green
		put('b', new Color(85, 255, 255)); // Aqua
		put('c', new Color(255, 85, 85)); // Red
		put('d', new Color(255, 85, 255)); // Light Purple
		put('e', new Color(255, 255, 85)); // Yellow
		put('f', new Color(255, 255, 255)); // White
	}};

	public final HashMap<Character, Color> codeToShadowColor = new HashMap<>() {{
		put('0', new Color(0, 0, 0)); // Black shadow
		put('1', new Color(0, 0, 42)); // Dark Blue shadow
		put('2', new Color(0, 42, 0)); // Dark Green shadow
		put('3', new Color(0, 42, 42)); // Dark Aqua shadow
		put('4', new Color(42, 0, 0)); // Dark Red shadow
		put('5', new Color(42, 0, 42)); // Dark Purple shadow
		put('6', new Color(42, 42, 0)); // Gold shadow
		put('7', new Color(42, 42, 42)); // Gray shadow
		put('8', new Color(21, 21, 21)); // Dark Gray shadow
		put('9', new Color(21, 21, 63)); // Blue shadow
		put('a', new Color(21, 63, 21)); // Green shadow
		put('b', new Color(21, 63, 63)); // Aqua shadow
		put('c', new Color(63, 21, 21)); // Red shadow
		put('d', new Color(63, 21, 63)); // Light Purple shadow
		put('e', new Color(63, 63, 21)); // Yellow shadow
		put('f', new Color(63, 63, 63)); // White shadow
	}};

	public String getCodeFromName(String colorName) {
		return nameToCode.get(colorName);
	}

	public Color getColorFromCode(char code) {
		return codeToColor.get(code);
	}

	public Color getShadowColorFromCode(char code) {
		return codeToShadowColor.get(code);
	}

}
