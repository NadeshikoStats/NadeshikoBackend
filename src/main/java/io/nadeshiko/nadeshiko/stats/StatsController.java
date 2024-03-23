package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonObject;
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
			return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
		}

		JsonObject cached = Nadeshiko.cache.get(request.queryParams("name"));

		if (cached.get("success").getAsBoolean()) {
			response.status(200);
		} else {
			response.status(cached.get("status").getAsInt());
			cached.remove("status");
		}

		response.type("application/json");
		return cached;
	};
}
