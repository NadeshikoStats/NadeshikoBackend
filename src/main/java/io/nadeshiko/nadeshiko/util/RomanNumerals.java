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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for the handling and conversion of Roman Numerals
 */
@UtilityClass
public class RomanNumerals {

	/**
	 * Convert the provided number to Roman Numerals
	 * @param number The number to convert
	 * @return The provided number converted to Roman Numerals, stored as a String
	 */
	public String arabicToRoman(int number) {

		// Ensure that the provided number is valid and can be expressed in Roman Numerals
		if ((number <= 0) || (number > 4000)) {
			throw new IllegalArgumentException(number + " is not in range (0,4000]");
		}

		List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();

		while ((number > 0) && (i < romanNumerals.size())) {
			RomanNumeral currentSymbol = romanNumerals.get(i);

			if (currentSymbol.getValue() <= number) {
				stringBuilder.append(currentSymbol.name());
				number -= currentSymbol.getValue();
			} else {
				i++;
			}
		}

		return stringBuilder.toString();
	}

	@Getter
	@AllArgsConstructor
	enum RomanNumeral {
		I(1),
		IV(4),
		V(5),
		IX(9),
		X(10),
		XL(40),
		L(50),
		XC(90),
		C(100),
		CD(400),
		D(500),
		CM(900),
		M(1000);

		private final int value;

		public static List<RomanNumeral> getReverseSortedValues() {
			return Arrays.stream(values())
				.sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
				.collect(Collectors.toList());
		}
	}
}
