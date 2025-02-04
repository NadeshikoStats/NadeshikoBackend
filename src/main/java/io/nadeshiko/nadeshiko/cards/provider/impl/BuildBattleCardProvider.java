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
import java.util.function.Function;

public class BuildBattleCardProvider extends CardProvider {

	public BuildBattleCardProvider() {
		super(CardGame.BUILD_BATTLE);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject buildBattle = stats.getAsJsonObject("stats").getAsJsonObject("BuildBattle");

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		int wins = buildBattle.get("wins").getAsInt();
		int losses = buildBattle.get("games_played").getAsInt() - wins;
		int score = buildBattle.get("score").getAsInt();
				int votes = buildBattle.has("total_votes") ? buildBattle.get("total_votes").getAsInt() : 0;
		int coins = buildBattle.get("coins").getAsInt();
		int highestScore = stats.getAsJsonObject("achievements").get("buildbattle_build_battle_points").getAsInt();

		// Draw title
		g.setColor(Color.WHITE);
		MinecraftRenderer.drawMinecraftString(g, Title.get(score).format(score), 930, 67, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, 750 - (g.getFontMetrics().stringWidth(wlr) / 2), 158);
		this.drawProgress(g, 664, 173, 177, wins / (double) (wins + losses));

		// Draw stats
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int scoreWidth = g.getFontMetrics().stringWidth("Score");
		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int votesWidth = g.getFontMetrics().stringWidth("Votes");
		int coinsWidth = g.getFontMetrics().stringWidth("Coins");
		int highestScoreWidth = g.getFontMetrics().stringWidth("Highest Score");

		g.drawString("Score", 950, 140);
		g.drawString("Wins", 950, 170);
		g.drawString("Votes", 950, 200);
		g.drawString("Coins", 1175, 140);
		g.drawString("Highest Score", 1175, 170);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);

		g.drawString(String.format("%,d", score), 950 + scoreWidth + 10, 140);
		g.drawString(String.format("%,d", wins), 950 + winsWidth + 10, 170);
		g.drawString(String.format("%,d", votes), 950 + votesWidth + 10, 200);
		g.drawString(String.format("%,d", coins), 1175 + coinsWidth + 10, 140);
		g.drawString(String.format("%,d", highestScore), 1175 + highestScoreWidth + 10, 170);

		// Draw top modes
		this.drawMode(g, Mode.SOLO, buildBattle, 635, 318);
		this.drawMode(g, Mode.TEAM, buildBattle, 1068, 318);
		this.drawMode(g, Mode.PRO, buildBattle, 635, 438);
		this.drawMode(g, Mode.GTB, buildBattle, 1068, 438);
	}

	private void drawMode(Graphics2D g, @NonNull Mode mode, @NonNull JsonObject stats, int baseX, int baseY) {

		int wins = 0;

		if (stats.has( "wins_" + mode.getApiName())) {
			wins = stats.get("wins_" + mode.getApiName()).getAsInt();
		}


		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw wins
		int winsWidth = g.getFontMetrics(smallLight).stringWidth("Wins  ");

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Wins", baseX, baseY);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", wins), baseX + winsWidth, baseY);
	}

	@Getter
	@AllArgsConstructor
	private enum Mode {
		SOLO("solo_normal", "Solo"),
		TEAM("teams_normal", "Teams"),
		PRO("solo_pro", "Pro"),
		GTB("guess_the_build", "Guess The Build");

		private final String apiName;
		private final String displayName;
	}

	@Getter
	@AllArgsConstructor
	private enum Title {
		ROOKIE(0, score -> "§fRookie"),
		UNTRAINED(100, score -> "§7Untrained"),
		AMATEUR(250, score -> "§8Amateur"),
		PROSPECT(500, score -> "§aProspect"),
		APPRENTICE(1000, score -> "§2Apprentice"),
		EXPERIENCED(2000, score -> "§bExperienced"),
		SEASONED(3500, score -> "§3Seasoned"),
		TRAINED(5000, score -> "§9Trained"),
		SKILLED(7500, score -> "§1Skilled"),
		TALENTED(10000, score -> "§5Talented"),
		PROFESSIONAL(15000, score -> "§2Professional"),
		ARTISAN(20000, score -> "§cArtisan"),
		EXPERT(30000, score -> "§4Expert"),
		MASTER(50000, score -> "§6Master"),
		LEGEND(100000, score -> "§a§lLegend"),
		GRANDMASTER(200000, score -> "§b§lGrandmaster"),
		CELESTIAL(300000, score -> "§d§lCelestial"),
		DIVINE(400000, score -> "§c§lDivine"),
		ASCENDED(500000, score -> "§6§lAscended");

		private final int requirement;
		private final Function<String, String> format;

		public String format(int score) {
			return this.format.apply(Integer.toString(score));
		}

		/**
		 * Get the title a given score belongs to by iterating over titles until the requirement is not met
		 * @param score The total score to analyze
		 * @return The title that the given score belongs to
		 */
		public static Title get(int score) {
			Title currentTitle = ROOKIE;

			for (Title title : Title.values()) {
				if (title.getRequirement() > score) {
					return currentTitle;
				} else {
					currentTitle = title;
				}
			}

			return currentTitle;
		}
	}
}
