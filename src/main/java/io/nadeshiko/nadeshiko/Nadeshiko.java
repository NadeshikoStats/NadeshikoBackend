package io.nadeshiko.nadeshiko;

import com.google.gson.Gson;
import io.nadeshiko.nadeshiko.api.CardController;
import io.nadeshiko.nadeshiko.api.StatsController;
import io.nadeshiko.nadeshiko.cards.CardsCache;
import io.nadeshiko.nadeshiko.monitoring.DiscordMonitor;
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

	public static String VERSION = "0.4.0";

	/**
	 * Global static logger
	 */
	public static Logger logger = LoggerFactory.getLogger(Nadeshiko.class);

	@Getter
	private DiscordMonitor discordMonitor;

	@Getter
	private final StatsCache statsCache = new StatsCache();

	@Getter
	private final CardsCache cardsCache = new CardsCache();

	/**
	 * The timestamp at which this instance began startup
	 */
	private long startTime;

	/**
	 * The Spark instance associated with this server instance
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
	 * Start this instance of backend. Reads and verifies the configuration file, verifies connections to both
	 * the Hypixel and Mojang APIs, and verifies the Hypixel API key. After this, starts the service on the
	 * provided port.
	 */
	public void startup() {
		this.startTime = System.currentTimeMillis();

		// Read config file
		try {
			this.readConfig();
		} catch (IOException e) {
			logger.error("Failed to read configuration file! Halting.");
			logger.error(e.toString());
			return;
		}

		// Start the Discord monitor, if enabled
		this.igniteDiscordMonitor();
		discordMonitor.log("Igniting Nadeshiko...");

		// Read the API key from the config file
		this.hypixelKey = (String) this.config.get("hypixel_key");

		// Verify that a valid API key was provided
		if (this.hypixelKey == null) {
			this.alert("No Hypixel API key was provided in the config! Halting.");
			return;
		} else {

			if (this.hypixelKey.length() < 32) {
				this.alert("A Hypixel API key was provided, but it's malformed! Halting.");
				return;
			}

			// Censor the API key, except for the first section (8 characters)
			String censoredKey = this.hypixelKey.replaceAll("[^-]", "*");
			String compositeKey = this.hypixelKey.substring(0, 7) + censoredKey.substring(8);

			logger.info("Using Hypixel API key " + compositeKey);
		}

		// Test the Hypixel and Mojang APIs
		if (!this.testHypixel()) {
			this.alert("Failed to connect to the Hypixel API! Verify the connection and API key. Halting.");
			return;
		}

		if (!this.testMojang()) {
			this.alert("Failed to connect to the Mojang API! Verify the connection. Halting.");
			return;
		}

		int port = 2000; // Default port

		// If a port was provided, use it instead!
		if (this.config.get("port") != null) {
			port = (int) ((double) this.config.get("port")); // No idea why this double cast is needed
		} else {
			logger.warn("No port was provided! Defaulting to {}!", port);
		}

		// Ignite the spark instance on the provided port
		logger.info("Starting service on port {}", port);
		this.spark.port(port);
		this.spark.init();

		// Bind endpoints to their controllers
		spark.get("/stats", StatsController.serveStatsEndpoint);
		spark.get("/card/:data", CardController.serveCardEndpoint);

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

		this.spark.stop();

		logger.info("Nadeshiko was running for {} ms", System.currentTimeMillis() - this.startTime);
		logger.info("Stopped Nadeshiko");

		discordMonitor.log("Stopped! Nadeshiko was running since <t:%d:f>", this.startTime / 1000);
	}

	/**
	 * Reads the configuration file into the {@code config} map
	 * @throws IOException If an exception was thrown reading the configuration file
	 */
	private void readConfig() throws IOException {
		File configFile = new File("config.json");

		if (!configFile.exists()) {
			logger.error("No config.json was found! Halting.");
			return;
		}

		this.config = (new Gson()).fromJson(Files.readString(configFile.toPath()), Map.class);
	}

	private void igniteDiscordMonitor() {
		Map<?, ?> discordConfig = (Map<?, ?>) this.config.get("discord");

		// Disable the discord monitor if it is set to "disabled", or is missing from the config entirely
		if (discordConfig == null || !((boolean) discordConfig.get("enabled"))) {
			this.discordMonitor = new DiscordMonitor(null, null);
			logger.info("Disabling Discord monitor!");
			return;
		}

		String logUrl = (String) discordConfig.get("log_url");
		String alertUrl = (String) discordConfig.get("alert_url");

		this.discordMonitor = new DiscordMonitor(logUrl, alertUrl);
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
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
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
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

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
