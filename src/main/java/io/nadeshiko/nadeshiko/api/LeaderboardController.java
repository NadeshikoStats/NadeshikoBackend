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

import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.leaderboards.Leaderboard;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

/**
 * Controller for the /leaderboard endpoint of the API
 * @see Route
 * @since 0.9.0
 * @author chloe
 */
public class LeaderboardController {

    /**
     * Route provider to serve the /leaderboard endpoint of the API
     */
    public static Route serveLeaderboardEndpoint = (Request request, Response response) -> {

        // Ensure that a leaderboard was provided
        if (!request.queryParams().contains("leaderboard")) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Missing leaderboard parameter\"}";
        }

        // Ensure that the provided leaderboard is valid
        if (Leaderboard.get(request.queryParams("leaderboard")) == null) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Unknown leaderboard!\"}";
        }

        int page = request.queryParams().contains("page") ? Integer.parseInt(request.queryParams("page")) : 1;

        // Ensure that the page is valid
        if (page < 1) {
            response.status(400);
            response.type("application/json");
            return "{\"success\":false,\"cause\":\"Page must greater than zero!\"}";
        }

        response.type("application/json");
        return Nadeshiko.INSTANCE.getLeaderboardService()
            .get(Objects.requireNonNull(Leaderboard.get(request.queryParams("leaderboard"))), page);
    };
}
