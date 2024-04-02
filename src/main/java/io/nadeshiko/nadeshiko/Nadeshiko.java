package io.nadeshiko.nadeshiko;

import com.google.gson.Gson;
import io.nadeshiko.nadeshiko.api.CardController;
import io.nadeshiko.nadeshiko.api.StatsController;
import io.nadeshiko.nadeshiko.cards.CardsCache;
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

	/**
	 * Global static logger
	 */
	public static Logger logger = LoggerFactory.getLogger(Nadeshiko.class);

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

		// Read the API key from the config file
		this.hypixelKey = (String) this.config.get("hypixel_key");

		// Verify that an API key was provided
		if (this.hypixelKey == null) {
			logger.error("No API key was provided in the config! Halting.");
			return;
		} else {

			// Censor the API key, except for the first section (8 characters)
			String censoredKey = this.hypixelKey.replaceAll("[^-]", "*");
			String compositeKey = this.hypixelKey.substring(0, 7) + censoredKey.substring(8);

			logger.info("Using Hypixel API key " + compositeKey);
		}

		// Test the Hypixel and Mojang APIs
		if (!this.testHypixel()) {
			logger.error("Failed to connect to the Hypixel API! Verify the connection and API key.");
			return;
		}

		if (!this.testMojang()) {
			logger.error("Failed to connect to the Mojang API! Verify the connection.");
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

		logger.info("Started Nadeshiko in {} seconds", ((System.currentTimeMillis() - startTime) / 1000f));
	}

	/**
	 * Shuts down this instance of the backend, stopping the service
	 */
	public void shutdown() {
		logger.info("Stopping!");

		this.spark.stop();

		logger.info("Nadeshiko was running for {} ms", System.currentTimeMillis() - this.startTime);
		logger.info("Stopped Nadeshiko");
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

	/**
	 * Send an arbitrary request to the Hypixel API, verifying the connection, API key, and the status of the API
	 * @return The success of the request
	 */
	private boolean testHypixel() {
		String endpoint = "https://api.hypixel.net/v2/counts";
		HTTPUtil.Response response;

		try {
			response = HTTPUtil.get(endpoint + "?key=" + hypixelKey);
			logger.info("Got status {} from Hypixel", response.status());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return response.status() == 200;
	}

	/**
	 * Send an arbitrary request to the Mojang API, verifying the connection and the status of the API
	 * @return The success of the request
	 */
	private boolean testMojang() {
		String endpoint = "https://api.mojang.com/users/profiles/minecraft/hypixel";
		HTTPUtil.Response response;

		try {
			response = HTTPUtil.get(endpoint);
			logger.info("Got status {} from Mojang", response.status());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return response.status() == 200;
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
