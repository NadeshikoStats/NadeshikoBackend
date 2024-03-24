package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;

/**
 * Utility class for the handling and conversion of Minecraft color and formatting codes
 * @author chloe
 */
@UtilityClass
public class MinecraftColors {

	public final char SECTION = '§';

	private final HashMap<String, String> nameToCode = new HashMap<>() {{
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

	public String getCodeFromName(String colorName) {
		return MinecraftColors.nameToCode.get(colorName);
	}

}
