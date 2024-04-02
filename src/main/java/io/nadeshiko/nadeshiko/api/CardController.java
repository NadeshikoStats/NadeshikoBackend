package io.nadeshiko.nadeshiko.api;

import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;

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

		// Ensure a name was provided
		if (!request.queryParams().contains("name")) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing name parameter\"}";
		}

		// Ensure a game was provided
		if (!request.queryParams().contains("game")) {
			response.status(400);
			response.type("application/json");
			return "{\"success\":false,\"cause\":\"Missing game parameter\"}";
		}

		CardGame game;

		// Ensure the game provided was valid
		try {
			game = CardGame.valueOf(request.queryParams("game"));
		} catch (Exception e) {
			response.status(400);
			response.type("application/json");

			String cause = String.format("Invalid game '%s'. Valid games: %s",
				request.queryParams("game"), Arrays.toString(CardGame.values()));

			return String.format("{\"success\":false,\"cause\":\"%s\"}", cause);
		}

		Nadeshiko.logger.info("Serving {} card for {}",
			request.queryParams("game"), request.queryParams("name"));

		response.type("image/png");
		return Nadeshiko.INSTANCE.getCardsCache().get(request.queryParams("name"), game);
	};
}