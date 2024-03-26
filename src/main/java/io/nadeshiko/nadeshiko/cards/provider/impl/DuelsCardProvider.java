package io.nadeshiko.nadeshiko.cards.provider.impl;

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DuelsCardProvider extends CardProvider {

	public DuelsCardProvider() {
		super(CardGame.DUELS);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject duels = stats.getAsJsonObject("Duels");

		int kills = duels.get("kills").getAsInt();
		int deaths = duels.get("deaths").getAsInt();
		int wins = duels.get("wins").getAsInt();
		int losses = duels.get("losses").getAsInt();

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

		// Draw top duels
		ArrayList<Duels> topDuels = this.getTopDuels(duels);
		this.drawTopDuel(g, topDuels.get(0), duels);
		this.drawSecondDuel(g, topDuels.get(1), duels);
	}

	private void drawTopDuel(Graphics g, Duels duel, JsonObject duelsStats) {

		int kills = duelsStats.get(duel.getApiName() + "_duel_kills").getAsInt();
		int deaths = duelsStats.get(duel.getApiName() + "_duel_deaths").getAsInt();
		int wins = duelsStats.get(duel.getApiName() + "_duel_wins").getAsInt();
		int losses = duelsStats.get(duel.getApiName() + "_duel_losses").getAsInt();

		// Set up the name font
		g.setColor(Color.WHITE);
		g.setFont(new Font("Inter Medium", Font.BOLD, 22));

		// Draw duel name
		int nameWidth = g.getFontMetrics().stringWidth(duel.name());
		g.drawString(duel.getDisplayName(), 635, 290);

		// Draw the line beside the duel name
		g.setColor(this.getColor());
		g.fillRect(635 + nameWidth + 15, 280, 350 - nameWidth - 15, 2);
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
	}

	private void drawSecondDuel(Graphics g, Duels duel, JsonObject duelsStats) {

		int kills = duelsStats.get(duel.getApiName() + "_duel_kills").getAsInt();
		int deaths = duelsStats.get(duel.getApiName() + "_duel_deaths").getAsInt();
		int wins = duelsStats.get(duel.getApiName() + "_duel_wins").getAsInt();
		int losses = duelsStats.get(duel.getApiName() + "_duel_losses").getAsInt();

		// Set up the name font
		g.setFont(new Font("Inter Medium", Font.BOLD, 22));

		// Draw duel name
		int nameWidth = g.getFontMetrics().stringWidth(duel.name());
		g.drawString(duel.getDisplayName(), 1068, 290);

		// Draw the line beside the duel name
		g.setColor(this.getColor());
		g.fillRect(1068 + nameWidth + 15, 280, 350 - nameWidth - 15, 2);
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
	}

	private ArrayList<Duels> getTopDuels(JsonObject duelsStats) {
		ArrayList<Duels> duels = new ArrayList<>();

		Duels first = null, second = null;
		int firstWins = -1, secondWins = -1;

		for (Duels duel : Duels.values()) {
			if (duelsStats.has(duel.getApiName() + "_duel_wins")) {
				int wins = duelsStats.get(duel.getApiName() + "_duel_wins").getAsInt();

				if (wins > firstWins) {
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
		BLITZ("blitz", "BLITZ"),
		BOW("bow", "BOW"),
		BOWSPLEEF("bowspleef", "BOW SPLEEF"),
		BRIDGE("bridge", "BRIDGE"),
		CLASSIC("classic", "CLASSIC"),
		COMBO("combo", "COMBO"),
		NODEBUFF("potion", "NODEBUFF"),
		OP("op", "OP"),
		SKYWARS("sw", "SKYWARS"),
		SUMO("sumo", "SUMO");

		private final String apiName, displayName;
	}
}
