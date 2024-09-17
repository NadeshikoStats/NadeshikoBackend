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

@UtilityClass
public class NumberUtil {

    /**
     * Formats a given numeric value into a human-readable string representation with a suffix.
     * If the value is less than 1,000, no suffix is used. The formatted number is rounded to one decimal place.
     *
     * <p>Example cases:</p>
     * <ul>
     *   <li>1,234 -> "1.2K"</li>
     *   <li>1,000,000 -> "1.0M"</li>
     *   <li>1,000,000,000 -> "1.0B"</li>
     *   <li>1,500,000,000,000 -> "1.5T"</li>
     * </ul>
     *
     * @param value The numeric value to format
     * @return A string representing the formatted value with a suffix
     */
    public String formatNumber(double value) {
        String[] suffixes = new String[]{ "", "K", "M", "B", "T" };
        double[] powers = { 1.0, 1e3, 1e6, 1e9, 1e12 };

        if (value == 0) {
            return "0";
        }

        int exponent = Math.min((int) (Math.log10(value) / 3), suffixes.length - 1);
        double baseValue = value / powers[exponent];

        return String.format("%.1f%s", baseValue, suffixes[exponent]);
    }
}
