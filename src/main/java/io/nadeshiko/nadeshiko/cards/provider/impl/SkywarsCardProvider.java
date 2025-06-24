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
import io.nadeshiko.nadeshiko.util.JsonUtil;
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
		String levelFormatted,
		int heads,
		int souls,
		int coins
	) {}

	public SkywarsCardProvider() {
		super(CardGame.SKYWARS);
	}

	private SkywarsStats extractStats(JsonObject skywars) {
		int kills = JsonUtil.getInt(skywars, "kills", 0);
		int deaths = Math.max(1, JsonUtil.getInt(skywars, "deaths", 1));
		int wins = JsonUtil.getInt(skywars, "wins", 0);
		int losses = Math.max(1, JsonUtil.getInt(skywars, "losses", 1));
		int winstreak = JsonUtil.getInt(skywars, "win_streak", 0);
		String levelFormatted = "";
		if (skywars != null && skywars.has("levelFormattedWithBrackets") && !skywars.get("levelFormattedWithBrackets").isJsonNull()) {
			levelFormatted = skywars.get("levelFormattedWithBrackets").getAsString();
		}
		int heads = JsonUtil.getInt(skywars, "heads", 0);
		int souls = JsonUtil.getInt(skywars, "souls", 0);
		int coins = JsonUtil.getInt(skywars, "coins", 0);


		double kdr = Math.round((kills / (double) deaths) * 100) / 100d;
		double wlr = Math.round((wins / (double) losses) * 100) / 100d;

		return new SkywarsStats(kills, deaths, wins, losses, winstreak, kdr, wlr, levelFormatted, heads, souls, coins);
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
			case FORUMS:
				generateForums(image, stats);
				break;
			case FULL:
			default:
				generateFull(image, stats);
				break;
		}
	}

	private void generateForums(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject skywars = stats.has("stats") && !stats.get("stats").isJsonNull()
			? stats.getAsJsonObject("stats").has("SkyWars")
			&& !stats.getAsJsonObject("stats").get("SkyWars").isJsonNull()
			? stats.getAsJsonObject("stats").getAsJsonObject("SkyWars")
			: new JsonObject()
			: new JsonObject();
		SkywarsStats swStats = extractStats(skywars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw level
		MinecraftRenderer.drawMinecraftString(g, swStats.levelFormatted, 690, CardGame.CardSize.ForumsConstants.TITLE_POSITION_Y, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 34));

		// Draw K/D ratio
		g.drawString(String.valueOf(swStats.kdr),
			CardGame.CardSize.ForumsConstants.FIRST_RATIO_POSITION_X
				- (g.getFontMetrics().stringWidth(String.valueOf(swStats.kdr)) / 2),
			CardGame.CardSize.ForumsConstants.RATIO_POSITION_Y);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.FIRST_PROGRESS_POSITION_X,
			CardGame.CardSize.ForumsConstants.PROGRESS_POSITION_Y, CardGame.CardSize.ForumsConstants.PROGRESS_WIDTH,
			CardGame.CardSize.ForumsConstants.PROGRESS_HEIGHT,
			swStats.kills / (double) (swStats.kills + swStats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(swStats.wlr),
			CardGame.CardSize.ForumsConstants.SECOND_RATIO_POSITION_X
				- (g.getFontMetrics().stringWidth(String.valueOf(swStats.wlr)) / 2),
			CardGame.CardSize.ForumsConstants.RATIO_POSITION_Y);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.SECOND_PROGRESS_POSITION_X,
			CardGame.CardSize.ForumsConstants.PROGRESS_POSITION_Y, CardGame.CardSize.ForumsConstants.PROGRESS_WIDTH,
			CardGame.CardSize.ForumsConstants.PROGRESS_HEIGHT,
			swStats.wins / (double) (swStats.wins + swStats.losses));

		drawStat(g, DrawStatOptions.builder().label("Kills").value(String.format("%,d", swStats.kills))
			.x(CardGame.CardSize.ForumsConstants.MAJOR_STAT_1_X).y(CardGame.CardSize.ForumsConstants.MAJOR_STAT_Y)
			.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).centered(true)
			.fontSize(20)
			.build());
		drawStat(g, DrawStatOptions.builder().label("Wins").value(String.format("%,d", swStats.wins))
			.x(CardGame.CardSize.ForumsConstants.MAJOR_STAT_2_X).y(CardGame.CardSize.ForumsConstants.MAJOR_STAT_Y)
			.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).centered(true)
			.fontSize(20).build());

		// Winstreaks might be disabled on the API
		drawStat(g,
			DrawStatOptions.builder().label("Winstreak").value(String.format("%,d", swStats.winstreak))
				.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_X)
				.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());

		// other bottom box stats
		drawStat(g,
			DrawStatOptions.builder().label("Heads").value(String.format("%,d", swStats.heads))
				.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_X)
				.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());
		drawStat(g,
			DrawStatOptions.builder().label("Souls").value(String.format("%,d", swStats.souls))
				.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_X)
				.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());
		drawStat(g,
			DrawStatOptions.builder().label("Coins").value(String.format("%,d", swStats.coins))
				.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_X)
				.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());

		// Draw top modes
		this.drawSideBox(g, Mode.SOLO, skywars, 0);
		this.drawSideBox(g, Mode.TEAM, skywars, 1);
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
		g.setFont(plain18);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int finalsWidth = g.getFontMetrics().stringWidth("Kills");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");

		g.drawString("Wins", 1175, 140);
		g.drawString("Kills", 1175, 170);
		g.drawString("Winstreak", 1175, 208);

		g.setColor(Color.WHITE);
		g.setFont(bold18);
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
		g.setFont(plain18);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int finalsWidth = g.getFontMetrics().stringWidth("Kills");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");

		g.drawString("Wins", 1015, 100);
		g.drawString("Kills", 1015, 125);
		g.drawString("Winstreak", 1015, 165);

		g.setColor(Color.WHITE);
		g.setFont(bold18);
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
		int finalKillsWidth = g.getFontMetrics(plain18).stringWidth("Kills  ");
		int finalKillsCountWidth = g.getFontMetrics(bold18).stringWidth(String.format("%,d", kills));
		int finalKillsTotalWidth = finalKillsWidth + finalKillsCountWidth;
		int finalKillsLeftX = baseX + 80 - (finalKillsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(plain18);
		g.drawString("Kills", finalKillsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(bold18);
		g.drawString(String.format("%,d", kills), finalKillsLeftX + finalKillsWidth, 425);

		// Draw wins
		int winsWidth = g.getFontMetrics(plain18).stringWidth("Wins  ");
		int winsCountWidth = g.getFontMetrics(bold18).stringWidth(String.format("%,d", wins));
		int winsTotalWidth = winsWidth + winsCountWidth;
		int winsLeftX = baseX + 263 - (winsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(plain18);
		g.drawString("Wins", winsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(bold18);
		g.drawString(String.format("%,d", wins), winsLeftX + winsWidth, 425);
	}

	private void drawSideBox(Graphics2D g, @NonNull Mode mode, @NonNull JsonObject skywarsStats, int boxNumber) {
		int kills = JsonUtil.getInt(skywarsStats, "kills_" + mode.getApiName(), 0);
		int deaths = Math.max(1, JsonUtil.getInt(skywarsStats, "deaths_" + mode.getApiName(), 1));
		int wins = JsonUtil.getInt(skywarsStats, "wins_" + mode.getApiName(), 0);
		int losses = Math.max(1, JsonUtil.getInt(skywarsStats, "losses_" + mode.getApiName(), 1));

		// Set up the name font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Medium", Font.BOLD, 20));

		// Draw mode name
		int nameWidth = g.getFontMetrics().stringWidth(mode.getDisplayName().toUpperCase());
		g.drawString(mode.getDisplayName().toUpperCase(Locale.ROOT), CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_X,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y);

		// Draw the line beside the mode name
		g.setColor(this.getColor());
		g.fillRect(CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_X + nameWidth + 15,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y - 10,
			450 - nameWidth - 15, 2);
		g.setColor(Color.WHITE);

		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw final K/D ratio
		String fkdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		this.drawCenterAlignedString(g, fkdr, CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_1_X,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
			false);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_1_X,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_WIDTH,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_HEIGHT,
			kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		this.drawCenterAlignedString(g, wlr, CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_2_X,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
			false);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_2_X,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_WIDTH,
			CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_HEIGHT, wins / (double) (wins + losses));

		// Draw final kills, wins
		this.drawStat(g,
			DrawStatOptions.builder().label("Kills").value(String.format("%,d", kills))
				.x(CardGame.CardSize.ForumsConstants.SIDE_BOX_STAT_SLOT_X)
				.y(CardGame.CardSize.ForumsConstants.SIDE_BOX_STAT_SLOT_1_Y
					+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET)
				.build());
		this.drawStat(g, DrawStatOptions.builder()
			.label("Wins")
			.value(String.format("%,d", wins))
			.x(CardGame.CardSize.ForumsConstants.SIDE_BOX_STAT_SLOT_X)
			.y(CardGame.CardSize.ForumsConstants.SIDE_BOX_STAT_SLOT_2_Y
				+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y)
			.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET)
			.build());
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
