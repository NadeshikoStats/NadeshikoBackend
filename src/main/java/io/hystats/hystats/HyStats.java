package io.hystats.hystats;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HyStats {

	public static HyStats INSTANCE = null;

	public static Gson gson = new Gson();
	public static Logger logger = LoggerFactory.getLogger(HyStats.class);

	private long startTime;

	private String hypixelKey;
	private Map<?, ?> config;

	public void startup() {
		this.startTime = System.currentTimeMillis();

		// Read config.json
		try {
			InputStream configStream = HyStats.class.getResourceAsStream("/config.json");

			if (configStream == null) {
				logger.error("No config.json was found! Halting.");
				return;
			}

			JsonReader reader = new JsonReader(new InputStreamReader(configStream));
			this.config = gson.fromJson(reader, Map.class);

			this.hypixelKey = (String) this.config.get("hypixel_key");

			if (this.hypixelKey == null) {
				logger.error("No API key was provided in the config! Halting.");
				return;
			} else {
				logger.info("Using Hypixel API key " + this.hypixelKey);
			}

			configStream.close();
		} catch (IOException e) {
			logger.error("Failed to read configuration file! Halting.");
			logger.error(e.toString());
			return;
		}

		logger.info("Starting service on port 8080");
		Spark.port(8080);
		Spark.init();

		logger.info("Started HyStats in {} seconds", ((System.currentTimeMillis() - startTime) / 1000f));
	}

	public void shutdown() {

	}

	private boolean testHypixel() {
		return true;
	}

	private boolean testMojang() {
		return true;
	}

	public static void main(String[] args) {
		INSTANCE = new HyStats();
		INSTANCE.startup();
	}
}
