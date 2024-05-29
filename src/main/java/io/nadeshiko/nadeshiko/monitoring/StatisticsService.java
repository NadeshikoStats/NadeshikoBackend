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
	 * A registry of {@code /stats} requests this StatisticsService has logged within this session
	 */
	private final List<RequestEntry> statsRequests = Collections.synchronizedList(new ArrayList<>());

	/**
	 * A registry of {@code /card} requests this StatisticsService has logged within this session
	 */
	private final List<RequestEntry> cardRequests = Collections.synchronizedList(new ArrayList<>());

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

	/**
	 * Register a new request to the {@code /stats} endpoint with this StatisticsService
	 * @param name The name of the player associated with the request
	 */
	public void registerStatsRequest(String name) {
		this.statsRequests.add(new RequestEntry(name, null));
	}

	/**
	 * Register a new request to the {@code /cards} endpoint with this StatisticsService
	 * @param name The name of the player associated with the request
	 * @param game The {@link CardGame} associated with the request
	 */
	public void registerCardRequest(String name, CardGame game) {
		this.cardRequests.add(new RequestEntry(name, game.name()));
	}

	/**
	 * Reset the registry of daily requests
	 */
	private void flush() {
		this.statsRequests.clear();
		this.cardRequests.clear();
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

	/**
	 * @return A URL using the {@code quickchart.io} API for a graph displaying both requests types by hour
	 */
	private String buildRequestsGraph() {
		
		Map<Integer, Integer> hourlyStatsRequests = new HashMap<>();
		Map<Integer, Integer> hourlyCardRequests = new HashMap<>();
		
		this.statsRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());
			
			if (hourlyStatsRequests.containsKey(hour)) {
				int currentValue = hourlyStatsRequests.get(hour);
				hourlyStatsRequests.remove(hour);
				hourlyStatsRequests.put(hour, currentValue + 1);
			} else {
				hourlyStatsRequests.put(hour, 1);
			}
		});

		this.cardRequests.forEach(request -> {
			int hour = this.getTimestampHour(request.getTime());

			if (hourlyCardRequests.containsKey(hour)) {
				int currentValue = hourlyCardRequests.get(hour);
				hourlyCardRequests.remove(hour);
				hourlyCardRequests.put(hour, currentValue + 1);
			} else {
				hourlyCardRequests.put(hour, 1);
			}
		});
		
		return "https://quickchart.io/chart?c={type:'bar'," +

			// Graph data
			"data:{" +

				// Graph labels (hours)
				"labels:['00:00','01:00','02:00','03:00','04:00','05:00','06:00','07:00','08:00','09:00'," +
				"'10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00','20:00'," +
				"'21:00','22:00','23:00']," +

				// Graph datasets
				"datasets:[" +

					// Stats requests
					"{label:'/stats%20requests',data:[" +
						hourlyStatsRequests.get(0) + "," + hourlyStatsRequests.get(1) + "," +
						hourlyStatsRequests.get(2) + "," + hourlyStatsRequests.get(3) + "," +
						hourlyStatsRequests.get(4) + "," + hourlyStatsRequests.get(5) + "," +
						hourlyStatsRequests.get(6) + "," + hourlyStatsRequests.get(7) + "," +
						hourlyStatsRequests.get(8) + "," + hourlyStatsRequests.get(9) + "," +
						hourlyStatsRequests.get(10) + "," + hourlyStatsRequests.get(11) + "," +
						hourlyStatsRequests.get(12) + "," + hourlyStatsRequests.get(13) + "," +
						hourlyStatsRequests.get(14) + "," + hourlyStatsRequests.get(15) + "," +
						hourlyStatsRequests.get(16) + "," + hourlyStatsRequests.get(17) + "," +
						hourlyStatsRequests.get(18) + "," + hourlyStatsRequests.get(19) + "," +
						hourlyStatsRequests.get(20) + "," + hourlyStatsRequests.get(21) + "," +
						hourlyStatsRequests.get(22) + "," + hourlyStatsRequests.get(23) +
					"]}," +

					// Cards requests
					"{label:'/card%20requests',data:[" +
						hourlyCardRequests.get(0) + "," + hourlyCardRequests.get(1) + "," +
						hourlyCardRequests.get(2) + "," + hourlyCardRequests.get(3) + "," +
						hourlyCardRequests.get(4) + "," + hourlyCardRequests.get(5) + "," +
						hourlyCardRequests.get(6) + "," + hourlyCardRequests.get(7) + "," +
						hourlyCardRequests.get(8) + "," + hourlyCardRequests.get(9) + "," +
						hourlyCardRequests.get(10) + "," + hourlyCardRequests.get(11) + "," +
						hourlyCardRequests.get(12) + "," + hourlyCardRequests.get(13) + "," +
						hourlyCardRequests.get(14) + "," + hourlyCardRequests.get(15) + "," +
						hourlyCardRequests.get(16) + "," + hourlyCardRequests.get(17) + "," +
						hourlyCardRequests.get(18) + "," + hourlyCardRequests.get(19) + "," +
						hourlyCardRequests.get(20) + "," + hourlyCardRequests.get(21) + "," +
						hourlyCardRequests.get(22) + "," + hourlyCardRequests.get(23) +
					"]}" +
				"]" +

			"}" +
		"}";
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
		embed.setDescription("**Requests:**\\n" +
			"Total requests today: **" + (this.statsRequests.size() + this.cardRequests.size()) + "**\\n" +
			"\\n" +
			"Total `/stats` requests today: **" + this.statsRequests.size() + "**\\n" +
			"Total `/card` requests today: **" + this.cardRequests.size() + "**\\n" +
			"\\n" +
			"**Hourly Visualization:**");

		embed.setColor(new Color(246, 173, 198));
		embed.setImage(this.buildRequestsGraph());
		embed.setFooter("Sent from Nadeshiko " + Nadeshiko.VERSION, "https://nadeshiko.io/img/logo.png");

		System.out.println(this.buildRequestsGraph());

		try {
			DiscordWebhook webhook = new DiscordWebhook(this.webhookUrl);
			webhook.addEmbed(embed);
			webhook.execute();
		} catch (IOException exception) {
			Nadeshiko.logger.error("Failed to log to the stats webhook! Is the URL valid?");
			this.webhookUrl = null; // Disable the service if we find it to be using an invalid URL
		}

		this.flush();
	}

	/**
	 * Called by the scheduler every day at midnight
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		if (this.webhookUrl != null) {
			this.sendStats();
		}
	}

	/**
	 * Represents a data state for a single abstract request to the Nadeshiko player API
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
