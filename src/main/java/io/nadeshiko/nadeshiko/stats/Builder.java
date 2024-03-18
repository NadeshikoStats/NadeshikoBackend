package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import lombok.NonNull;

/**
 * @author chloe
 */
public class Builder {

	public JsonObject build(@NonNull String name) {

		JsonObject response = new JsonObject();
		response.addProperty("success", true);

		final JsonObject mojangProfile = this.fetchMojangProfile(name);

		// If the Mojang profile was null, the player couldn't be found
		if (mojangProfile == null) {
			return error("No player by the name \"" + name + "\" could be found.", 404);
		}

		// If something else with the Mojang request went wrong
		else if (mojangProfile.has("errorMessage")) {
			return error(mojangProfile.get("errorMessage").getAsString(), 520);
		}

		response.addProperty("name", mojangProfile.get("name").getAsString());
		response.addProperty("uuid", mojangProfile.get("id").getAsString());

		return response;
	}

	/**
	 * Generate a response, as a JsonObject, to indicate a failure with the given cause
	 * @param cause The reason for the failure - returned to the client in the response
	 * @return The response, as a JsonObject
	 */
	private JsonObject error(@NonNull String cause, int status) {

		JsonObject response = new JsonObject();

		response.addProperty("success", false);
		response.addProperty("status", status);
		response.addProperty("cause", cause);

		return response;
	}

	/**
	 * Fetch the Mojang profile from the {@code api.mojang.com/users/profiles/minecraft/} endpoint, grabbing the
	 * players UUID and properly capitalized name
	 * @param name The name of the player to look up
	 * @return The response from the Mojang API
	 */
	private JsonObject fetchMojangProfile(@NonNull String name) {
		try {
			HTTPUtil.Response response =
				HTTPUtil.get("https://api.mojang.com/users/profiles/minecraft/" + name);

			// If the API responded OK
			if (response.status() == 200) {
				return JsonParser.parseString(response.response()).getAsJsonObject();
			}

			// If the profile wasn't found
			else if (response.status() == 404) {
				return null;
			}

			// If something else went wrong, return the response, since we want to know what happened
			else {
				return JsonParser.parseString(response.response()).getAsJsonObject();
			}
		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up Minecraft profile for {}", name);
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}
}
