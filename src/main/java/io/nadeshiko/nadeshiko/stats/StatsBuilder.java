package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.hypixel.GuildLevel;
import io.nadeshiko.nadeshiko.hypixel.NetworkLevel;
import io.nadeshiko.nadeshiko.hypixel.RankHelper;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.MinecraftColors;
import lombok.NonNull;

import java.util.Base64;
import java.util.Map;

/**
 * @since 0.0.1
 * @author chloe
 */
public class StatsBuilder {

	public JsonObject build(@NonNull String name) {

		JsonObject response = new JsonObject();
		response.addProperty("success", true);

		JsonObject textures = null;

		// If the query is a username
		if (name.length() < 30) {

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

			textures = this.fetchTextures(mojangProfile.get("id").getAsString());
		}

		// If the query is a UUID
		else {
			try {
				HTTPUtil.Response mojangResponse = HTTPUtil.
					get("https://sessionserver.mojang.com/session/minecraft/profile/" + name);
				JsonObject mojangJson = JsonParser.parseString(mojangResponse.response()).getAsJsonObject();

				// The UUID couldn't be found
				if (mojangResponse.status() == 404) {
					return error("No player by the UUID \"" + name + "\" could be found.", 404);
				}

				response.addProperty("name", mojangJson.get("name").getAsString());
				response.addProperty("uuid", mojangJson.get("id").getAsString());

				textures = this.fetchTextures(mojangJson.get("id").getAsString());
			} catch (Exception e) {
				Nadeshiko.logger.error("Encountered error while looking up Minecraft profile for {}", name);
				Nadeshiko.logger.error("Stack trace:");
			}
		}


		// Add the skin and model
		if (textures != null && textures.has("SKIN")) {
			final JsonObject skinObject = textures.getAsJsonObject("SKIN");

			if (skinObject.has("url")) {
				response.addProperty("skin", skinObject.get("url").getAsString());
			}

			// Read the metadata to get the model
			if (skinObject.has("metadata")) {
				final JsonObject metadata = skinObject.getAsJsonObject("metadata");

				if (metadata.has("model")) {
					boolean slim = metadata.get("model").getAsString().equals("slim");
					response.addProperty("slim", slim);
				}
			}
		}

		// Add the cape
		if (textures != null && textures.has("CAPE")) {
			final JsonObject capeObject = textures.getAsJsonObject("CAPE");

			if (capeObject.has("url")) {
				response.addProperty("cape", capeObject.get("url").getAsString());
			}
		}

		// Add the Hypixel status
		final JsonObject hypixelStatus = this.fetchHypixelStatus(response.get("uuid").getAsString());
		response.add("status", hypixelStatus);

		// Add the Hypixel guild
		final JsonObject hypixelGuild = this.fetchHypixelGuild(response.get("uuid").getAsString());
		response.add("guild", hypixelGuild);

		// Add the Hypixel stats
		final JsonObject hypixelStats = this.fetchHypixelStats(response.get("uuid").getAsString());
		if (hypixelStats != null) {
			response.add("profile", this.buildHypixelProfile(hypixelStats));

			// Some staff members have their stats disabled
			if (hypixelStats.has("stats")) {
				response.add("stats", hypixelStats.get("stats").getAsJsonObject());
			} else {
				response.add("stats", new JsonObject()); // Fallback if stats are off/missing
			}
		}

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

	private JsonObject fetchTextures(@NonNull String uuid) {
		try {
			HTTPUtil.Response response =
				HTTPUtil.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);

			// If the API responded OK
			if (response.status() == 200) {
				JsonObject profile = JsonParser.parseString(response.response()).getAsJsonObject();
				JsonObject outerTexturesObject = profile.getAsJsonArray("properties").get(0).getAsJsonObject();

				String texturesPropertyEncoded = outerTexturesObject.get("value").getAsString();
				String texturesPropertyDecoded = new String(Base64.getDecoder().decode(texturesPropertyEncoded));

				JsonObject texturesProperty = JsonParser.parseString(texturesPropertyDecoded).getAsJsonObject();

				return texturesProperty.getAsJsonObject("textures");
			}

			// If something went wrong, return the response, since we want to know what happened
			else {
				return JsonParser.parseString(response.response()).getAsJsonObject();
			}
		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up Minecraft textures for {}", uuid);
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}

	// TODO error handling
	private JsonObject fetchHypixelStatus(@NonNull String uuid) {
		try {
			HTTPUtil.Response response =
				HTTPUtil.get("https://api.hypixel.net/v2/status?uuid=" + uuid +
					"&key=" + Nadeshiko.INSTANCE.getHypixelKey());

			JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();
			JsonObject session = jsonResponse.getAsJsonObject("session");
			JsonObject object = new JsonObject();

			object.addProperty("online", session.get("online").getAsBoolean());

			if (session.has("gameType")) {
				object.addProperty("game", session.get("gameType").getAsString());
			}

			if (session.has("mode")) {
				object.addProperty("mode", session.get("mode").getAsString());
			}

			return object;

		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up Hypixel status for {}", uuid);
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}

	private JsonObject fetchHypixelGuild(@NonNull String uuid) {
		try {
			HTTPUtil.Response response =
				HTTPUtil.get("https://api.hypixel.net/v2/guild?player=" + uuid +
					"&key=" + Nadeshiko.INSTANCE.getHypixelKey());

			JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

			// Check to see if the player actually has a guild
			if (!jsonResponse.has("guild") || jsonResponse.get("guild").isJsonNull()) {
				return null;
			}

			JsonObject guild = jsonResponse.getAsJsonObject("guild");
			JsonObject object = new JsonObject();

			long joined = 0;
			JsonObject playerEntry;

			// Iterate over all guild members to find the requested player by UUID
			for (JsonElement element : guild.getAsJsonArray("members")) {
				JsonObject entry = (JsonObject) element;

				if (entry.get("uuid").getAsString().equals(uuid.replace("-", ""))) {
					playerEntry = entry;
					joined = playerEntry.get("joined").getAsLong();
					break;
				}
			}

			object.addProperty("name", guild.get("name").getAsString());

			// Not all guilds have tags
			if (guild.has("tag")) {

				// Not all guilds have tag colors, apparently
				if (guild.has("tagColor")) {
					object.addProperty("tag",
						String.format("%s[%s]",
							MinecraftColors.getCodeFromName(guild.get("tagColor").getAsString()),
							guild.get("tag").getAsString()
						)
					);
				} else {
					object.addProperty("tag",
						String.format("%s[%s]", "§7", guild.get("tag").getAsString()));
				}

			} else {
				object.addProperty("tag", "");
			}

			object.addProperty("level", GuildLevel.getExactLevel(guild.get("exp").getAsInt()));
			object.addProperty("members", guild.getAsJsonArray("members").size());
			object.addProperty("joined", joined);

			return object;

		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up Hypixel guild for {}", uuid);
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}

	private JsonObject fetchHypixelStats(@NonNull String uuid) {
		try {
			HTTPUtil.Response response =
				HTTPUtil.get("https://api.hypixel.net/v2/player?uuid=" + uuid +
					"&key=" + Nadeshiko.INSTANCE.getHypixelKey());

			JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();
			return jsonResponse.get("player").getAsJsonObject();

		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up Hypixel stats for {}", uuid);
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}

	private JsonObject buildHypixelProfile(@NonNull JsonObject playerObj) {
		try {
			JsonObject profile = new JsonObject();

			RankHelper rankHelper = new RankHelper(playerObj);
			String tag = rankHelper.getTag();

			profile.addProperty("tag", tag);
			profile.addProperty("tagged_name",
				tag.replace("]", "] ") +
					playerObj.get("displayname").getAsString());

			profile.addProperty("first_login", playerObj.get("firstLogin").getAsLong());

			// Add the last login
			// Players can disable the last login from their API
			if (playerObj.has("lastLogin")) {
				profile.addProperty("last_login", playerObj.get("lastLogin").getAsLong());
			} else {
				profile.addProperty("last_login", 0); // Default
			}

			// Add the network level and coin multiplier
			// Staff can disable network XP from their API
			if (playerObj.has("networkExp")) {
				profile.addProperty("network_level", NetworkLevel.getExactLevel(
					playerObj.get("networkExp").getAsLong()));
				profile.addProperty("coin_multiplier",
					NetworkLevel.getCoinMultiplier(playerObj.get("networkExp").getAsLong()));
			} else {
				profile.addProperty("network_level", 0d); // Default
				profile.addProperty("coin_multiplier", 1d); // Default
			}

			// Add the achievement points
			// Staff can disable achievement points from their API
			if (playerObj.has("achievementPoints")) {
				profile.addProperty("achievement_points", playerObj.get("achievementPoints").getAsInt());
			} else {
				profile.addProperty("achievement_points", 0); // Default
			}

			// Add karma
			// Staff can disable karma from their API
			if (playerObj.has("karma")) {
				profile.addProperty("karma", playerObj.get("karma").getAsInt());
			} else {
				profile.addProperty("karma", 0); // Default
			}

			// Add ranks gifted
			if (playerObj.has("giftingMeta")) {

				JsonObject giftingMeta = playerObj.getAsJsonObject("giftingMeta");

				// Not everyone has gifted a rank
				if (giftingMeta.has("ranksGiven")) {
					profile.addProperty("ranks_gifted",
						giftingMeta.get("ranksGiven").getAsInt());
				} else {
					profile.addProperty("ranks_gifted", 0); // Default
				}
			} else {
				profile.addProperty("ranks_gifted", 0); // Default
			}

			// Add quests completed
			if (playerObj.has("quests")) {
				profile.addProperty("quests_completed",
					countQuests(playerObj.getAsJsonObject("quests")));
			} else {
				profile.addProperty("quests_completed", 0); // Default
			}

			// Add social media
			if (playerObj.has("socialMedia")) {
				profile.add("social_media", playerObj.getAsJsonObject(
					"socialMedia").getAsJsonObject("links"));
			} else {
				profile.add("social_media", new JsonObject()); // Default
			}

			return profile;

		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while building Hypixel profile for {}!",
				playerObj.get("displayname").getAsString());
			Nadeshiko.logger.error("Stack trace:");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Count the number of completed quests a player has given the quests object from the API
	 * @param questsObject The /player/quests object as returned by the /player endpoint of the Hypixel API
	 * @return The number of completed quests the player has
	 */
	private int countQuests(@NonNull JsonObject questsObject) {

		int quests = 0;

		// Iterate over members of the JsonObject
		for (Map.Entry<String, JsonElement> entry : questsObject.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();

			if (entryObj.has("completions")) {
				quests += entryObj.getAsJsonArray("completions").size();
			}
		}

		return quests;
	}
}
