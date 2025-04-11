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

package io.nadeshiko.nadeshiko.monitoring;

import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service to monitor usage statistics of the players API and automatically log them to the server's webhook
 */
public class StatisticsService implements Runnable {

	/**
	 * A series of request registries this StatisticsService has logged within this session
	 */
	private final List<RequestEntry> statsRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> questRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> achievementRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> guildRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> cardRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> leaderboardRequests = Collections.synchronizedList(new ArrayList<>());
	private final List<RequestEntry> skyBlockRequests = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The scheduler used to send the daily statistics
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	/**
	 * The target Discord webhook that this statistics server should send data to
	 */
	@Setter
	private String webhookUrl;

	public StatisticsService() {

		long midnight = LocalDateTime.now().until(LocalDate.now().
			plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
		scheduler.scheduleAtFixedRate(this, midnight, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
	}

	public void registerStatsRequest(String name) {
		this.statsRequests.add(new RequestEntry(name, null));
	}

	public void registerQuestRequest(String name) {
		this.questRequests.add(new RequestEntry(name, null));
	}

	public void registerAchievementRequest(String name) {
		this.achievementRequests.add(new RequestEntry(name, null));
	}

	public void registerGuildRequest(String name) {
		this.guildRequests.add(new RequestEntry(name, null));
	}

	public void registerCardRequest(String name, CardGame game) {
		this.cardRequests.add(new RequestEntry(name, game.name()));
	}

	public void registerLeaderboardRequest(String leaderboardName) {
		this.leaderboardRequests.add(new RequestEntry(leaderboardName, null));
	}

	public void registerSkyBlockRequest(String name) {
		this.skyBlockRequests.add(new RequestEntry(name, null));
	}

	/**
	 * Reset the registry of daily requests
	 */
	private void flush() {
		this.statsRequests.clear();
		this.questRequests.clear();
		this.achievementRequests.clear();
		this.guildRequests.clear();
		this.cardRequests.clear();
		this.leaderboardRequests.clear();
		this.skyBlockRequests.clear();
	}

	/**
	 * Gets the hour from a millisecond timestamp
	 * @param millis The millisecond timestamp to analyze
	 * @return The hour of the timestamp
	 */
	private int getTimestampHour(long millis) {
		Instant instant = Instant.ofEpochMilli(millis);
		return instant.atZone(ZoneId.systemDefault()).getHour();
	}

	private String buildHourlyStatsText() {
		Map<Integer, Integer> hourlyStatsRequests = new HashMap<>();
		Map<Integer, Integer> hourlyCardRequests = new HashMap<>();
		Map<Integer, Integer> hourlyGuildRequests = new HashMap<>();
		Map<Integer, Integer> hourlyQuestRequests = new HashMap<>();
		Map<Integer, Integer> hourlyAchievementRequests = new HashMap<>();
		Map<Integer, Integer> hourlyLeaderboardRequests = new HashMap<>();
		Map<Integer, Integer> hourlySkyBlockRequests = new HashMap<>();

		this.statsRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyStatsRequests.merge(hour, 1, Integer::sum);
		});

		this.cardRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyCardRequests.merge(hour, 1, Integer::sum);
		});

		this.guildRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyGuildRequests.merge(hour, 1, Integer::sum);
		});

		this.questRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyQuestRequests.merge(hour, 1, Integer::sum);
		});

		this.achievementRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyAchievementRequests.merge(hour, 1, Integer::sum);
		});

		this.leaderboardRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlyLeaderboardRequests.merge(hour, 1, Integer::sum);
		});

		this.skyBlockRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			hourlySkyBlockRequests.merge(hour, 1, Integer::sum);
		});

		// Table format
		StringBuilder table = new StringBuilder();
		table.append("```\\n");
		table.append(" hour | tot | sta | car | gui | que | ach | lea | sky \\n");
		table.append("------|-----|-----|-----|-----|-----|-----|-----|-----\\n");

		for (int hour = 0; hour < 24; hour++) {
			int stats = hourlyStatsRequests.getOrDefault(hour, 0);
			int cards = hourlyCardRequests.getOrDefault(hour, 0);
			int guilds = hourlyGuildRequests.getOrDefault(hour, 0);
			int quests = hourlyQuestRequests.getOrDefault(hour, 0);
			int achievements = hourlyAchievementRequests.getOrDefault(hour, 0);
			int leaderboards = hourlyLeaderboardRequests.getOrDefault(hour, 0);
			int skyBlock = hourlySkyBlockRequests.getOrDefault(hour, 0);
			int total = stats + cards + guilds + quests + achievements + leaderboards + skyBlock;

			// Skip empty
			if (total == 0) continue;

			String timeStr = String.format("%02d:00", hour);
			table.append(String.format("%-5s |%4d |%4d |%4d |%4d |%4d |%4d |%4d |%4d\\n",
				timeStr, total, stats, cards, guilds, quests, achievements, leaderboards, skyBlock));
		}
		table.append("```");
		return table.toString();
	}

	/**
	 * Build and send the statistics embed, and then flush the request cache
	 */
	private synchronized void sendStats() {
		DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

		// Title
		LocalDate yesterday = LocalDate.now().minusDays(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		embed.setTitle("API statistics for " + formatter.format(yesterday));

		// Description
		int requests = this.statsRequests.size() + this.guildRequests.size() + this.achievementRequests.size() +
			this.questRequests.size() + this.cardRequests.size() + this.leaderboardRequests.size() + this.skyBlockRequests.size();
		embed.setDescription("**Requests:**\\n" +
			"Total requests today: **" + requests + "**\\n" +
			"\\n" +
			"`/stats` requests: **" + this.statsRequests.size() + "**\\n" +
			"`/guild` requests: **" + this.guildRequests.size() + "**\\n" +
			"`/achievements` requests: **" + this.achievementRequests.size() + "**\\n" +
			"`/quests` requests: **" + this.questRequests.size() + "**\\n" +
			"`/card` requests: **" + this.cardRequests.size() + "**\\n" +
			"`/leaderboard` requests: **" + this.leaderboardRequests.size() + "**\\n" +
			"`/skyblock` requests: **" + this.skyBlockRequests.size() + "**\\n" +
			"\\n" +
			"**Hourly Distribution:**\\n" +
			buildHourlyStatsText());

		embed.setColor(new Color(246, 173, 198));
		embed.setFooter("Sent from nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		try {
			DiscordWebhook webhook = new DiscordWebhook(this.webhookUrl);
			webhook.addEmbed(embed);
			webhook.execute();
			Nadeshiko.logger.info("Sent statistics to the stats webhook");
		} catch (IOException exception) {
			Nadeshiko.logger.error("Failed to send statistics to webhook: {}", exception.getMessage());
			Nadeshiko.logger.debug("Exception details:", exception);
		}

		this.flush();
	}

	/**
	 * Called by the scheduler every day at midnight
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		Nadeshiko.logger.info("Statistics service run() called - checking webhook URL");
		if (this.webhookUrl != null) {
			Nadeshiko.logger.info("Webhook URL is set, sending statistics...");
			this.sendStats();
		} else {
			Nadeshiko.logger.warn("Webhook URL is not set, skipping statistics");
		}
	}

	/**
	 * Represents a data state for a single abstract request to the nadeshiko player API
	 */
	@Getter
	@RequiredArgsConstructor
	private static class RequestEntry {

		/**
		 * The time that this request occurred at
		 */
		private final long time = System.currentTimeMillis();

		/**
		 * The name of the player that this request was for
		 */
		private final String name;

		/**
		 * Any additional data to be stored with the request. The exact data stored depends on the
		 * implementation of this specific request type
		 */
		private final String additionalData;
	}
}
