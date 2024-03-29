package io.nadeshiko.nadeshiko.cards.provider.impl;

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.CardGenerator;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class DuelsCardProvider extends CardProvider {

	public DuelsCardProvider() {
		super(CardGame.DUELS);

		// Read the duel icons from resources into the cache
		for (Duels duel : Duels.values()) {
			try (InputStream iconStream =  CardGenerator.class.
				getResourceAsStream("/cards/duels/" + duel.getTextureName() + ".png")) {

				if (iconStream == null) {
					Nadeshiko.logger.error("Missing duel icon for {}!", duel.name());
					continue;
				}

				byte[] iconBytes = iconStream.readAllBytes();
				BufferedImage icon = Nadeshiko.INSTANCE.getCardGenerator().createImageFromBytes(iconBytes);

				this.iconMap.put(duel, icon);
			} catch (IOException e) {
				Nadeshiko.logger.error("Failed reading duel icon for {}!", duel.name());
			}
		}
	}

	private final HashMap<Duels, BufferedImage> iconMap = new HashMap<>();

	private final Font smallLight = new Font("Inter Medium", Font.PLAIN, 18);
	private final Font smallBold = new Font("Inter Medium", Font.BOLD, 18);

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject duels = stats.getAsJsonObject("Duels");

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
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
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
		this.drawTopDuel(g, topDuels.get(0), duels);
		this.drawSecondDuel(g, topDuels.get(1), duels);
	}

	private void drawTitle(Graphics g, @NonNull JsonObject duelsStats) {

		// Ensure the player actually has a title
		if (!duelsStats.has("active_cosmetictitle")) {
			return;
		}

		String activeTitle = duelsStats.get("active_cosmetictitle").getAsString();

		MinecraftRenderer.drawString(g, activeTitle, 845, 65, 30);
	}

	private void drawTopDuel(Graphics g, @NonNull Duels duel, @NonNull JsonObject duelsStats) {

		int kills = 0, deaths = 0, wins = 0, losses = 0;

		if (duelsStats.has(duel.getApiName() + "_kills")) {
			kills = duelsStats.get(duel.getApiName() + "_kills").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO)) {
			// Inconsistent API naming breaks with bridge duels...
			kills = duelsStats.get("bridge_kills").getAsInt();
		}

		if (duelsStats.has(duel.getApiName() + "_deaths")) {
			deaths = duelsStats.get(duel.getApiName() + "_deaths").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO)) {
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
		int nameWidth = g.getFontMetrics().stringWidth(duel.name());
		g.drawString(duel.getDisplayName(), 635, 290);

		// Draw duel icon
		g.drawImage(this.iconMap.get(duel), 635 + nameWidth + 15, 265, null);

		// Draw the line beside the duel name
		g.setColor(this.getColor());
		g.fillRect(635 + nameWidth + 60, 280, 350 - nameWidth - 60, 2);
		g.setColor(Color.WHITE);

		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw K/D ratio
		String kdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		g.drawString(kdr, 713 - (g.getFontMetrics().stringWidth(kdr) / 2), 340);
		this.drawProgress(g, 641, 354, 146, kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, 896 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, 824, 354, 146, wins / (double) (wins + losses));

		// Draw kills
		int killsWidth = g.getFontMetrics(smallLight).stringWidth("Kills  ");
		int killsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", kills));
		int killsTotalWidth = killsWidth + killsCountWidth;
		int killsLeftX = 715 - (killsTotalWidth / 2);

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
		int winsLeftX = 898 - (winsTotalWidth / 2);

		g.setColor(new Color(138, 138, 138));
		g.setFont(smallLight);
		g.drawString("Wins", winsLeftX, 425);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(String.format("%,d", wins), winsLeftX + winsWidth, 425);
	}

	private void drawSecondDuel(Graphics g, Duels duel, JsonObject duelsStats) {

		int kills = 0, deaths = 0, wins = 0, losses = 0;

		if (duelsStats.has(duel.getApiName() + "_kills")) {
			kills = duelsStats.get(duel.getApiName() + "_kills").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO)) {
			// Inconsistent API naming breaks with bridge duels...
			kills = duelsStats.get("bridge_kills").getAsInt();
		}

		if (duelsStats.has(duel.getApiName() + "_deaths")) {
			deaths = duelsStats.get(duel.getApiName() + "_deaths").getAsInt();
		} else if (duel.equals(Duels.BRIDGE_SOLO)) {
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
		g.setFont(new Font("Inter Medium", Font.BOLD, 22));

		// Draw duel name
		int nameWidth = g.getFontMetrics().stringWidth(duel.name());
		g.drawString(duel.getDisplayName(), 1068, 290);

		// Draw duel icon
		g.drawImage(this.iconMap.get(duel), 1068 + nameWidth + 15, 265, null);

		// Draw the line beside the duel name
		g.setColor(this.getColor());
		g.fillRect(1068 + nameWidth + 60, 280, 350 - nameWidth - 60, 2);
		g.setColor(Color.WHITE);

		// Set up the stat font
		g.setFont(new Font("Inter Bold", Font.BOLD, 24));

		// Draw K/D ratio
		String kdr = (Math.round((kills / (double) deaths) * 100) / 100d) + "";
		g.drawString(kdr, 1146 - (g.getFontMetrics().stringWidth(kdr) / 2), 340);
		this.drawProgress(g, 1074, 355, 146, kills / (double) (kills + deaths));

		// Draw W/L ratio
		String wlr = (Math.round((wins / (double) losses) * 100) / 100d) + "";
		g.drawString(wlr, 1329 - (g.getFontMetrics().stringWidth(wlr) / 2), 340);
		this.drawProgress(g, 1257, 355, 146, wins / (double) (wins + losses));

		// Draw kills
		int killsWidth = g.getFontMetrics(smallLight).stringWidth("Kills  ");
		int killsCountWidth = g.getFontMetrics(smallBold).stringWidth(String.format("%,d", kills));
		int killsTotalWidth = killsWidth + killsCountWidth;
		int killsLeftX = 1150 - (killsTotalWidth / 2);

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
		int winsLeftX = 1332 - (winsTotalWidth / 2);

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
		ARENA("duel_arena", "ARENA", "ARENA"),
		BLITZ("blitz_duel", "BLITZ", "BLITZ"),
		BOW("bow_duel", "BOW", "BOW"),
		BOWSPLEEF("bowspleef_duel", "BOW SPLEEF", "BOWSPLEEF"),
		BOXING("boxing_duel", "BOXING", "BOXING"),
		BRIDGE_SOLO("bridge_duel", "BRIDGE SOLO", "BRIDGE"),
		// TODO bridge 2s, very malformed api names
		CLASSIC("classic_duel", "CLASSIC", "CLASSIC"),
		COMBO("combo_duel", "COMBO", "COMBO"),
		MEGAWALLS("mw_duel", "MEGA WALLS", "MEGAWALLS"),
		NODEBUFF("potion_duel", "NODEBUFF", "NODEBUFF"),
		OP("op_duel", "OP", "OP"),
		PARKOUR("parkour_eight", "PARKOUR", "PARKOUR"),
		SKYWARS_SOLO("sw_duel", "SKYWARS SOLO", "SKYWARS"),
		SKYWARS_DOUBLES("sw_doubles", "SKYWARS 2S", "SKYWARS"),
		SUMO("sumo_duel", "SUMO", "SUMO"),
		UHC_SOLO("uhc_duel", "UHC SOLO", "UHC"),
		UHC_DOUBLES("uhc_doubles", "UHC 2S", "UHC");

		private final String apiName;
		private final String displayName;
		private final String textureName;
	}
}
