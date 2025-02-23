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
import java.util.Locale;

public class SkywarsCardProvider extends CardProvider {

	private record SkywarsStats(
		int kills,
		int deaths,
		int wins,
		int losses,
		int winstreak,
		double kdr,
		double wlr,
		String levelFormatted
	) {}

	public SkywarsCardProvider() {
		super(CardGame.SKYWARS);
	}

	private SkywarsStats extractStats(JsonObject skywars) {
		// Default values in case stats are missing
		int kills = 0;
		int deaths = 1; // Avoid division by zero
		int wins = 0;
		int losses = 1; // Avoid division by zero
		int winstreak = 0;
		String levelFormatted = "";

		// Extract stats if they exist
		if (skywars != null) {
			if (skywars.has("kills") && !skywars.get("kills").isJsonNull()) {
				kills = skywars.get("kills").getAsInt();
			}
			if (skywars.has("deaths") && !skywars.get("deaths").isJsonNull()) {
				deaths = Math.max(1, skywars.get("deaths").getAsInt()); // Fixes divzero problem
			}
			if (skywars.has("wins") && !skywars.get("wins").isJsonNull()) {
				wins = skywars.get("wins").getAsInt();
			}
			if (skywars.has("losses") && !skywars.get("losses").isJsonNull()) {
				losses = Math.max(1, skywars.get("losses").getAsInt()); // Fixes divzero problem
			}
			if (skywars.has("win_streak") && !skywars.get("win_streak").isJsonNull()) {
				winstreak = skywars.get("win_streak").getAsInt();
			}
			if (skywars.has("levelFormattedWithBrackets") && !skywars.get("levelFormattedWithBrackets").isJsonNull()) {
				levelFormatted = skywars.get("levelFormattedWithBrackets").getAsString();
			}
		}

		double kdr = Math.round((kills / (double) deaths) * 100) / 100d;
		double wlr = Math.round((wins / (double) losses) * 100) / 100d;

		return new SkywarsStats(kills, deaths, wins, losses, winstreak, kdr, wlr, levelFormatted);
	}

	@Override
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

	private void generateFull(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject skywars = stats.getAsJsonObject("stats").getAsJsonObject("SkyWars");
		SkywarsStats swStats = extractStats(skywars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw stars
		g.setColor(Color.WHITE);
		MinecraftRenderer.drawMinecraftString(g, swStats.levelFormatted, 900, 67, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw K/D ratio
		g.drawString(String.valueOf(swStats.kdr), 750 - (g.getFontMetrics().stringWidth(String.valueOf(swStats.kdr)) / 2), 158);
		this.drawProgress(g, 664, 173, 177, swStats.kills / (double) (swStats.kills + swStats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(swStats.wlr), 1007 - (g.getFontMetrics().stringWidth(String.valueOf(swStats.wlr)) / 2), 158);
		this.drawProgress(g, 921, 173, 177, swStats.wins / (double) (swStats.wins + swStats.losses));

		// Draw wins and winstreak
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int finalsWidth = g.getFontMetrics().stringWidth("Kills");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");

		g.drawString("Wins", 1175, 140);
		g.drawString("Kills", 1175, 170);
		g.drawString("Winstreak", 1175, 208);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", swStats.wins), 1175 + winsWidth + 10, 140);
		g.drawString(String.format("%,d", swStats.kills), 1175 + finalsWidth + 10, 170);
		g.drawString(String.format("%,d", swStats.winstreak), 1175 + winstreakWidth + 10, 208);

		// Draw top modes
		this.drawMode(g, Mode.SOLO, skywars, 635);
		this.drawMode(g, Mode.TEAM, skywars, 1068);
	}

	private void generateTiny(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject skywars = stats.getAsJsonObject("stats").getAsJsonObject("SkyWars");
		SkywarsStats swStats = extractStats(skywars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw stars
		g.setColor(Color.WHITE);
		MinecraftRenderer.drawMinecraftString(g, swStats.levelFormatted, 728, 44, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw K/D ratio
		g.drawString(String.valueOf(swStats.kdr), 615 - (g.getFontMetrics().stringWidth(String.valueOf(swStats.kdr)) / 2), 118);
		this.drawProgress(g, 534, 133, 177, swStats.kills / (double) (swStats.kills + swStats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(swStats.wlr), 860 - (g.getFontMetrics().stringWidth(String.valueOf(swStats.wlr)) / 2), 118);
		this.drawProgress(g, 776, 133, 177, swStats.wins / (double) (swStats.wins + swStats.losses));

		// Draw wins and winstreak
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int finalsWidth = g.getFontMetrics().stringWidth("Kills");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");

		g.drawString("Wins", 1015, 100);
		g.drawString("Kills", 1015, 125);
		g.drawString("Winstreak", 1015, 165);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", swStats.wins), 1015 + winsWidth + 10, 100);
		g.drawString(String.format("%,d", swStats.kills), 1015 + finalsWidth + 10, 125);
		g.drawString(String.format("%,d", swStats.winstreak), 1015 + winstreakWidth + 10, 165);
	}

	private void drawMode(Graphics2D g, @NonNull Mode mode, @NonNull JsonObject bedwarsStats, int baseX) {

		int kills = 0, deaths = 1, wins = 0, losses = 1;

		if (bedwarsStats.has("kills_" + mode.getApiName())) {
			kills = bedwarsStats.get("kills_" + mode.getApiName()).getAsInt();
		}

		if (bedwarsStats.has("deaths_" + mode.getApiName())) {
			deaths = bedwarsStats.get("deaths_" + mode.getApiName()).getAsInt();
		}

		if (bedwarsStats.has( "wins_" + mode.getApiName())) {
			wins = bedwarsStats.get("wins_" + mode.getApiName()).getAsInt();
		}

		if (bedwarsStats.has("losses_" + mode.getApiName())) {
			losses = bedwarsStats.get("losses_" + mode.getApiName()).getAsInt();
		}

		// Set up the name font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Medium", Font.BOLD, 22));

		// Draw mode name
		int nameWidth = g.getFontMetrics().stringWidth(mode.getDisplayName().toUpperCase());
		g.drawString(mode.getDisplayName().toUpperCase(Locale.ROOT), baseX, 290);

		// Draw the line beside the mode name
		g.setColor(this.getColor());
		g.fillRect(baseX + nameWidth + 15, 280, 350 - nameWidth - 15, 2);
		g.setColor(Color.WHITE);

		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw K/D ratio
		String kdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		g.drawString(kdr, baseX + 78 - (g.getFontMetrics().stringWidth(kdr) / 2), 340);
		this.drawProgress(g, baseX + 6, 354, 146, kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, baseX + 261 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, baseX + 189, 354, 146, wins / (double) (wins + losses));

		// Draw kills
		int finalKillsWidth = g.getFontMetrics(smallLight).stringWidth("Kills  ");
		int finalKillsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", kills));
		int finalKillsTotalWidth = finalKillsWidth + finalKillsCountWidth;
		int finalKillsLeftX = baseX + 80 - (finalKillsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Kills", finalKillsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", kills), finalKillsLeftX + finalKillsWidth, 425);

		// Draw wins
		int winsWidth = g.getFontMetrics(smallLight).stringWidth("Wins  ");
		int winsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", wins));
		int winsTotalWidth = winsWidth + winsCountWidth;
		int winsLeftX = baseX + 263 - (winsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Wins", winsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", wins), winsLeftX + winsWidth, 425);
	}

	@Getter
	@AllArgsConstructor
	private enum Mode {
		SOLO("solo", "Solo"),
		TEAM("team", "Doubles");

		private final String apiName;
		private final String displayName;
	}
}
