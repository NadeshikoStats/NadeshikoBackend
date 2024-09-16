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

package io.nadeshiko.nadeshiko;

import com.google.gson.Gson;
import io.nadeshiko.nadeshiko.api.*;
import io.nadeshiko.nadeshiko.cards.CardsCache;
import io.nadeshiko.nadeshiko.leaderboards.LeaderboardService;
import io.nadeshiko.nadeshiko.monitoring.DiscordMonitor;
import io.nadeshiko.nadeshiko.monitoring.StatisticsService;
import io.nadeshiko.nadeshiko.stats.GuildCache;
import io.nadeshiko.nadeshiko.stats.StatsCache;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * Main class of the Nadeshiko backend
 *
 * @author chloe
 * @since March 11, 2024
 */
public class Nadeshiko {

	/**
	 * Global Nadeshiko instance, created by the {@link Nadeshiko#main(String[])} entry point
	 */
	public static Nadeshiko INSTANCE = null;

	public static String VERSION = "0.9.0";
	public static String DEFAULT_DATABASE = "mongodb://localhost:27017";
	public static int DEFAULT_PORT = 2000;

	/**
	 * Global static logger
	 */
	public static Logger logger = LoggerFactory.getLogger("nadeshiko");

	/**
	 * The {@link DiscordMonitor} instance of this backend instance
	 */
	@Getter
	private DiscordMonitor discordMonitor;

	/**
	 * The {@link StatsCache} instance of this backend instance
	 */
	@Getter
	private final StatsCache statsCache = new StatsCache();

	/**
	 * The {@link CardsCache} instance of this backend instance
	 */
	@Getter
	private final CardsCache cardsCache = new CardsCache();

	/**
	 * The {@link GuildCache} instance of this backend instance
	 */
	@Getter
	private final GuildCache guildCache = new GuildCache();

	/**
	 * The {@link StatisticsService} of this backend instance
	 */
	@Getter
	private final StatisticsService statsService = new StatisticsService();

	/**
	 * The {@link LeaderboardService} of this backend instance
	 */
	@Getter
	private final LeaderboardService leaderboardService = new LeaderboardService();

	/**
	 * The timestamp at which this instance began startup
	 */
	private long startTime;

	/**
	 * The Spark instance of this backend instance
	 */
	private final Service spark = Service.ignite();

	/**
	 * The Hypixel API key used by this instance
	 */
	@Getter
	private String hypixelKey;

	/**
	 * The configuration stored as a map, loaded from config.json at startup
	 */
	@Getter
	private Map<?, ?> config;

	/**
	 * The port that this backend instance is operating on
	 */
	@Getter
	private int port = DEFAULT_PORT;

	/**
	 * Start this instance of backend.
	 * <p>
	 * Reads and verifies the configuration file, verifies connections to both the Hypixel and Mojang APIs, and
	 * verifies the Hypixel API key. After this, starts the service on the provided port.
	 */
	public void startup() {
		this.startTime = System.currentTimeMillis();

		// Pre-release warning
		if (VERSION.contains("SNAPSHOT")) {
			logger.warn("============================ WARNING ===========================");
			logger.warn("This is a pre-release version of nadeshiko. (version {})", VERSION);
			logger.warn("Do NOT use this version in production; it may be unstable!");
			logger.warn("================================================================");
		}

		// Read config file
		this.readConfig();

		// Start the Discord monitor, if enabled
		this.igniteDiscordMonitor();
		discordMonitor.log("Igniting Nadeshiko...");

		// Connect to the leaderboard database, creating the collections if required
		String uri = this.config.containsKey("database") ? (String) this.config.get("database") : DEFAULT_DATABASE;
		this.leaderboardService.connect(uri);

		// Read the API key from the config file
		this.hypixelKey = (String) this.config.get("hypixel_key");

		// Verify that a valid API key was provided
		if (this.hypixelKey == null) {
			this.alert("No Hypixel API key was provided in the config! Halting.");
			return;
		} else if (this.hypixelKey.length() < 32) {
			this.alert("A Hypixel API key was provided, but it's malformed! Halting.");
			return;
		}

		String censoredKey = this.hypixelKey.replaceAll("[^-]", "*");
		String compositeKey = this.hypixelKey.substring(0, 7) + censoredKey.substring(8);
        logger.info("Using Hypixel API key {}", compositeKey);

		// Test the connections to the APIs used
		this.testApiConnections();

		// If a port was provided, use it instead of the default!
		if (this.config.get("port") != null) {
			this.port = (int) ((double) this.config.get("port")); // No idea why this double cast is needed
		} else {
			logger.warn("No port was provided! Defaulting to {}!", DEFAULT_PORT);
		}

		// Ignite the spark instance on the provided port
		logger.info("Starting service on port {}", port);
		this.spark.port(this.port);
		this.spark.init();

		// Bind endpoints to their controllers
		spark.get("/achievements", AchievementsController.serveAchievementsEndpoint);
		spark.get("/card/:data", CardController.serveCardEndpoint);
		spark.get("/guild", GuildController.serveGuildEndpoint);
		spark.get("/stats", StatsController.serveStatsEndpoint);
		spark.get("/quests", QuestsController.serveQuestsEndpoint);
		spark.get("/leaderboard", LeaderboardController.serverLeaderboardEndpoint);
		spark.get("/", (request, response) -> "nadeshiko backend version " + VERSION);

		// Set up the shutdown method on JVM stop
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

		double startSeconds = ((System.currentTimeMillis() - startTime) / 1000f);
		logger.info("Nadeshiko is now up! Took {} seconds to ignite!", startSeconds);
		discordMonitor.ok("Nadeshiko is now up! Took %f seconds to ignite!", startSeconds);
	}

	/**
	 * Shuts down this instance of the backend, stopping the service
	 */
	public void shutdown() {
		logger.info("Stopping!");

		// Disconnect from the database
		this.leaderboardService.disconnect();

		// Stop the Spark instance
		this.spark.stop();

		logger.info("Nadeshiko was running for {} ms", System.currentTimeMillis() - this.startTime);
		logger.info("Stopped Nadeshiko");
		discordMonitor.log("Stopped! Nadeshiko was running since <t:%d:f>", this.startTime / 1000);
	}

	/**
	 * Reads the configuration file into the {@code config} map. If the process fails, the server is terminated
	 */
	private void readConfig() {
		File configFile = new File("config.json");

		// Verify that the config file exists
		if (!configFile.exists()) {
			logger.error("No config.json was found! Halting.");
			System.exit(1);
		}

		// Read the config file, parse it, and store it in memory
		try {
			this.config = (new Gson()).fromJson(Files.readString(configFile.toPath()), Map.class);
		} catch (IOException e) {
			logger.error("Failed to read configuration file! Halting.");
			System.exit(1);
		}
	}

	/**
	 * Tests the connection to the various APIs used by the backend. If any connections fail, the server is
	 * terminated
	 */
	private void testApiConnections() {

		// Test the connection to the Hypixel API and the API key
		if (!this.testHypixel()) {
			this.alert("Failed to connect to the Hypixel API! Verify the connection and API key. Halting.");
			System.exit(2);
		}

		// Test the connection to the Mojang API
		else if (!this.testMojang()) {
			this.alert("Failed to connect to the Mojang API! Verify the connection. Halting.");
			System.exit(2);
		}
	}

	/**
	 * Ignite the {@link DiscordMonitor} instance if enabled in the configuration file
	 */
	private void igniteDiscordMonitor() {

		// The "discord" section of the configuration file
		Map<?, ?> discordConfig = (Map<?, ?>) this.config.get("discord");

		// Disable the discord monitor if it is set to "disabled", or is missing from the config entirely
		if (discordConfig == null || !((boolean) discordConfig.get("enabled"))) {
			this.discordMonitor = new DiscordMonitor(null);
			logger.info("Disabling Discord monitor!");
			return;
		}

		String logUrl = (String) discordConfig.get("log_url");
		this.discordMonitor = new DiscordMonitor(logUrl);

		this.getStatsService().setWebhookUrl((String) discordConfig.get("stats_url"));
	}

	/**
	 * Send an arbitrary request to the Hypixel API, verifying the connection, API key, and the status of the API
	 * @return The success of the request
	 */
	private boolean testHypixel() {
		String endpoint = "https://api.hypixel.net/v2/counts";

		try {
			HTTPUtil.Response response = HTTPUtil.get(endpoint + "?key=" + hypixelKey);
			logger.info("Got status {} from Hypixel", response.status());

			// Log the response if it wasn't 200, and return the success of the request
			if (response.status() != 200) {
				logger.warn("Hypixel response: {}", response.response());
				this.alert("Failed to connect to Hypixel API, got response " + response.status() + "!");
				return false; // Request failed
			} else {
				return true; // Request succeeded
			}
		} catch (Exception e) {
			logger.error("Failed to test Hypixel API", e);
			return false; // Request failed
		}
	}

	/**
	 * Send an arbitrary request to the Mojang API, verifying the connection and the status of the API
	 * @return The success of the request
	 */
	private boolean testMojang() {
		String endpoint = "https://api.mojang.com/users/profiles/minecraft/hypixel";

		try {
			HTTPUtil.Response response = HTTPUtil.get(endpoint);
			logger.info("Got status {} from Mojang", response.status());

			// Log the response if it wasn't 200, and return the success of the request
			if (response.status() != 200) {
				logger.warn("Mojang response: {}", response.response());
				this.alert("Failed to connect to Mojang API, got response " + response.status() + "!");
				return false; // Request failed
			} else {
				return true; // Request succeeded
			}
		} catch (Exception e) {
			logger.error("Failed to test Mojang API", e);
			return false; // Request failed
		}
	}

	/**
	 * Raises an alert that is logged to both the logger and, optionally, the Discord monitor
	 * @param message The message to log, including {@link String#format(String, Object...)} formatting codes
	 * @param args Optional arguments for the formatting codes provided in {@code message}
	 */
	public void alert(Object message, Object... args) {
		String formatted = String.format(message.toString(), args);
		Nadeshiko.logger.error(formatted);
		discordMonitor.alert(formatted);
	}

	/**
	 * Create the global Nadeshiko instance and start it
	 * @param args ignored
	 */
	public static void main(String[] args) {
		INSTANCE = new Nadeshiko();
		INSTANCE.startup();
	}
}
