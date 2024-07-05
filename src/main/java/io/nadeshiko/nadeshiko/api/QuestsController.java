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
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;

/**
 * Controller for the /quests endpoint of the API
 * @see Route
 * @since 0.7.0
 * @author chloe
 */
public class QuestsController {

	/**
     * The cached global quests object, documented at
	 * <a href="https://api.hypixel.net/#tag/Resources/paths/~1v2~1resources~1quests/get">the API documentation</a>
     */
	private static JsonObject globalQuests;
    static {
        try {
	        globalQuests = JsonParser.parseString(HTTPUtil.
	            get("https://api.hypixel.net/v2/resources/quests").response()).getAsJsonObject();
			Nadeshiko.logger.info("Fetched and cached global quests data from Hypixel!");
        } catch (IOException e) {
            Nadeshiko.logger.error("Failed to fetch global quests!", e);
        }
    }

    /**
	 * Route provider to serve the /quests endpoint of the API
	 */
	public static Route serveQuestsEndpoint = (Request request, Response response) -> {

		// Ensure a name was provided
		if (!request.queryParams().contains("name")) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
		}

		// Fetch the API response from the cache. If the cache doesn't already contain an up-to-date entry
		//   for this player, one will be created and stored by the cache.
		JsonObject cached = Nadeshiko.INSTANCE.getStatsCache().get(request.queryParams("name"), true);

		// Ensure that the response from the cache is valid
		if (cached.get("success").getAsBoolean()) {
			response.status(200);
		} else {
			response.status(cached.get("status").getAsInt());

			// Remove the bad response from the cache, forcing it to be reattempted on the next request
			cached.remove("status");
		}

		// Log the request
		Nadeshiko.logger.info("Serving achievements for {}", request.queryParams("name"));

		JsonObject responseJson = new JsonObject();
		responseJson.addProperty("success", true);
		responseJson.add("global", globalQuests);

		JsonObject player = new JsonObject();
		player.add("profile", cached.getAsJsonObject("profile"));
		player.add("quests", cached.getAsJsonObject("quests"));
		responseJson.add("player", player);

		response.type("application/json");
		return responseJson;
	};
}