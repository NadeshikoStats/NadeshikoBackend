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

public class BedwarsCardProvider extends CardProvider {

	public BedwarsCardProvider() {
		super(CardGame.BEDWARS);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject bedwars = stats.getAsJsonObject("Bedwars");

		int finalKills = bedwars.get("final_kills_bedwars").getAsInt();
		int finalDeaths = bedwars.get("final_deaths_bedwars").getAsInt();
		int wins = bedwars.get("wins_bedwars").getAsInt();
		int losses = bedwars.get("losses_bedwars").getAsInt();

		int winstreak = 0;

		// Winstreaks can be disabled from the API
		if (bedwars.has("winstreak")) {
			winstreak = bedwars.get("winstreak").getAsInt();
		}

		// Draw stars
		this.drawStar(g, bedwars);

		// Set up the stat font
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw final K/D ratio
		String fkdr = (Math.round((finalKills / (double) finalDeaths) * 100) / 100d) + "";
		g.drawString(fkdr, 750 - (g.getFontMetrics().stringWidth(fkdr) / 2), 158);
		this.drawProgress(g, 664, 173, 177, finalKills / (double) (finalKills + finalDeaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, 1007 - (g.getFontMetrics().stringWidth(wlr) / 2), 158);
		this.drawProgress(g, 921, 173, 177, wins / (double) (wins + losses));

		// Draw wins and winstreak
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int finalsWidth = g.getFontMetrics().stringWidth("Final Kills");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");

		g.drawString("Wins", 1175, 140);
		g.drawString("Final Kills", 1175, 170);
		g.drawString("Winstreak", 1175, 208);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", wins), 1175 + winsWidth + 10, 140);
		g.drawString(String.format("%,d", finalKills), 1175 + finalsWidth + 10, 170);

		// Winstreaks might be disabled on the API
		if (bedwars.has("winstreak")) {
			g.drawString(String.format("%,d", winstreak), 1175 + winstreakWidth + 10, 208);
		} else {
			g.setColor(new Color(138, 138, 138));
			g.setFont(smallLight);
			g.drawString("Unknown", 1175 + winstreakWidth + 5, 208);
		}

		// Draw top modes
		ArrayList<Mode> topModes = this.getTopModes(bedwars);
		this.drawMode(g, topModes.get(0), bedwars, 635);
		this.drawMode(g, topModes.get(1), bedwars, 1068);
	}

	private void drawStar(Graphics g, @NonNull JsonObject bedwarsStats) {
		if (!bedwarsStats.has("Experience")) {
			return;
		}

		int star = (int) getBedWarsLevel(bedwarsStats.get("Experience").getAsInt());
		Prestige prestige = Prestige.get(star);

		if (prestige == null) {
			return;
		}

		MinecraftRenderer.drawString(g, prestige.format(star), 900, 67, 30);
	}

	private void drawMode(Graphics g, @NonNull Mode mode, @NonNull JsonObject bedwarsStats, int baseX) {

		int finalKills = 0, finalDeaths = 1, wins = 0, losses = 1;

		if (bedwarsStats.has(mode.getApiName() + "_final_kills_bedwars")) {
			finalKills = bedwarsStats.get(mode.getApiName() + "_final_kills_bedwars").getAsInt();
		}

		if (bedwarsStats.has(mode.getApiName() + "_final_deaths_bedwars")) {
			finalDeaths = bedwarsStats.get(mode.getApiName() + "_final_deaths_bedwars").getAsInt();
		}

		if (bedwarsStats.has(mode.getApiName() + "_wins_bedwars")) {
			wins = bedwarsStats.get(mode.getApiName() + "_wins_bedwars").getAsInt();
		}

		if (bedwarsStats.has(mode.getApiName() + "_losses_bedwars")) {
			losses = bedwarsStats.get(mode.getApiName() + "_losses_bedwars").getAsInt();
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

		// Draw final K/D ratio
		String fkdr = (Math.round((finalKills / (double) finalDeaths) * 100) / 100d) + "";
		g.drawString(fkdr, baseX + 78 - (g.getFontMetrics().stringWidth(fkdr) / 2), 340);
		this.drawProgress(g, baseX + 6, 354, 146, finalKills / (double) (finalKills + finalDeaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, baseX + 261 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, baseX + 189, 354, 146, wins / (double) (wins + losses));

		// Draw final kills
		int finalKillsWidth = g.getFontMetrics(smallLight).stringWidth("Final Kills  ");
		int finalKillsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", finalKills));
		int finalKillsTotalWidth = finalKillsWidth + finalKillsCountWidth;
		int finalKillsLeftX = baseX + 80 - (finalKillsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Final Kills", finalKillsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", finalKills), finalKillsLeftX + finalKillsWidth, 425);

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

	private static double getBedWarsLevel(double exp) {
		int level = 100 * ((int)(exp / 487000));
		exp = exp % 487000;
		if(exp < 500) return level + exp / 500;
		level++;
		if(exp < 1500) return level + (exp - 500) / 1000;
		level++;
		if(exp < 3500) return level + (exp - 1500) / 2000;
		level++;
		if(exp < 7000) return level + (exp - 3500) / 3500;
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
	private enum Prestige {
		STONE(0, star -> "§7[" + star + "✫]"),
		IRON(100, star -> "§f[" + star + "✫]"),
		GOLD(200, star -> "§6[" + star + "✫]"),
		DIAMOND(300, star -> "§b[" + star + "✫]");

		private final int requirement;
		private final Function<String, String> format;

		public String format(int star) {
			return this.format.apply(Integer.toString(star));
		}

		public static Prestige get(int star) {
			Prestige currentPrestige = STONE;

			for (Prestige prestige : Prestige.values()) {
				if (prestige.getRequirement() > star) {
					return currentPrestige;
				} else {
					currentPrestige = prestige;
				}
			}

			return null;
		}
	}
}
