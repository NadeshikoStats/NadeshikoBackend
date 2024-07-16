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

    public String formatNumber(double value) {
        String[] suffixes = new String[]{"", "K", "M", "B", "T"};

        if (value == 0) {
            return "0";
        }

        int exponent = (int) (Math.log10(value) / 3);
        if (exponent >= suffixes.length) {
            exponent = suffixes.length - 1;
        }

        double baseValue = value / Math.pow(10, exponent * 3);

        return String.format("%.1f%s", baseValue, suffixes[exponent]);
    }
}
