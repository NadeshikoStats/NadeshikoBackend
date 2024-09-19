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
 * Controller for the /guild endpoint of the API
 * @see Route
 * @since 0.6.0
 * @author chloe
 */
public class GuildController {

    /**
     * Route provider to serve the /guild endpoint of the API
     */
    public static Route serveGuildEndpoint = (Request request, Response response) -> {

        // Ensure that a guild was provided
        if (!request.queryParams().contains("name") && !request.queryParams().contains("player")) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Missing name/player parameter\"}";
        }

        // Fetch the API response from the cache. If the cache doesn't already contain an up-to-date entry
        //   for this guild, one will be created and stored by the cache.
        JsonObject cached;
        if (request.queryParams().contains("name")) {
            cached = Nadeshiko.INSTANCE.getGuildCache().getByName(request.queryParams("name"));
        } else {
            cached = Nadeshiko.INSTANCE.getGuildCache().getByPlayer(request.queryParams("player"));
        }

        // Ensure that the response from the cache is valid
        if (cached.get("success").getAsBoolean()) {
            response.status(200);

            // Register the request with the stats service
            Nadeshiko.INSTANCE.getStatsService().registerGuildRequest(cached.get("name").getAsString());
        } else {
            response.status(cached.get("status").getAsInt());

            // Remove the bad response from the cache, forcing it to be reattempted on the next request
            cached.remove("status");
        }

        // Log the request
//        Nadeshiko.logger.info("Serving stats for guild {}", cached.get("name").getAsString());

        // Return the data as provided from the cache
        response.type("application/json");
        return cached;
    };
}
