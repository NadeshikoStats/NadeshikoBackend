package io.nadeshiko.nadeshiko.hypixel;

import lombok.experimental.UtilityClass;

/**
 * Utility class for the conversion and handling of network level and EXP. Based strongly on the official Hypixel API
 * Java implementation.
 *
 * @author Plancke, chloe
 */
@UtilityClass
public class NetworkLevel {

	public static final double BASE = 10_000;
	public static final double GROWTH = 2_500;

	/**
	 * Constants to generate the total amount of XP to complete a level
	 */
	public static final double HALF_GROWTH = 0.5 * GROWTH;

	/**
	 * Constants to look up the level from the total amount of XP
	 */
	public static final double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
	public static final double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
	public static final double GROWTH_DIVIDES_2 = 2 / GROWTH;


	/**
	 * This method returns the level of a player calculated by the current experience gathered. The result is
	 * a precise level of the player. The value is not zero-indexed and represents the absolute visible level
	 * for the player.
	 * The result can't be smaller than 1 and negative experience results in level 1.
	 * <p>
	 * Examples:
	 * -        0 XP = 1.0
	 * -     5000 XP = 1.0
	 * -    10000 XP = 2.0
	 * -    50000 XP = 4.0
	 * - 79342431 XP = 249.0
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
	 * <p>
	 * Examples:
	 * -        0 XP = 1.0
	 * -     5000 XP = 1.5
	 * -    10000 XP = 2.0
	 * -    50000 XP = 4.71...
	 * - 79342431 XP = 249.46...
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
	 * <p>
	 * Examples:
	 * -    1.0 =        0.0 XP
	 * -    2.0 =    10000.0 XP
	 * -    3.0 =    22500.0 XP
	 * -    5.0 =    55000.0 XP
	 * -  5.764 =    70280.0 XP
	 * -  130.0 = 21930000.0 XP
	 * - 250.43 = 79951975.0 XP
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
	static double getTotalExpToFullLevel(double level) {
		return (HALF_GROWTH * (level - 2) + BASE) * (level - 1);
	}

	/**
	 * This method returns the current progress of this level to reach the next level. This method is as
	 * precise as possible due to rounding errors on the mantissa. The first 10 decimals are totally
	 * accurate.
	 * <p>
	 * Examples:
	 * -     5000.0 XP   (Lv. 1) = 0.5                               (50 %)
	 * -    22499.0 XP   (Lv. 2) = 0.99992                       (99.992 %)
	 * -  5324224.0 XP  (Lv. 62) = 0.856763076923077   (85.6763076923077 %)
	 * - 23422443.0 XP (Lv. 134) = 0.4304905109489051 (43.04905109489051 %)
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
