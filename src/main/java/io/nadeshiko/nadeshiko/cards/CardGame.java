/*
 * This file is a part of the nadeshiko project. nadeshiko is free software, licensed under the MIT license.
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

package io.nadeshiko.nadeshiko.cards;

import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.cards.provider.impl.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public enum CardGame {
	BEDWARS(BedwarsCardProvider.class),
	BUILD_BATTLE(BuildBattleCardProvider.class),
	DUELS(DuelsCardProvider.class),
	FISHING(FishingCardProvider.class),
	NETWORK(NetworkCardProvider.class),
	SKYBLOCK_GENERAL(SkyBlockGeneralCardProvider.class),
	SKYBLOCK_DUNGEONS(SkyblockDungeonsCardProvider.class),
	SKYWARS(SkywarsCardProvider.class);

	private final Class<? extends CardProvider> providerClass;

	private CardProvider provider = null;

	@Getter
	public enum CardSize {
		FULL(
			".png",
			"https://visage.surgeplay.com/bust/333/%s.png",
			138,
			165,
			40,
			300,
			0,
			1.0,
			0,
			0,
			1.0,
			false
		),
	
		FORUMS(
			"_FORUMS.png",
			"https://visage.surgeplay.com/full/368/%s.png",
			115,
			89,
			40,
			228, 
			-40,
			1.0,
			0,
			0,
			0.6,
			true
		),

		TINY(
			"_TINY.png",
			"https://visage.surgeplay.com/bust/333/%s.png",
			138,
			165,
			40,
			300,
			20,
			0.66,
			40,
			-80,
			1,
			true
		);

		private final String templateSuffix;
		private final String playerRenderUrl;
		private final int playerX;
		private final int playerY;
		private final int nameplateFontSize;
		private final int nameplateX;
		private final int nameplateYOffset;
		private final double mainContentScale;
		private final double mainContentTranslateX;
		private final double mainContentTranslateY;
		private final double nameplateScale;
		private final boolean useAntialiasing;

		CardSize(String templateSuffix, String playerRenderUrl, int playerX, int playerY, 
				int nameplateFontSize, int nameplateX, int nameplateYOffset, double mainContentScale,
				double mainContentTranslateX, double mainContentTranslateY, double nameplateScale, boolean useAntialiasing) {
			this.templateSuffix = templateSuffix;
			this.playerRenderUrl = playerRenderUrl;
			this.playerX = playerX;
			this.playerY = playerY;
			this.nameplateFontSize = nameplateFontSize;
			this.nameplateX = nameplateX;
			this.nameplateYOffset = nameplateYOffset;
			this.mainContentScale = mainContentScale;
			this.mainContentTranslateX = mainContentTranslateX;
			this.mainContentTranslateY = mainContentTranslateY;
			this.nameplateScale = nameplateScale;
			this.useAntialiasing = useAntialiasing;
		}

		public String getPlayerRenderUrl(String name) {
			return String.format(this.playerRenderUrl, name);
		}
		

		// Constants for FORUMS card layout positioning
		public static class ForumsConstants {
			public static final int FIRST_RATIO_POSITION_X = 514 + (150 / 2);
			public static final int SECOND_RATIO_POSITION_X = 731 + (150 / 2);
			public static final int RATIO_POSITION_Y = 123 - 5;

			public static final int FIRST_PROGRESS_POSITION_X = 514;
			public static final int SECOND_PROGRESS_POSITION_X = 731;
			public static final int PROGRESS_POSITION_Y = 134 - 5;
			public static final int PROGRESS_WIDTH = 150;
			public static final int PROGRESS_HEIGHT = 7;

			public static final int STATS_LABEL_START_X = 940;
			public static final int STATS_VALUE_OFFSET = 10;

			public static final int MAJOR_STAT_1_X = 586;
			public static final int MAJOR_STAT_2_X = 810;
			public static final int MAJOR_STAT_Y = 203 - 5;

			// Bottom box
			public static final int BOTTOM_BOX_QUAD_1_X = 485;
			public static final int BOTTOM_BOX_QUAD_2_X = 714;
			public static final int BOTTOM_BOX_QUAD_1_Y = 284 - 5;
			public static final int BOTTOM_BOX_QUAD_2_Y = 308 - 5;

			// For stuff like the Bed Wars level
			public static final int TITLE_POSITION_Y = 40;

			// two side boxes
			public static final int SIDE_BOX_TITLE_X = 979;
			public static final int SIDE_BOX_TITLE_Y = 89 - 5;

			public static final int SIDE_BOX_LABEL_1_X = 1056;
			public static final int SIDE_BOX_LABEL_2_X = 1205;
			public static final int SIDE_BOX_LABEL_Y = 129 - 5;

			public static final int SIDE_BOX_PROGRESS_1_X = 996;
			public static final int SIDE_BOX_PROGRESS_2_X = 1144;
			public static final int SIDE_BOX_PROGRESS_Y = 136 - 5;

			public static final int SIDE_BOX_PROGRESS_WIDTH = 120;
			public static final int SIDE_BOX_PROGRESS_HEIGHT = 6;

			public static final int SIDE_BOX_STAT_SLOT_X = 1287;
			public static final int SIDE_BOX_STAT_SLOT_1_Y = 128 - 5;
			public static final int SIDE_BOX_STAT_SLOT_2_Y = 152 - 5;

			public static final int SIDE_BOX_SECOND_BOX_DIFFERENCE_Y = 143;
		}
	}

	@SneakyThrows
	public CardProvider getProvider() {
		if (this.provider == null) {
			this.provider = this.providerClass.getDeclaredConstructor().newInstance();
		}

		return this.provider;
	}
}
