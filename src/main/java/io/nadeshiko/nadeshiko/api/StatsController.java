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

package io.nadeshiko.nadeshiko.api;

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

		// Fetch the API response from the cache. If the cache doesn't already contain an up-to-date entry
		//   for this player, one will be created and stored by the cache.
		JsonObject cached = Nadeshiko.INSTANCE.getStatsCache().get(request.queryParams("name"), true).deepCopy();

		// Ensure that the response from the cache is valid
		if (cached.get("success").getAsBoolean()) {
			response.status(200);
		} else {
			response.status(cached.get("status").getAsInt());
		}

		// Log the request
		Nadeshiko.logger.info("Serving stats for {}", request.queryParams("name"));

		// Register the request with the stats service
		Nadeshiko.INSTANCE.getStatsService().registerStatsRequest(request.queryParams("name"));

		// Remove these to save bandwidth since this endpoint doesn't require them
		cached.remove("achievements_one_time");
		cached.remove("quests");

		// Return the data as provided from the cache
		response.type("application/json");
		return cached;
	};
}
