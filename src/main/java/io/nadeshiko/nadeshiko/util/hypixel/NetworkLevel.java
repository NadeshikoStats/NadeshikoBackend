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
 * Utility class for the conversion and handling of network level and EXP. Based strongly on the official Hypixel API
 * Java implementation.
 *
 * @since 0.0.1
 * @author Plancke, chloe
 */
@UtilityClass
public class NetworkLevel {

	public final double BASE = 10_000;
	public final double GROWTH = 2_500;

	/**
	 * Constants to generate the total amount of XP to complete a level
	 */
	public final double HALF_GROWTH = 0.5 * GROWTH;

	/**
	 * Constants to look up the level from the total amount of XP
	 */
	public final double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
	public final double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
	public final double GROWTH_DIVIDES_2 = 2 / GROWTH;

	/**
	 * This method returns the level of a player calculated by the current experience gathered. The result is
	 * a precise level of the player. The value is not zero-indexed and represents the absolute visible level
	 * for the player.
	 * <p>
	 *
	 * The result can't be smaller than 1 and negative experience results in level 1.
	 *
	 * @param exp Total experience gathered by the player.
	 * @return Absolute level of player (Smallest value is 1.0)
	 */
	public double getLevel(double exp) {
		return exp < 0 ? 1 : Math.floor(1 + REVERSE_PQ_PREFIX +
			Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * exp));
	}

	/**
	 * This method returns the level of a player calculated by the current experience gathered. The result is
	 * a precise level of the player. The value is not zero-indexed and represents the visible level
	 * for the player.
	 * The result can't be smaller than 1 and negative experience results in level 1.
	 *
	 * @param exp Total experience gathered by the player.
	 * @return Exact level of player (The smallest value is 1.0)
	 */
	public double getExactLevel(double exp) {
		return NetworkLevel.getLevel(exp) + NetworkLevel.getPercentageToNextLevel(exp);
	}

	/**
	 * This method returns the experience it needs to reach that level. If you want to reach the given level
	 * you have to gather the amount of experience returned by this method. This method is precise, that means
	 * you can pass any progress of a level to receive the experience to reach that progress. (5.764 to get
	 * the experience to reach level 5 with 76.4% of level 6.
	 *
	 * @param level The level and progress of the level to reach
	 * @return The experience required to reach that level and progress
	 */
	static double getTotalExpToLevel(double level) {
		double lv = Math.floor(level), x0 = NetworkLevel.getTotalExpToFullLevel(lv);
		if (level == lv) return x0;
		return (NetworkLevel.getTotalExpToFullLevel(lv + 1) - x0) * (level % 1) + x0;
	}

	/**
	 * Helper method that may only be called by full levels and has the same functionality as getTotalExpToLevel()
	 * but doesn't support progress and returns wrong values for progress due to perfect curve shape.
	 *
	 * @param level Level to receive the amount of experience to
	 * @return Experience to reach the given level
	 */
	public double getTotalExpToFullLevel(double level) {
		return (HALF_GROWTH * (level - 2) + BASE) * (level - 1);
	}

	/**
	 * This method returns the current progress of this level to reach the next level. This method is as
	 * precise as possible due to rounding errors on the mantissa. The first 10 decimals are totally
	 * accurate.
	 *
	 * @param exp Current experience gathered by the player
	 * @return Current progress to the next level
	 */
	public double getPercentageToNextLevel(double exp) {
		double lv = NetworkLevel.getLevel(exp), x0 = NetworkLevel.getTotalExpToLevel(lv);
		return (exp - x0) / (NetworkLevel.getTotalExpToLevel(lv + 1) - x0);
	}

	/**
	 * Gets the coin multiplier at the provided network level
	 * @param level The network level of a player
	 * @return The players coin multiplier
	 */
	public double getCoinMultiplier(double level) {
		if (level < 5) {
			return 1;
		} else if (level < 10) {
			return 1.5;
		} else if (level < 15) {
			return 2;
		} else if (level < 20) {
			return 2.5;
		} else if (level < 25) {
			return 3;
		} else if (level < 30) {
			return 3.5;
		} else if (level < 40) {
			return 4;
		} else if (level < 50) {
			return 4.5;
		} else if (level < 100) {
			return 5;
		} else if (level < 125) {
			return 5.5;
		} else if (level < 150) {
			return 6;
		} else if (level < 200) {
			return 6.5;
		} else if (level < 250) {
			return 7;
		} else {
			return 8;
		}
	}
}
