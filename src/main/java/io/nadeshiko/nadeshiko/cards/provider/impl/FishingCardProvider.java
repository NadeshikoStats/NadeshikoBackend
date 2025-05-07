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

package io.nadeshiko.nadeshiko.cards.provider.impl;

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

public class FishingCardProvider extends CardProvider {

	private record FishingStats(
		int specialFish,
		int mythicalFish,
		// Ice stats
		int iceFish,
		int iceTreasure,
		int iceJunk,
		int iceTotal,
		// Water stats
		int waterFish,
		int waterTreasure,
		int waterJunk,
		int waterTotal,
		// Lava stats
		int lavaFish,
		int lavaTreasure,
		int lavaJunk,
		int lavaTotal,
		// Overall stats
		int overallFish,
		int overallTreasure,
		int overallJunk,
		int overallTotal
	) {}

	public FishingCardProvider() {
		super(CardGame.FISHING);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		System.out.println("how did this even get called?");
		generateFull(image, stats);
	}

	/**
	 * Generate a card with the given data and stats
	 * @param image The image to draw on
	 * @param data The data object containing parameters like size
	 * @param stats The stats object containing player stats
	 */
	public void generate(BufferedImage image, JsonObject data, JsonObject stats) {
		// Get the size from the data object
		CardGame.CardSize size = CardGame.CardSize.FULL; // Default to FULL
				
		if (data.has("size")) {
			try {
				String sizeStr = data.get("size").getAsString().toUpperCase();
				size = CardGame.CardSize.valueOf(sizeStr);
			} catch (IllegalArgumentException ignored) {
				// Bad size
			}
		}

		// pick a size
		switch (size) {
			case TINY:
				generateTiny(image, stats);
				break;
			case FULL:
			default:
				generateFull(image, stats);
				break;
		}
	}

	private FishingStats extractStats(JsonObject fishing) {
		// Default values in case stats are missing
		int specialFish = 0;
		int mythicalFish = 0;
		int iceFish = 0;
		int iceTreasure = 0;
		int iceJunk = 0;
		int iceTotal = 0;
		int waterFish = 0;
		int waterTreasure = 0;
		int waterJunk = 0;
		int waterTotal = 0;
		int lavaFish = 0;
		int lavaTreasure = 0;
		int lavaJunk = 0;
		int lavaTotal = 0;
		int overallFish = 0;
		int overallTreasure = 0;
		int overallJunk = 0;
		int overallTotal = 0;

		// Safely extract stats if they exist
		if (fishing != null) {
			if (fishing.has("special_fish") && !fishing.get("special_fish").isJsonNull()) {
				JsonObject specialFishObject = fishing.getAsJsonObject("special_fish");
                for (String key : specialFishObject.keySet()) {
                    if (!specialFishObject.get(key).isJsonNull() && !key.equals("mahi-mahi")) {
                        specialFish += 1;
                    }
                }
			}

			if (fishing.has("orbs") && !fishing.get("orbs").isJsonNull()) {
				JsonObject orbs = fishing.getAsJsonObject("orbs");
				for (String key : orbs.keySet()) {
					if (!key.equals("weight") && !orbs.get(key).isJsonNull()) {
						mythicalFish += orbs.get(key).getAsInt();
					}
				}
			}

			if (fishing.has("stats") && !fishing.get("stats").isJsonNull()) {
				JsonObject stats = fishing.getAsJsonObject("stats");
				if (stats.has("permanent") && !stats.get("permanent").isJsonNull()) {
					JsonObject permanent = stats.getAsJsonObject("permanent");
					if (permanent.has("ice") && !permanent.get("ice").isJsonNull()) {
						JsonObject ice = permanent.getAsJsonObject("ice");
						if (ice.has("fish") && !ice.get("fish").isJsonNull()) {
							iceFish = ice.get("fish").getAsInt();
						}
						if (ice.has("treasure") && !ice.get("treasure").isJsonNull()) {
							iceTreasure = ice.get("treasure").getAsInt();
						}
						if (ice.has("junk") && !ice.get("junk").isJsonNull()) {
							iceJunk = ice.get("junk").getAsInt();
						}
					}
				}
			}

			if (fishing.has("stats") && !fishing.get("stats").isJsonNull()) {
				JsonObject stats = fishing.getAsJsonObject("stats");
				if (stats.has("permanent") && !stats.get("permanent").isJsonNull()) {
					JsonObject permanent = stats.getAsJsonObject("permanent");
					if (permanent.has("water") && !permanent.get("water").isJsonNull()) {
						JsonObject water = permanent.getAsJsonObject("water");
						if (water.has("fish") && !water.get("fish").isJsonNull()) {
							waterFish = water.get("fish").getAsInt();
						}
						if (water.has("treasure") && !water.get("treasure").isJsonNull()) {
							waterTreasure = water.get("treasure").getAsInt();
						}
						if (water.has("junk") && !water.get("junk").isJsonNull()) {
							waterJunk = water.get("junk").getAsInt();
						}
					}
				}
			}

			if (fishing.has("stats") && !fishing.get("stats").isJsonNull()) {
				JsonObject stats = fishing.getAsJsonObject("stats");
				if (stats.has("permanent") && !stats.get("permanent").isJsonNull()) {
					JsonObject permanent = stats.getAsJsonObject("permanent");
					if (permanent.has("lava") && !permanent.get("lava").isJsonNull()) {
						JsonObject lava = permanent.getAsJsonObject("lava");
						if (lava.has("fish") && !lava.get("fish").isJsonNull()) {
							lavaFish = lava.get("fish").getAsInt();
						}
						if (lava.has("treasure") && !lava.get("treasure").isJsonNull()) {
							lavaTreasure = lava.get("treasure").getAsInt();
						}
						if (lava.has("junk") && !lava.get("junk").isJsonNull()) {
							lavaJunk = lava.get("junk").getAsInt();
						}
					}
				}
			}
		}

		iceTotal = iceFish + iceTreasure + iceJunk;
		waterTotal = waterFish + waterTreasure + waterJunk;
		lavaTotal = lavaFish + lavaTreasure + lavaJunk;
		overallFish = iceFish + waterFish + lavaFish;
		overallTreasure = iceTreasure + waterTreasure + lavaTreasure;
		overallJunk = iceJunk + waterJunk + lavaJunk;
		overallTotal = overallFish + overallTreasure + overallJunk + specialFish + mythicalFish;

		return new FishingStats(specialFish, mythicalFish, iceFish, iceTreasure, iceJunk, iceTotal,
			waterFish, waterTreasure, waterJunk, waterTotal, lavaFish, lavaTreasure, lavaJunk, lavaTotal,
			overallFish, overallTreasure, overallJunk, overallTotal);
	}

	private void generateFull(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		// Safely get Fishing stats, defaulting to empty object if not found
		JsonObject fishing = stats.has("stats") && !stats.get("stats").isJsonNull() 
			? stats.getAsJsonObject("stats").has("MainLobby") && !stats.getAsJsonObject("stats").get("MainLobby").isJsonNull()
				? stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").has("fishing") && !stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").get("fishing").isJsonNull()
					? stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").getAsJsonObject("fishing")
					: new JsonObject()
				: new JsonObject()
			: new JsonObject();
		FishingStats fsStats = extractStats(fishing);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(this.hugeBold);

        this.drawCenterAlignedString(g, String.format("%,d", fsStats.overallTotal), 814, 168, false);

        g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int fishWidth = g.getFontMetrics().stringWidth("Fish");
		int treasureWidth = g.getFontMetrics().stringWidth("Treasure");
		int junkWidth = g.getFontMetrics().stringWidth("Junk");
        int specialFishWidth = g.getFontMetrics().stringWidth("Special Fish");
        int mythicalFishWidth = g.getFontMetrics().stringWidth("Mythical Fish");

		g.drawString("Fish", 627+402-19, 140);
		g.drawString("Treasure", 627+402-19, 170);
		g.drawString("Junk", 627+402-19, 200);
		g.drawString("Special Fish", 846+402-19, 140);
		g.drawString("Mythical Fish", 846+402-19, 170);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);

        g.drawString(String.format("%,d", fsStats.overallFish), 627+402+fishWidth+10-19, 140);
        g.drawString(String.format("%,d", fsStats.overallTreasure), 627+402+treasureWidth+10-19, 170);
        g.drawString(String.format("%,d", fsStats.overallJunk), 627+402+junkWidth+10-19, 200);

        g.drawString(String.format("%,d", fsStats.specialFish), 846+402+specialFishWidth+10-19, 140);
        g.drawString(String.format("%,d", fsStats.mythicalFish), 846+402+mythicalFishWidth+10-19, 170);

        g.setFont(this.mediumBold);

        drawModeStats(g, "Water", fsStats.waterFish, fsStats.waterTreasure, fsStats.waterJunk, fsStats.waterTotal, 627+223-11, 330);
		drawModeStats(g, "Ice", fsStats.iceFish, fsStats.iceTreasure, fsStats.iceJunk, fsStats.iceTotal, 914+223-11, 330);
		drawModeStats(g, "Lava", fsStats.lavaFish, fsStats.lavaTreasure, fsStats.lavaJunk, fsStats.lavaTotal, 1201+223-11, 330);
	}

	private void generateTiny(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		// Safely get Fishing stats, defaulting to empty object if not found
		JsonObject fishing = stats.has("stats") && !stats.get("stats").isJsonNull() 
			? stats.getAsJsonObject("stats").has("MainLobby") && !stats.getAsJsonObject("stats").get("MainLobby").isJsonNull()
				? stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").has("fishing") && !stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").get("fishing").isJsonNull()
					? stats.getAsJsonObject("stats").getAsJsonObject("MainLobby").getAsJsonObject("fishing")
					: new JsonObject()
				: new JsonObject()
			: new JsonObject();
		FishingStats fsStats = extractStats(fishing);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(this.hugeBold);

        int tinyXOffset = -140;
        int tinyYOffset = -32;
        int tinyXCatchesOffset = -28-44;

        this.drawCenterAlignedString(g, String.format("%,d", fsStats.overallTotal), 814+tinyXOffset-36, 168+tinyYOffset, false);

        g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int fishWidth = g.getFontMetrics().stringWidth("Fish");
		int treasureWidth = g.getFontMetrics().stringWidth("Treasure");
		int junkWidth = g.getFontMetrics().stringWidth("Junk");
        int specialFishWidth = g.getFontMetrics().stringWidth("Special Fish");
        int mythicalFishWidth = g.getFontMetrics().stringWidth("Mythical Fish");

		g.drawString("Fish", 627+402-19+tinyXOffset+tinyXCatchesOffset, 140+tinyYOffset);
		g.drawString("Treasure", 627+402-19+tinyXOffset+tinyXCatchesOffset, 170+tinyYOffset);
		g.drawString("Junk", 627+402-19+tinyXOffset+tinyXCatchesOffset, 200+tinyYOffset);
		g.drawString("Special Fish", 846+402-19+tinyXOffset+tinyXCatchesOffset, 140+tinyYOffset);
		g.drawString("Mythical Fish", 846+402-19+tinyXOffset+tinyXCatchesOffset, 170+tinyYOffset);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);

        g.drawString(String.format("%,d", fsStats.overallFish), 627+402+fishWidth+10-19+tinyXOffset+tinyXCatchesOffset, 140+tinyYOffset);
        g.drawString(String.format("%,d", fsStats.overallTreasure), 627+402+treasureWidth+10-19+tinyXOffset+tinyXCatchesOffset, 170+tinyYOffset);
        g.drawString(String.format("%,d", fsStats.overallJunk), 627+402+junkWidth+10-19+tinyXOffset+tinyXCatchesOffset, 200+tinyYOffset);

        g.drawString(String.format("%,d", fsStats.specialFish), 846+402+specialFishWidth+10-19+tinyXOffset+tinyXCatchesOffset, 140+tinyYOffset);
        g.drawString(String.format("%,d", fsStats.mythicalFish), 846+402+mythicalFishWidth+10-19+tinyXOffset+tinyXCatchesOffset, 170+tinyYOffset);
	}

	private void drawModeStats(Graphics2D g, String mode, int fish, int treasure, int junk, int total, int x, int y) {
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		// Draw values
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		this.drawRightAlignedString(g, String.format("%,d", fish), x + 10, y, true);
		this.drawRightAlignedString(g, String.format("%,d", treasure), x + 10, y + 34, true);
		this.drawRightAlignedString(g, String.format("%,d", junk), x + 10, y + 68, true);
		this.drawRightAlignedString(g, String.format("%,d", total), x + 10, y + 102, true);
	}

	@Getter
	@AllArgsConstructor
	private enum Mode {
		ICE("ice", "Ice"),
		WATER("water", "Water"),
		LAVA("lava", "Lava");

		private final String apiName;
		private final String displayName;
	}
}
