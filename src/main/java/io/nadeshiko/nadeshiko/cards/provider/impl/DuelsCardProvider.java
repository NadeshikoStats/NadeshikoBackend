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
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.CardGenerator;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.util.ImageUtil;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;
import io.nadeshiko.nadeshiko.util.RomanNumerals;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DuelsCardProvider extends CardProvider {

	private record DuelsStats(
		int kills,
		int deaths,
		int wins,
		int losses,
		int winstreak,
		int bestWinstreak,
		double kdr,
		double wlr,
		boolean hasWinstreak,
		String activeTitle
	) {}

	private record ModeStats(
		int kills,
		int deaths,
		int wins,
		int losses,
		double kdr,
		double wlr
	) {}

	private final HashMap<Duels, BufferedImage> iconMap = new HashMap<>();

	public DuelsCardProvider() {
		super(CardGame.DUELS);

		// Read the duel icons from resources into the cache
		for (Duels duel : Duels.values()) {
			if (duel.getTextureName() == null) {
				continue; // If the duel has no texture, skip it
			}

			try (InputStream iconStream =  CardGenerator.class.
				getResourceAsStream("/cards/duels/" + duel.getTextureName() + ".png")) {

				if (iconStream == null) {
					Nadeshiko.INSTANCE.alert("Missing duel icon for %s!", duel.name());
					continue;
				}

				byte[] iconBytes = iconStream.readAllBytes();
				BufferedImage icon = ImageUtil.createImageFromBytes(iconBytes);

				this.iconMap.put(duel, icon);
			} catch (IOException e) {
				Nadeshiko.INSTANCE.alert("Failed reading duel icon for %s!", duel.name());
			}
		}
	}

	private DuelsStats extractStats(JsonObject duels) {
		// Default values in case stats are missing
		int kills = 0;
		int deaths = 1; // Avoid div0
		int wins = 0;
		int losses = 1; // Avoid div0
		int winstreak = 0;
		int bestWinstreak = 0;
		boolean hasWinstreak = false;
		String activeTitle = "";

		// Safely extract stats if they exist
		if (duels != null) {
			try {
				if (duels.has("kills") && !duels.get("kills").isJsonNull()) {
					kills = duels.get("kills").getAsInt();
				}
				if (duels.has("deaths") && !duels.get("deaths").isJsonNull()) {
					deaths = Math.max(1, duels.get("deaths").getAsInt()); // Fixes divzero problem
				}
				if (duels.has("wins") && !duels.get("wins").isJsonNull()) {
					wins = duels.get("wins").getAsInt();
				}
				if (duels.has("losses") && !duels.get("losses").isJsonNull()) {
					losses = Math.max(1, duels.get("losses").getAsInt()); // Fixes divzero problem
				}
				if (duels.has("current_winstreak") && !duels.get("current_winstreak").isJsonNull() &&
					duels.has("best_overall_winstreak") && !duels.get("best_overall_winstreak").isJsonNull()) {
					hasWinstreak = true;
					winstreak = duels.get("current_winstreak").getAsInt();
					bestWinstreak = duels.get("best_overall_winstreak").getAsInt();
				}
				if (duels.has("active_cosmetictitle") && !duels.get("active_cosmetictitle").isJsonNull()) {
					activeTitle = duels.get("active_cosmetictitle").getAsString();
				}
			} catch (Exception e) {
				// If any parsing fails, we'll use the default values
				Nadeshiko.INSTANCE.alert("Failed to parse duels stats: %s", e.getMessage());
			}
		}

		double kdr = Math.round((kills / (double) deaths) * 100) / 100d;
		double wlr = Math.round((wins / (double) losses) * 100) / 100d;

		return new DuelsStats(kills, deaths, wins, losses, winstreak, bestWinstreak, kdr, wlr, hasWinstreak, activeTitle);
	}

	private ModeStats extractModeStats(JsonObject duelsStats, Duels mode) {
		int kills = 0;
		int deaths = 1;
		int wins = 0;
		int losses = 1;

		try {
			if (duelsStats.has(mode.getApiName() + "_kills") && !duelsStats.get(mode.getApiName() + "_kills").isJsonNull()) {
				kills = duelsStats.get(mode.getApiName() + "_kills").getAsInt();
			} else if (mode.equals(Duels.BRIDGE_SOLO) && duelsStats.has("bridge_kills") && !duelsStats.get("bridge_kills").isJsonNull()) {
				// Inconsistent API naming breaks with bridge duels...
				kills = duelsStats.get("bridge_kills").getAsInt();
			}

			if (duelsStats.has(mode.getApiName() + "_deaths") && !duelsStats.get(mode.getApiName() + "_deaths").isJsonNull()) {
				deaths = Math.max(1, duelsStats.get(mode.getApiName() + "_deaths").getAsInt());
			} else if (mode.equals(Duels.BRIDGE_SOLO) && duelsStats.has("bridge_deaths") && !duelsStats.get("bridge_deaths").isJsonNull()) {
				// Inconsistent API naming breaks with bridge duels...
				deaths = Math.max(1, duelsStats.get("bridge_deaths").getAsInt());
			}

			if (duelsStats.has(mode.getApiName() + "_wins") && !duelsStats.get(mode.getApiName() + "_wins").isJsonNull()) {
				wins = duelsStats.get(mode.getApiName() + "_wins").getAsInt();
			}

			if (duelsStats.has(mode.getApiName() + "_losses") && !duelsStats.get(mode.getApiName() + "_losses").isJsonNull()) {
				losses = Math.max(1, duelsStats.get(mode.getApiName() + "_losses").getAsInt());
			}
		} catch (Exception e) {
			// If any parsing fails, we'll use the default values
			Nadeshiko.INSTANCE.alert("Failed to parse mode stats for %s: %s", mode.getDisplayName(), e.getMessage());
		}

		double kdr = Math.round((kills / (double) deaths) * 100) / 100d;
		double wlr = Math.round((wins / (double) losses) * 100) / 100d;

		return new ModeStats(kills, deaths, wins, losses, kdr, wlr);
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
		JsonObject duels = stats.getAsJsonObject("stats").getAsJsonObject("Duels");
		DuelsStats duelsStats = extractStats(duels);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw title
		this.drawTitle(g, duels, false);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw K/D ratio
		g.drawString(String.valueOf(duelsStats.kdr), 750 - (g.getFontMetrics().stringWidth(String.valueOf(duelsStats.kdr)) / 2), 158);
		this.drawProgress(g, 664, 173, 177, duelsStats.kills / (double) (duelsStats.kills + duelsStats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(duelsStats.wlr), 1007 - (g.getFontMetrics().stringWidth(String.valueOf(duelsStats.wlr)) / 2), 158);
		this.drawProgress(g, 921, 173, 177, duelsStats.wins / (double) (duelsStats.wins + duelsStats.losses));

		// Draw wins and winstreak
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");
		int bestWinstreakWidth = g.getFontMetrics().stringWidth("Best Winstreak");

		g.drawString("Wins", 1175, 140);
		g.drawString("Winstreak", 1175, 178);
		g.drawString("Best Winstreak", 1175, 208);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", duelsStats.wins), 1175 + winsWidth + 10, 140);

		// Winstreaks might be disabled on the API
		if (duelsStats.hasWinstreak) {
			g.drawString(String.format("%,d", duelsStats.winstreak), 1175 + winstreakWidth + 10, 178);
			g.drawString(String.format("%,d", duelsStats.bestWinstreak), 1175 + bestWinstreakWidth + 10, 208);
		} else {
			g.setColor(new Color(138, 138, 138));
			g.setFont(smallLight);
			g.drawString("Unknown", 1175 + winstreakWidth + 5, 178);
			g.drawString("Unknown", 1175 + bestWinstreakWidth + 5, 208);
		}

		// Draw top duels
		ArrayList<Duels> topDuels = this.getTopDuels(duels);
		this.drawDuel(g, topDuels.get(0), duels, 635);
		this.drawDuel(g, topDuels.get(1), duels, 1068);
	}

	private void generateTiny(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject duels = stats.getAsJsonObject("stats").getAsJsonObject("Duels");
		DuelsStats duelsStats = extractStats(duels);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw title
		this.drawTitle(g, duels, true);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw K/D ratio
		g.drawString(String.valueOf(duelsStats.kdr), 615 - (g.getFontMetrics().stringWidth(String.valueOf(duelsStats.kdr)) / 2), 118);
		this.drawProgress(g, 534, 133, 177, duelsStats.kills / (double) (duelsStats.kills + duelsStats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(duelsStats.wlr), 860 - (g.getFontMetrics().stringWidth(String.valueOf(duelsStats.wlr)) / 2), 118);
		this.drawProgress(g, 776, 133, 177, duelsStats.wins / (double) (duelsStats.wins + duelsStats.losses));

		// Draw wins and winstreak
		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);

		int winsWidth = g.getFontMetrics().stringWidth("Wins");
		int winstreakWidth = g.getFontMetrics().stringWidth("Winstreak");
		int bestWinstreakWidth = g.getFontMetrics().stringWidth("Best Winstreak");

		g.drawString("Wins", 1015, 100);
		g.drawString("Winstreak", 1015, 125);
		g.drawString("Best Winstreak", 1015, 165);

		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", duelsStats.wins), 1015 + winsWidth + 10, 100);

		// Winstreaks might be disabled on the API
		if (duelsStats.hasWinstreak) {
			g.drawString(String.format("%,d", duelsStats.winstreak), 1015 + winstreakWidth + 10, 125);
			g.drawString(String.format("%,d", duelsStats.bestWinstreak), 1015 + bestWinstreakWidth + 10, 165);
		} else {
			g.setColor(new Color(138, 138, 138));
			g.setFont(smallLight);
			g.drawString("Unknown", 1015 + winstreakWidth + 5, 125);
			g.drawString("Unknown", 1015 + bestWinstreakWidth + 5, 165);
		}
	}

	private void drawTitle(Graphics g, @NonNull JsonObject duelsStats, boolean isTiny) {
		// Ensure the player actually has a title
		if (!duelsStats.has("active_cosmetictitle")) {
			return;
		}

		String activeTitle = duelsStats.get("active_cosmetictitle").getAsString();
		String finalTitle = "";

		Title title = Title.get(activeTitle);
		Duels duel = Duels.getFromTitle(activeTitle);

		if (title == null || duel == null) {
			return; // The player doesn't have a wins-based title
		}

		// Draw the hyphen after "Duels Stats"
		g.setColor(Color.WHITE);
		if (isTiny) {
			g.fillRect(654, 33, 16, 3);
		} else {
			g.fillRect(808, 56, 16, 4);
		}

		finalTitle += title.getColor();
		finalTitle += duel.getDisplayName();

		// If the title is overall, there is no game name and the space shouldn't be there
		if (!duel.getDisplayName().isEmpty()) {
			finalTitle += " ";
		}

		finalTitle += title.getName() + " ";

		int level = duelsStats.get(duel.getTitleName() + "_" +
			title.getName().toLowerCase() + "_title_prestige").getAsInt();

		// Hypixel doesn't draw the number if it's only 1. Sumo Legend I is displayed as Sumo Legend
		if (level > 1) {
			finalTitle += RomanNumerals.arabicToRoman(level);
		}

		// Draw at different positions based on card size
		if (isTiny) {
			MinecraftRenderer.drawMinecraftString(g, finalTitle, 683, 44, 30);
		} else {
			MinecraftRenderer.drawMinecraftString(g, finalTitle, 845, 67, 30);
		}
	}

	private void drawDuel(Graphics2D g, @NonNull Duels duel, @NonNull JsonObject duelsStats, int baseX) {
		ModeStats stats = extractModeStats(duelsStats, duel);

		// Set up the name font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Medium", Font.BOLD, 22));

		// Draw duel name
		int nameWidth = g.getFontMetrics().stringWidth(duel.getDisplayName().toUpperCase());
		g.drawString(duel.getDisplayName().toUpperCase(Locale.ROOT), baseX, 290);

		// Draw duel icon
		g.drawImage(this.iconMap.get(duel), baseX + nameWidth + 15, 265, null);

		// Draw the line beside the duel name
		g.setColor(this.getColor());
		g.fillRect(baseX + nameWidth + 60, 280, 350 - nameWidth - 60, 2);
		g.setColor(Color.WHITE);

		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw K/D ratio
		g.drawString(String.valueOf(stats.kdr), baseX + 78 - (g.getFontMetrics().stringWidth(String.valueOf(stats.kdr)) / 2), 340);
		this.drawProgress(g, baseX + 6, 354, 146, stats.kills / (double) (stats.kills + stats.deaths));

		// Draw W/L ratio
		g.drawString(String.valueOf(stats.wlr), baseX + 261 - (g.getFontMetrics().stringWidth(String.valueOf(stats.wlr)) / 2), 340);
		this.drawProgress(g, baseX + 189, 354, 146, stats.wins / (double) (stats.wins + stats.losses));

		// Draw kills
		int killsWidth = g.getFontMetrics(smallLight).stringWidth("Kills  ");
		int killsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", stats.kills));
		int killsTotalWidth = killsWidth + killsCountWidth;
		int killsLeftX = baseX + 80 - (killsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Kills", killsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", stats.kills), killsLeftX + killsWidth, 425);

		// Draw wins
		int winsWidth = g.getFontMetrics(smallLight).stringWidth("Wins  ");
		int winsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", stats.wins));
		int winsTotalWidth = winsWidth + winsCountWidth;
		int winsLeftX = baseX + 263 - (winsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Wins", winsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", stats.wins), winsLeftX + winsWidth, 425);
	}

	private ArrayList<Duels> getTopDuels(JsonObject duelsStats) {
		ArrayList<Duels> duels = new ArrayList<>();

		Duels first = Duels.BRIDGE_SOLO, second = Duels.CLASSIC;
		int firstWins = -1, secondWins = -1;

		for (Duels duel : Duels.values()) {
			if (duelsStats.has(duel.getApiName() + "_wins")) {
				int wins = duelsStats.get(duel.getApiName() + "_wins").getAsInt();

				if (wins > firstWins) {

					// Bump first down to second
					second = first;
					secondWins = firstWins;

					first = duel;
					firstWins = wins;
				} else if (wins > secondWins) {
					second = duel;
					secondWins = wins;
				}
			}
		}

		duels.add(first);
		duels.add(second);
		return duels;
	}

	@Getter
	@AllArgsConstructor
	private enum Duels {
		ARENA("duel_arena", "Arena", "ARENA", null),
		BLITZ("blitz_duel", "Blitz", "BLITZ", "blitz"),
		BOW("bow_duel", "Bow", "BOW", "bow"),
		BOWSPLEEF("bowspleef_duel", "Bow Spleef", "BOWSPLEEF", "bowspleef"),
		BOXING("boxing_duel", "Boxing", "BOXING", "boxing"),
		BRIDGE_SOLO("bridge_duel", "Bridge Solo", "BRIDGE", null),
		// TODO bridge 2s, very malformed api names
		// TODO bridge 3v3, 4v4, 2v2v2v2, 3v3v3v3, CTF 3s
		CLASSIC("classic_duel", "Classic", "CLASSIC", "classic"),
		COMBO("combo_duel", "Combo", "COMBO", "combo"),
		MEGAWALLS_SOLO("mw_duel", "Mega Walls Solo", "MEGAWALLS", null),
		MEGAWALLS_DOUBLES("mw_doubles", "Mega Walls 2s", "MEGAWALLS", null),
		NODEBUFF("potion_duel", "Nodebuff", "NODEBUFF", "nodebuff"),
		OP_SOLO("op_duel", "OP Solo", "OP", null),
		OP_DOUBLES("op_doubles", "OP 2s", "OP", null),
		PARKOUR("parkour_eight", "Parkour", "PARKOUR", "parkour"),
		SKYWARS_SOLO("sw_duel", "SkyWars Solo", "SKYWARS", null),
		SKYWARS_DOUBLES("sw_doubles", "SkyWars 2s", "SKYWARS", null),
		SUMO("sumo_duel", "Sumo", "SUMO", "sumo"),
		UHC_SOLO("uhc_duel", "UHC Solo", "UHC", null),
		UHC_DOUBLES("uhc_doubles", "UHC 2s", "UHC", null),
		UHC_FOURS("uhc_four", "UHC 4s", "UHC", null),
		UHC_EIGHTS("uhc_meetup", "UHC Deathmatch", "UHC", null),

		// Special ones for titles
		ALL(null, "", null, "all_modes"),
		BRIDGE_OVERALL(null, "Bridge", "BRIDGE", "bridge"),
		MEGAWALLS_OVERALL(null, "Mega Walls", "MEGAWALLS", "mega_walls"),
		SKYWARS_OVERALL(null, "SkyWars", "SKYWARS", "skywars"),
		OP_OVERALL(null, "OP", "OP", "op"),
		UHC_OVERALL(null, "UHC", "UHC", "uhc");

		private final String apiName;
		private final String displayName;
		private final String textureName;
		private final String titleName;

		public static Duels getFromTitle(String fullTitle) {
			String titleString = fullTitle.split("_")[0];
			String duelString = fullTitle.substring(titleString.length() + 1);

			for (Duels duel : Duels.values()) {
				if (duel.getTitleName() == null) {
					continue;
				}

				if (duel.getTitleName().equalsIgnoreCase(duelString)) {
					return duel;
				}
			}

			return null;
		}
	}

	@Getter
	@AllArgsConstructor
	private enum Title {
		ROOKIE("Rookie", "§8"),
		IRON("Iron", "§f"),
		GOLD("Gold", "§6"),
		DIAMOND("Diamond", "§3"),
		MASTER("Master", "§2"),
		LEGEND("Legend", "§4§l"),
		GRANDMASTER("Grandmaster", "§e§l"),
		GODLIKE("Godlike", "§5§l"),
		CELESTIAL("CELESTIAL", "§b§l"),
		DIVINE("DIVINE", "§d§l"),
		ASCENDED("ASCENDED", "§c§l");

		private final String name;
		private final String color;

		public static Title get(String fullTitle) {
			String titleString = fullTitle.split("_")[0];

			for (Title title : Title.values()) {
				if (title.name().equalsIgnoreCase(titleString)) {
					return title;
				}
			}

			return null;
		}
	}
}
