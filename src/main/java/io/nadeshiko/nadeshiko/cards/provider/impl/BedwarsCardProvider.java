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
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

public class BedwarsCardProvider extends CardProvider {

	private record BedwarsStats(
			int finalKills,
			int finalDeaths,
			int wins,
			int losses,
			int winstreak,
			double fkdr,
			double wlr,
			boolean hasWinstreak,
			int bedsBroken,
			int slumberTickets,
			int challengesCompleted) {
	}

	public BedwarsCardProvider() {
		super(CardGame.BEDWARS);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		System.out.println("how did this even get called?");
		generateFull(image, stats);
	}

	/**
	 * Generate a card with the given data and stats
	 * 
	 * @param image The image to draw on
	 * @param data  The data object containing parameters like size
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
			case TINY -> generateTiny(image, stats);
			case FORUMS -> generateForums(image, stats);
			case FULL -> generateFull(image, stats);
			default -> generateFull(image, stats);
		}
	}

	private BedwarsStats extractStats(JsonObject bedwars) {
		// Safely extract stats if they exist
		int finalKills = JsonUtil.getInt(bedwars, "final_kills_bedwars", 0);
		int finalDeaths = Math.max(1, JsonUtil.getInt(bedwars, "final_deaths_bedwars", 1));
		int wins = JsonUtil.getInt(bedwars, "wins_bedwars", 0);
		int losses = Math.max(1, JsonUtil.getInt(bedwars, "losses_bedwars", 1));
		int winstreak = JsonUtil.getInt(bedwars, "winstreak", 0);
		int bedsBroken = JsonUtil.getInt(bedwars, "beds_broken_bedwars", 0);
		int challengesCompleted = JsonUtil.getInt(bedwars, "total_challenges_completed", 0);
		boolean hasWinstreak = bedwars != null && bedwars.has("winstreak") && !bedwars.get("winstreak").isJsonNull();

		// located in slumber -> tickets
		int slumberTickets = 0;
		if (bedwars != null && bedwars.has("slumber") && !bedwars.get("slumber").isJsonNull()) {
			slumberTickets = JsonUtil.getInt(bedwars.getAsJsonObject("slumber"), "tickets", 0);
		}

		double fkdr = Math.round((finalKills / (double) finalDeaths) * 100) / 100d;
		double wlr = Math.round((wins / (double) losses) * 100) / 100d;

		return new BedwarsStats(finalKills, finalDeaths, wins, losses, winstreak, fkdr, wlr, hasWinstreak, bedsBroken,
				slumberTickets, challengesCompleted);
	}

	private void generateFull(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		// Safely get Bedwars stats, defaulting to empty object if not found
		JsonObject bedwars = stats.has("stats") && !stats.get("stats").isJsonNull()
				? stats.getAsJsonObject("stats").has("Bedwars")
						&& !stats.getAsJsonObject("stats").get("Bedwars").isJsonNull()
								? stats.getAsJsonObject("stats").getAsJsonObject("Bedwars")
								: new JsonObject()
				: new JsonObject();
		BedwarsStats bwStats = extractStats(bedwars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw stars
		this.drawStar(g, bedwars, 900, 67, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw final K/D ratio
		g.drawString(String.valueOf(bwStats.fkdr),
				750 - (g.getFontMetrics().stringWidth(String.valueOf(bwStats.fkdr)) / 2), 158);
		this.drawProgress(g, 664, 173, 177, bwStats.finalKills / (double) (bwStats.finalKills + bwStats.finalDeaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(bwStats.wlr),
				1007 - (g.getFontMetrics().stringWidth(String.valueOf(bwStats.wlr)) / 2), 158);
		this.drawProgress(g, 921, 173, 177, bwStats.wins / (double) (bwStats.wins + bwStats.losses));

		// Draw wins and winstreak
		drawStat(g, DrawStatOptions.builder().label("Wins").value(String.format("%,d", bwStats.wins)).x(1175).y(140)
				.build());
		drawStat(g, DrawStatOptions.builder().label("Final Kills").value(String.format("%,d", bwStats.finalKills))
				.x(1175).y(170).build());

		// Winstreaks might be disabled on the API
		if (bwStats.hasWinstreak) {
			drawStat(g, DrawStatOptions.builder().label("Winstreak").value(String.format("%,d", bwStats.winstreak))
					.x(1175).y(208).build());
		} else {
			drawStat(g, DrawStatOptions.builder().label("Winstreak").value("Unknown").x(1175).y(208).padding(5)
					.valueColor(new Color(138, 138, 138)).valueFont(plain18).build());
		}

		// Draw top modes
		ArrayList<Mode> topModes = this.getTopModes(bedwars);
		this.drawMode(g, topModes.get(0), bedwars, 635);
		this.drawMode(g, topModes.get(1), bedwars, 1068);
	}

	private void generateTiny(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		// Safely get Bedwars stats, defaulting to empty object if not found
		JsonObject bedwars = stats.has("stats") && !stats.get("stats").isJsonNull()
				? stats.getAsJsonObject("stats").has("Bedwars")
						&& !stats.getAsJsonObject("stats").get("Bedwars").isJsonNull()
								? stats.getAsJsonObject("stats").getAsJsonObject("Bedwars")
								: new JsonObject()
				: new JsonObject();
		BedwarsStats bwStats = extractStats(bedwars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw stars
		this.drawStar(g, bedwars, 738, 44, 30);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw final K/D ratio
		g.drawString(String.valueOf(bwStats.fkdr),
				615 - (g.getFontMetrics().stringWidth(String.valueOf(bwStats.fkdr)) / 2), 118);
		this.drawProgress(g, 534, 133, 177, bwStats.finalKills / (double) (bwStats.finalKills + bwStats.finalDeaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(bwStats.wlr),
				860 - (g.getFontMetrics().stringWidth(String.valueOf(bwStats.wlr)) / 2), 118);
		this.drawProgress(g, 776, 133, 177, bwStats.wins / (double) (bwStats.wins + bwStats.losses));

		// Draw wins and winstreak
		drawStat(g, DrawStatOptions.builder().label("Wins").value(String.format("%,d", bwStats.wins)).x(1015).y(100)
				.build());
		drawStat(g, DrawStatOptions.builder().label("Final Kills").value(String.format("%,d", bwStats.finalKills))
				.x(1015).y(125).build());

		// Winstreaks might be disabled on the API
		if (bwStats.hasWinstreak) {
			drawStat(g, DrawStatOptions.builder().label("Winstreak").value(String.format("%,d", bwStats.winstreak))
					.x(1015).y(165).build());
		} else {
			drawStat(g, DrawStatOptions.builder().label("Winstreak").value("Unknown").x(1015).y(165).padding(5)
					.valueColor(new Color(138, 138, 138)).valueFont(plain18).build());
		}
	}

	private void generateForums(BufferedImage image, JsonObject stats) {

		Graphics2D g = (Graphics2D) image.getGraphics();

		JsonObject bedwars = stats.has("stats") && !stats.get("stats").isJsonNull()
				? stats.getAsJsonObject("stats").has("Bedwars")
						&& !stats.getAsJsonObject("stats").get("Bedwars").isJsonNull()
								? stats.getAsJsonObject("stats").getAsJsonObject("Bedwars")
								: new JsonObject()
				: new JsonObject();
		BedwarsStats bwStats = extractStats(bedwars);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		this.drawStar(g, bedwars, 700, CardGame.CardSize.ForumsConstants.TITLE_POSITION_Y, 30);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 34));

		g.drawString(String.valueOf(bwStats.fkdr),
				CardGame.CardSize.ForumsConstants.FIRST_RATIO_POSITION_X
						- (g.getFontMetrics().stringWidth(String.valueOf(bwStats.fkdr)) / 2),
				CardGame.CardSize.ForumsConstants.RATIO_POSITION_Y);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.FIRST_PROGRESS_POSITION_X,
				CardGame.CardSize.ForumsConstants.PROGRESS_POSITION_Y, CardGame.CardSize.ForumsConstants.PROGRESS_WIDTH,
				CardGame.CardSize.ForumsConstants.PROGRESS_HEIGHT,
				bwStats.finalKills / (double) (bwStats.finalKills + bwStats.finalDeaths));

		g.drawString(String.valueOf(bwStats.wlr),
				CardGame.CardSize.ForumsConstants.SECOND_RATIO_POSITION_X
						- (g.getFontMetrics().stringWidth(String.valueOf(bwStats.wlr)) / 2),
				CardGame.CardSize.ForumsConstants.RATIO_POSITION_Y);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.SECOND_PROGRESS_POSITION_X,
				CardGame.CardSize.ForumsConstants.PROGRESS_POSITION_Y, CardGame.CardSize.ForumsConstants.PROGRESS_WIDTH,
				CardGame.CardSize.ForumsConstants.PROGRESS_HEIGHT,
				bwStats.wins / (double) (bwStats.wins + bwStats.losses));

		drawStat(g, DrawStatOptions.builder().label("Final Kills").value(String.format("%,d", bwStats.finalKills))
				.x(CardGame.CardSize.ForumsConstants.MAJOR_STAT_1_X).y(CardGame.CardSize.ForumsConstants.MAJOR_STAT_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).centered(true)
				.fontSize(20)
				.build());
		drawStat(g, DrawStatOptions.builder().label("Wins").value(String.format("%,d", bwStats.wins))
				.x(CardGame.CardSize.ForumsConstants.MAJOR_STAT_2_X).y(CardGame.CardSize.ForumsConstants.MAJOR_STAT_Y)
				.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).centered(true)
				.fontSize(20).build());

		// Winstreaks might be disabled on the API
		if (bwStats.hasWinstreak) {
			drawStat(g,
					DrawStatOptions.builder().label("Winstreak").value(String.format("%,d", bwStats.winstreak))
							.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_X)
							.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_Y)
							.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());
		} else {
			drawStat(g,
					DrawStatOptions.builder().label("Winstreak").value("Unknown")
							.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_X)
							.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_Y)
							.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET)
							.valueColor(new Color(138, 138, 138)).build());
		}

		// other bottom box stats
		drawStat(g,
				DrawStatOptions.builder().label("Beds Broken").value(String.format("%,d", bwStats.bedsBroken))
						.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_X)
						.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_Y)
						.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());
		drawStat(g,
				DrawStatOptions.builder().label("Slumber Tickets").value(String.format("%,d", bwStats.slumberTickets))
						.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_1_X)
						.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_Y)
						.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());
		drawStat(g,
				DrawStatOptions.builder().label("Challenges").value(String.format("%,d", bwStats.challengesCompleted))
						.x(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_X)
						.y(CardGame.CardSize.ForumsConstants.BOTTOM_BOX_QUAD_2_Y)
						.padding(CardGame.CardSize.ForumsConstants.STATS_VALUE_OFFSET).build());

		// Draw top modes
		ArrayList<Mode> topModes = this.getTopModes(bedwars);
		this.drawSideBox(g, topModes.get(0), bedwars, 0);
		this.drawSideBox(g, topModes.get(1), bedwars, 1);

	}

	private void drawStar(Graphics2D g, JsonObject bedwarsStats, int x, int y, int size) {
		if (!bedwarsStats.has("Experience")) {
			return;
		}

		int star = (int) getBedWarsLevel(bedwarsStats.get("Experience").getAsInt());
		Prestige prestige = Prestige.get(star);

		if (prestige == null) {
			return;
		}

		MinecraftRenderer.drawMinecraftString(g, prestige.format(star), x, y, size);
	}

	private void drawSideBox(Graphics2D g, @NonNull Mode mode, @NonNull JsonObject bedwarsStats, int boxNumber) {
		int finalKills = JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_final_kills_bedwars", 0);
		int finalDeaths = Math.max(1, JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_final_deaths_bedwars", 1));
		int wins = JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_wins_bedwars", 0);
		int losses = Math.max(1, JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_losses_bedwars", 1));

	
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Medium", Font.BOLD, 20));

		int nameWidth = g.getFontMetrics().stringWidth(mode.getDisplayName().toUpperCase());
		g.drawString(mode.getDisplayName().toUpperCase(Locale.ROOT), CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_X,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_Y
						+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y);

		g.setColor(this.getColor());
		g.fillRect(CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_X + nameWidth + 15,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_TITLE_Y
						+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y - 10,
				450 - nameWidth - 15, 2);
		g.setColor(Color.WHITE);

		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		String fkdr = (Math.round((finalKills / (double) finalDeaths) * 100) / 100d) + "";
		this.drawCenterAlignedString(g, fkdr, CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_1_X,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_LABEL_Y
						+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
				false);
		this.drawProgress(g, CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_1_X,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_Y
						+ boxNumber * CardGame.CardSize.ForumsConstants.SIDE_BOX_SECOND_BOX_DIFFERENCE_Y,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_WIDTH,
				CardGame.CardSize.ForumsConstants.SIDE_BOX_PROGRESS_HEIGHT,
				finalKills / (double) (finalKills + finalDeaths));

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

		this.drawStat(g,
				DrawStatOptions.builder().label("Final Kills").value(String.format("%,d", finalKills))
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

	private void drawMode(Graphics2D g, @NonNull Mode mode, @NonNull JsonObject bedwarsStats, int baseX) {

		int finalKills = JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_final_kills_bedwars", 0);
		int finalDeaths = Math.max(1, JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_final_deaths_bedwars", 1));
		int wins = JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_wins_bedwars", 0);
		int losses = Math.max(1, JsonUtil.getInt(bedwarsStats, mode.getApiName() + "_losses_bedwars", 1));

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

		// Draw final K/D ratio
		String fkdr = (Math.round((finalKills / (double) finalDeaths) * 100) / 100d) + "";
		g.drawString(fkdr, baseX + 78 - (g.getFontMetrics().stringWidth(fkdr) / 2), 340);
		this.drawProgress(g, baseX + 6, 354, 146, finalKills / (double) (finalKills + finalDeaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, baseX + 261 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, baseX + 189, 354, 146, wins / (double) (wins + losses));

		// Draw final kills
		drawStat(g, DrawStatOptions.builder()
				.label("Final Kills")
				.value(String.format("%,d", finalKills))
				.x(baseX + 80)
				.y(425)
				.centered(true)
				.build());

		// Draw wins
		drawStat(g, DrawStatOptions.builder()
				.label("Wins")
				.value(String.format("%,d", wins))
				.x(baseX + 263)
				.y(425)
				.centered(true)
				.build());
	}

	private ArrayList<Mode> getTopModes(JsonObject bedwarsStats) {
		ArrayList<Mode> modes = new ArrayList<>();

		Mode first = Mode.SOLO, second = Mode.DOUBLES;
		int firstWins = -1, secondWins = -1;

		for (Mode mode : Mode.values()) {
			if (bedwarsStats.has(mode.getApiName() + "_wins_bedwars")) {
				int wins = bedwarsStats.get(mode.getApiName() + "_wins_bedwars").getAsInt();

				if (wins > firstWins) {

					// Bump first down to second
					second = first;
					secondWins = firstWins;

					first = mode;
					firstWins = wins;
				} else if (wins > secondWins) {
					second = mode;
					secondWins = wins;
				}
			}
		}

		modes.add(first);
		modes.add(second);
		return modes;
	}

	public static double getBedWarsLevel(double exp) {
		int level = 100 * ((int) (exp / 487000));
		exp = exp % 487000;
		if (exp < 500)
			return level + exp / 500;
		level++;
		if (exp < 1500)
			return level + (exp - 500) / 1000;
		level++;
		if (exp < 3500)
			return level + (exp - 1500) / 2000;
		level++;
		if (exp < 7000)
			return level + (exp - 3500) / 3500;
		level++;
		exp -= 7000;
		return level + exp / 5000;
	}

	@Getter
	@AllArgsConstructor
	private enum Mode {
		SOLO("eight_one", "Solos"),
		DOUBLES("eight_two", "Doubles"),
		THREES("four_three", "Threes"),
		FOURS("four_four", "Fours"),
		FOUR_V_FOUR("two_four", "4v4");

		private final String apiName;
		private final String displayName;
	}

	@Getter
	@AllArgsConstructor
	public enum Prestige {

		// 0 - 900
		STONE(0, star -> "§7[" + star + "✫]"),
		IRON(100, star -> "§f[" + star + "✫]"),
		GOLD(200, star -> "§6[" + star + "✫]"),
		DIAMOND(300, star -> "§b[" + star + "✫]"),
		EMERALD(400, star -> "§2[" + star + "✫]"),
		SAPPHIRE(500, star -> "§3[" + star + "✫]"),
		RUBY(600, star -> "§c[" + star + "✫]"),
		CRYSTAL(700, star -> "§d[" + star + "✫]"),
		OPAL(800, star -> "§9[" + star + "✫]"),
		AMETHYST(900, star -> "§5[" + star + "✫]"),

		// 1000 - 1900
		RAINBOW(1000, star -> String.format("§c[§6%s§e%s§a%s§b%s§d✫§5]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		IRON_PRIME(1100, star -> String.format("§7[§f%s%s%s%s§7✪]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		GOLD_PRIME(1200, star -> String.format("§7[§e%s%s%s%s§6✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		DIAMOND_PRIME(1300, star -> String.format("§7[§b%s%s%s%s§5✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		EMERALD_PRIME(1400, star -> String.format("§7[§a%s%s%s%s§2✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		SAPPHIRE_PRIME(1500, star -> String.format("§7[§5%s%s%s%s§9✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		RUBY_PRIME(1600, star -> String.format("§7[§c%s%s%s%s§4✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		CRYSTAL_PRIME(1700, star -> String.format("§7[§d%s%s%s%s§5✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		OPAL_PRIME(1800, star -> String.format("§7[§9%s%s%s%s§1✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		AMETHYST_PRIME(1900, star -> String.format("§7[§5%s%s%s%s§8✪§7]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),

		// 2000 - 2900
		MIRROR(2000, star -> String.format("§8[§7%s§f%s%s§7%s✪§8]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		LIGHT(2100, star -> String.format("§f[%s§e%s%s§6%s⚝]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		DAWN(2200, star -> String.format("§6[%s§f%s%s§b%s§5⚝]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		DUSK(2300, star -> String.format("§5[%s§d%s%s§6%s§f⚝]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		AIR(2400, star -> String.format("§b[%s§f%s%s§7%s⚝§8]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		WIND(2500, star -> String.format("§f[%s§a%s%s§2%s⚝]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		NEBULA(2600, star -> String.format("§4[%s§c%s%s§d%s⚝§5]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		THUNDER(2700, star -> String.format("§e[%s§f%s%s§8%s⚝]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		EARTH(2800, star -> String.format("§a[%s§2%s%s§6%s⚝§e]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		WATER(2900, star -> String.format("§b[%s§5%s%s§9%s⚝§1]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),

		// 3000 - 3900
		FIRE(3000, star -> String.format("§f[%s§6%s%s§c%s⚝§4]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		SUNRISE(3100, star -> String.format("§9[%s§5%s%s§6%s✥§e]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ECLIPSE(3200, star -> String.format("§c[§4%s§7%s%s§4%s§c✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		GAMMA(3300, star -> String.format("§9[%s%s§d%s§c%s✥§4]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		MAJESTIC(3400, star -> String.format("§2[§a%s§d%s%s§5%s✥§2]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ANDESINE(3500, star -> String.format("§c[%s§4%s%s§2%s§a✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		MARINE(3600, star -> String.format("§a[%s%s§b%s§9%s✥§1]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ELEMENT(3700, star -> String.format("§4[%s§c%s%s§b%s§3✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		GALAXY(3800, star -> String.format("§1[%s§b%s§5%s%s§d✥§1]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ATOMIC(3900, star -> String.format("§c[%s§a%s%s§3%s§9✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),

		// 4000 - 5000
		SUNSET(4000, star -> String.format("§5[%s§c%s%s§6%s✥§e]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		TIME(4100, star -> String.format("§e[%s§6%s§c%s§d%s✥§5]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		WINTER(4200, star -> String.format("§1[§9%s§3%s§b%s§f%s§7✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		OBSIDIAN(4300, star -> String.format("§0[§5%s§8%s%s§5%s✥§0]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		SPRING(4400, star -> String.format("§2[%s§a%s§e%s§6%s§5✥§d]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ICE(4500, star -> String.format("§f[%s§b%s%s§3%s✥]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		SUMMER(4600, star -> String.format("§3[§b%s§e%s%s§6%s§d✥§5]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		SPINEL(4700, star -> String.format("§f[§4%s§c%s%s§9%s§1✥§9]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		AUTUMN(4800, star -> String.format("§5[%s§c%s§6%s§f%s§b✥§5]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		MYSTIC(4900, star -> String.format("§2[§a%s§f%s%s§a%s✥§2]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3))),
		ETERNAL(5000, star -> String.format("§c[%s§5%s§9%s%s§1✥§0]",
				star.charAt(0), star.charAt(1), star.charAt(2), star.charAt(3)));

		private final int requirement;
		private final Function<String, String> format;

		public String format(int star) {
			return this.format.apply(Integer.toString(star));
		}

		/**
		 * Get the prestige a given star belongs to by iterating over prestiges until
		 * the requirement is not met
		 * 
		 * @param star The star count to analyze
		 * @return The prestige that the given star count belongs to
		 */
		public static Prestige get(int star) {
			Prestige currentPrestige = STONE;

			for (Prestige prestige : Prestige.values()) {
				if (prestige.getRequirement() > star) {
					return currentPrestige;
				} else {
					currentPrestige = prestige;
				}
			}

			return currentPrestige;
		}
	}
}
