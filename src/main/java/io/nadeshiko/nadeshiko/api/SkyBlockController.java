/*
 * This file is a part of the nadeshiko project. nadeshiko is free software, licensed under the MIT license.
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
 * Controller for the /skyblock endpoint of the API
 * @see Route
 * @author brooke
 */
public class SkyBlockController {

    /**
     * Route provider to serve the /skyblock endpoint of the API
     */
    public static Route serveSkyBlockEndpoint = (Request request, Response response) -> {
        // Ensure a name was provided
        if (!request.queryParams().contains("name")) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
        }

        String profile = request.queryParams("profile");

        // Fetch the API response from the cache. If the cache doesn't already contain an up-to-date entry
        // for this player, one will be created and stored by the cache.
        JsonObject cached = Nadeshiko.INSTANCE.getSkyBlockCache().get(request.queryParams("name"), profile);

        // Ensure that the response from the cache is valid
        if (cached.get("success").getAsBoolean()) {
            response.status(200);
        } else {
            response.status(cached.get("status").getAsInt());
        }

        // Log the request
        Nadeshiko.logger.info("Serving SkyBlock data for {}", request.queryParams("name"));

        // Register the request with the stats service
        Nadeshiko.INSTANCE.getStatsService().registerSkyBlockRequest(request.queryParams("name"));

        response.type("application/json");
        return cached;
    };
} 