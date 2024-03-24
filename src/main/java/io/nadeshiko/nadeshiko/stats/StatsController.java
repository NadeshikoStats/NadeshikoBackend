package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.Nadeshiko;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Controller for the /stats endpoint of the API
 * @see Route
 * @author chloe
 */
public class StatsController {

	/**
	 * Route provider to serve the /stats endpoint of the API
	 */
	public static Route serveStatsEndpoint = (Request request, Response response) -> {

		// Ensure a name was provided
		if (!request.queryParams().contains("name")) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
		}

		Nadeshiko.logger.info("Serving stats for {}", request.queryParams("name"));

		// Fetch the API response from the cache. If the cache doesn't already contain an up-to-date entry
		//   for this player, one will be created and stored by the cache.
		JsonObject cached = Nadeshiko.cache.get(request.queryParams("name"));

		// Ensure that the response from the cache is valid
		if (cached.get("success").getAsBoolean()) {
			response.status(200);
		} else {
			response.status(cached.get("status").getAsInt());

			// Remove the bad response from the cache, forcing it to be reattempted on the next request
			cached.remove("status");
		}

		response.type("application/json");
		return cached;
	};
}
