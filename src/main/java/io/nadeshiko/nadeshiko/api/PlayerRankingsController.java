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

import io.nadeshiko.nadeshiko.Nadeshiko;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A controller that provides player leaderboard rankings. Currently disabled
 * @see Route
 * @since 1.1.9
 */
public class PlayerRankingsController {

    /**
     * /rankings The endpoint that provides player leaderboard rankings
     */
    public static Route servePlayerRankingsEndpoint = (Request request, Response response) -> {
        // Check if the uuid parameter is provided
        if (!request.queryParams().contains("uuid")) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Missing uuid parameter\"}";
        }

        String uuid = request.queryParams("uuid");

        // Log the request
        Nadeshiko.logger.info("Serving rankings for player {}", uuid);

        // Get rankings from the leaderboard service
        response.type("application/json");
        return Nadeshiko.INSTANCE.getLeaderboardService().getPlayerRankings(uuid);
    };
}   