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

	private final HashMap<Duels, BufferedImage> iconMap = new HashMap<>();

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject duels = stats.getAsJsonObject("stats").getAsJsonObject("Duels");

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		int kills = duels.get("kills").getAsInt();
		int deaths = duels.get("deaths").getAsInt();
		int wins = duels.get("wins").getAsInt();
		int losses = duels.get("losses").getAsInt();

		int winstreak = 0;
		int best_winstreak = 0;

		// Winstreaks can be disabled from the API
		if (duels.has("current_winstreak")) {
			winstreak = duels.get("current_winstreak").getAsInt();
			best_winstreak = duels.get("best_overall_winstreak").getAsInt();
		}

		// Draw title
		this.drawTitle(g, duels);

		// Set up the stat font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Bold", Font.BOLD, 38));

		// Draw K/D ratio
		String kdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		g.drawString(kdr, 750 - (g.getFontMetrics().stringWidth(kdr) / 2), 158);
		this.drawProgress(g, 664, 173, 177, kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, 1007 - (g.getFontMetrics().stringWidth(wlr) / 2), 158);
		this.drawProgress(g, 921, 173, 177, wins / (double) (wins + losses));

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
		g.drawString(String.format("%,d", wins), 1175 + winsWidth + 10, 140);

		// Winstreaks might be disabled on the API
		if (duels.has("current_winstreak")) {
			g.drawString(String.format("%,d", winstreak), 1175 + winstreakWidth + 10, 178);
			g.drawString(String.format("%,d", best_winstreak), 1175 + bestWinstreakWidth + 10, 208);
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

	private void drawTitle(Graphics g, @NonNull JsonObject duelsStats) {

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
		g.fillRect(808, 56, 16, 4);

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

		MinecraftRenderer.drawMinecraftString(g, finalTitle, 845, 67, 30);
	}

	private void drawDuel(Graphics2D g, @NonNull Duels duel, @NonNull JsonObject duelsStats, int baseX) {

		int kills = 0, deaths = 1, wins = 0, losses = 1;

		if (duelsStats.has(duel.getApiName() + "_kills")) {
			kills = duelsStats.get(duel.getApiName() + "_kills").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO) && duelsStats.has("bridge_kills")) {
			// Inconsistent API naming breaks with bridge duels...
			kills = duelsStats.get("bridge_kills").getAsInt();
		}

		if (duelsStats.has(duel.getApiName() + "_deaths")) {
			deaths = duelsStats.get(duel.getApiName() + "_deaths").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO) && duelsStats.has("bridge_deaths")) {
			// Inconsistent API naming breaks with bridge duels...
			deaths = duelsStats.get("bridge_deaths").getAsInt();
		}

		if (duelsStats.has(duel.getApiName() + "_wins")) {
			wins = duelsStats.get(duel.getApiName() + "_wins").getAsInt();
		}

		if (duelsStats.has(duel.getApiName() + "_losses")) {
			losses = duelsStats.get(duel.getApiName() + "_losses").getAsInt();
		}

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
		String kdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		g.drawString(kdr, baseX + 78 - (g.getFontMetrics().stringWidth(kdr) / 2), 340);
		this.drawProgress(g, baseX + 6, 354, 146, kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, baseX + 261 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, baseX + 189, 354, 146, wins / (double) (wins + losses));

		// Draw kills
		int killsWidth = g.getFontMetrics(smallLight).stringWidth("Kills  ");
		int killsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", kills));
		int killsTotalWidth = killsWidth + killsCountWidth;
		int killsLeftX = baseX + 80 - (killsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Kills", killsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", kills), killsLeftX + killsWidth, 425);

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
		ROOKIE("Rookie", "§7"),
		IRON("Iron", "§f"),
		GOLD("Gold", "§6"),
		DIAMOND("Diamond", "§3"),
		MASTER("Master", "§2"),
		LEGEND("Legend", "§l§4"),
		GRANDMASTER("Grandmaster", "§l§e"),
		GODLIKE("Godlike", "§l§5"),
		CELESTIAL("CELESTIAL", "§l§b"),
		DIVINE("DIVINE", "§l§d"),
		ASCENDED("ASCENDED", "§l§c");

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
