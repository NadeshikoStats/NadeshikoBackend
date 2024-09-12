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

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkCardProvider extends CardProvider {

	private final Color LIGHT_GRAY = new Color(187, 187, 187);

	public NetworkCardProvider() {
		super(CardGame.NETWORK);
	}

	@Override
	public void generate(BufferedImage image, JsonObject stats) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		JsonObject profile = stats.getAsJsonObject("profile");
		JsonObject guild = stats.get("guild") instanceof JsonNull ? null : stats.getAsJsonObject("guild");

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw the network level
		int networkLevel = profile.get("network_level").getAsInt();
		double networkLevelProgress = profile.get("network_level").getAsDouble() % 1;
		g.setColor(networkLevel > 250 ? new Color(255, 138, 0) : Color.WHITE);
		g.setFont(mediumLight);
		g.drawString("Level", 636, 140);
		g.setFont(mediumBold);
		g.drawString(Integer.toString(networkLevel), 695, 140);

		// Draw the network multiplier
		int levelWidth = g.getFontMetrics().stringWidth(Integer.toString(networkLevel));
		g.setColor(Color.GRAY);
		g.setFont(mediumLight);
		g.drawString("(" + profile.get("coin_multiplier") + "x)", 700 + levelWidth, 140);

		// Draw the network level progress bar
		this.drawProgress(g, 636, 153, 780, 14, networkLevelProgress);

		// Draw the first login date
		this.drawLabelValuePair(g, "First Login",
			this.formatDate(profile.get("first_login").getAsLong()), 635, 205);

		// Draw the last login date
		if (profile.get("last_login").getAsInt() > 0) {
			this.drawLabelValuePair(g, "Last Login",
				this.formatDate(profile.get("last_login").getAsLong()), 1065, 205);
		}

		// Draw the general stats card
		this.drawGeneral(g, profile);

		// Draw the guild card
		this.drawGuild(g, guild);
	}

	private void drawGeneral(Graphics2D g, JsonObject profile) {
		this.drawLabelValuePair(g, "Achievement Points",
			String.format("%,d", profile.get("achievement_points").getAsInt()), 635, 325);
		this.drawLabelValuePair(g, "Karma",
			String.format("%,d", profile.get("karma").getAsLong()), 635, 357);
		this.drawLabelValuePair(g, "Quests Completed",
			String.format("%,d", profile.get("quests_completed").getAsInt()), 635, 389);
		this.drawLabelValuePair(g, "Ranks Gifted",
			String.format("%,d", profile.get("ranks_gifted").getAsInt()), 635, 422);
	}

	private void drawGuild(Graphics2D g, JsonObject guild) {

		// Ensure the user is in a guild
		if (guild == null) {
			g.setColor(Color.DARK_GRAY);
			g.setFont(smallLight);
			g.drawString("None", 1220, 370);
			return;
		}

		// Draw guild name and tag manually since we need to support color codes
		g.setColor(LIGHT_GRAY);
		g.setFont(smallLight);
		g.drawString("Name", 1065, 325);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		String name = guild.get("name").getAsString();
		int dotWidth = g.getFontMetrics().stringWidth("...");
		int width = MinecraftRenderer.customWidth(g, name + " " + guild.get("tag").getAsString());
		boolean nameTruncated = false;
		while (width > 300 - dotWidth) {
			nameTruncated = true;
			name = name.substring(0, Math.max(0, name.length() - 1));
			width = MinecraftRenderer.customWidth(g, name + "... " + guild.get("tag").getAsString());
		}
		if (nameTruncated) {
			name += "...";
		}
		MinecraftRenderer.drawCustomString(g,
			name + " " + guild.get("tag").getAsString(), 1125, 325);

		this.drawLabelValuePair(g, "Level",
			String.format("%,d", guild.get("level").getAsInt()), 1065, 357);
		this.drawLabelValuePair(g, "Members",
			String.format("%,d", guild.get("members").getAsInt()), 1065, 389);
		this.drawLabelValuePair(g, "Joined",
			this.formatDate(guild.get("joined").getAsLong()), 1065, 422);
	}

	private void drawLabelValuePair(Graphics2D g, String label, Object value, int x, int y) {
		g.setColor(LIGHT_GRAY);
		g.setFont(smallLight);
		g.drawString(label, x, y);
		g.setColor(Color.WHITE);
		g.setFont(smallBold);
		g.drawString(value.toString(), x + g.getFontMetrics(smallLight).stringWidth(label) + 10, y);
	}

	private String formatDate(long timestamp) {
		Date date = new Date(timestamp);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return format.format(date);
	}
}

