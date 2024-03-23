package io.nadeshiko.nadeshiko.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HypixelUtil {

	public double calculateNetworkLevel(int networkExp) {
		double root = Math.sqrt(Math.pow(8750, 2) + (5000d * networkExp));
		return 1 + ((-8750 + root) / 2500d);
	}

}
