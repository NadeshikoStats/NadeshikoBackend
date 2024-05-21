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

package io.nadeshiko.nadeshiko.util.hypixel;

import lombok.experimental.UtilityClass;

/**
 * Utility class for the conversion and handling of guild level and EXP.
 * @since 0.0.3
 * @author chloe
 */
@UtilityClass
public class GuildLevel {

	/**
	 * Array of EXP required for the first 15 levels of a guild. Past level 15, all levels require the same amount
	 * of EXP (3 million)
	 */
	private final int[] EXP_NEEDED = {
		100_000,
		150_000,
		250_000,
		500_000,
		750_000,
		1_000_000,
		1_250_000,
		1_500_000,
		2_000_000,
		2_500_000,
		2_500_000,
		2_500_000,
		2_500_000,
		2_500_000,
		3_000_000
	};

	/**
	 * The max guild level
	 */
	private final int MAX_LEVEL = 1000;

	/**
	 * Get the exact level of a guild given the total guild EXP
	 * @param exp The total EXP of a guild
	 * @return The exact guild level as a double, including progress to the next one
	 */
	public double getExactLevel(int exp) {

		double level = 0;

		// Iterate over all levels from 0 to the max until we run out of EXP remaining
		for (int i = 0; i < MAX_LEVEL; i++) {

			// The needed EXP for this level
			int needed_exp = EXP_NEEDED[Math.min(i, EXP_NEEDED.length - 1)];

			// If we've run out of EXP remaining
			if ((exp - needed_exp) < 0) {

				// Return the current level plus the progress to the next one
				return level + (exp / (double) needed_exp);
			}

			level++;
			exp -= needed_exp;
		}

		return level;
	}

}
