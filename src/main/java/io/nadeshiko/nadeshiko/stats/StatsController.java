package io.nadeshiko.nadeshiko.stats;

import io.nadeshiko.nadeshiko.Nadeshiko;
import spark.Request;
import spark.Response;
import spark.Route;

public class StatsController {
	public static Route serveStatsEndpoint = (Request request, Response response) -> {

		// Ensure a name was provided
		if (!request.queryParams().contains("name")) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"reason\":\"Missing name parameter\"}";
		}

		response.status(200);
		response.type("application/json");
		return Nadeshiko.cache.get(request.queryParams("name"));
	};
}
