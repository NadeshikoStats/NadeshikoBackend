package io.hystats.hystats;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.hystats.hystats.util.HTTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Main class of the HyStats backend
 *
 * @author chloe
 * @since March 11, 2024
 */
public class HyStats {

	public static HyStats INSTANCE = null;

	public static Gson gson = new Gson();
	public static Logger logger = LoggerFactory.getLogger(HyStats.class);

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
	private String hypixelKey;

	/**
	 * The configuration stored as a map, loaded from config.json at startup
	 */
	private Map<?, ?> config;

	/**
	 * Start this instance of backend. Reads and verifies the configuration file, verifies connections to both
	 * the Hypixel and Mojang APIs, and verifies the Hypixel API key. After this, starts the service on the
	 * provided port.
	 */
	public void startup(int port) {
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
			logger.info("Using Hypixel API key " + this.hypixelKey);
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

		// Ignite the spark instance on the provided port
		logger.info("Starting service on port {}", port);
		this.spark.port(port);
		this.spark.init();

		logger.info("Started HyStats in {} seconds", ((System.currentTimeMillis() - startTime) / 1000f));
	}

	/**
	 * Shuts down this instance of the backend, stopping the service
	 */
	public void shutdown() {
		this.spark.stop();
	}

	/**
	 * Reads the configuration file into the {@code config} map
	 * @throws IOException If an exception was thrown reading the configuration file
	 */
	private void readConfig() throws IOException {
		InputStream configStream = HyStats.class.getResourceAsStream("/config.json");

		if (configStream == null) {
			logger.error("No config.json was found! Halting.");
			return;
		}

		JsonReader reader = new JsonReader(new InputStreamReader(configStream));
		this.config = gson.fromJson(reader, Map.class);

		configStream.close();
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
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return response.status() == 200;
	}

	/**
	 * Create the global HyStats instance and start it on port 8080
	 * @param args ignored
	 */
	public static void main(String[] args) {
		INSTANCE = new HyStats();
		INSTANCE.startup(8080);
	}
}
