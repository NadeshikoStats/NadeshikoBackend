package io.nadeshiko.nadeshiko.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.Base64;

/**
 * Controller for the /card endpoint of the API
 * @see Route
 * @since 0.1.0
 * @author chloe
 */
public class CardController {

	/**
	 * Route provider to serve the /card endpoint of the API
	 */
	public static Route serveCardEndpoint = (Request request, Response response) -> {

		// Ensure data was provided
		if (request.params(":data") == null || request.params(":data").isEmpty()) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing card data\"}";
		}

		String decodedData = new String(Base64.getUrlDecoder().decode(request.params("data")));
		JsonObject data = JsonParser.parseString(decodedData).getAsJsonObject();

		// Ensure a name was provided
		if (!data.has("name") || data.get("name").isJsonNull()) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
		}

		// Ensure a game was provided
		if (!data.has("game") || data.get("game").isJsonNull()) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing game parameter\"}";
		}

		// Ensure a size was provided
		if (!data.has("size") || data.get("size").isJsonNull()) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing size parameter\"}";
		}

		CardGame game;

		// Ensure the game provided was valid
		try {
			game = CardGame.valueOf(data.get("game").getAsString());
		} catch (Exception e) {
			response.status(400);
			response.type("application/json");

			String cause = String.format("Invalid game '%s'. Valid games: %s",
				request.queryParams("game"), Arrays.toString(CardGame.values()));

			return String.format("{\"success\":false,\"cause\":\"%s\"}", cause);
		}

		Nadeshiko.logger.info("Serving {} card for {}",
			data.get("game"), data.get("name"));

		response.type("image/png");
		return Nadeshiko.INSTANCE.getCardsCache().get(data, game);
	};
}