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

package io.nadeshiko.nadeshiko.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.BaseBuilder;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import lombok.NonNull;

import java.util.Base64;

/**
 * @since 1.1.0
 * @author chloe
 */
public class SkyBlockBuilder extends BaseBuilder {

	public JsonObject build(@NonNull String name, String profile) {

		JsonObject response = new JsonObject();
		response.addProperty("success", true);

		JsonObject textures;

		final JsonObject minecraftProfile = this.fetchMinecraftProfile(name);

		// Ensure the request succeeded
		if (minecraftProfile == null) {
			return error("Couldn't fetch data from PlayerDB!", 500);
		}

		// If the Mojang profile was null, the player couldn't be found
		if (minecraftProfile.get("code").getAsString().equals("minecraft.invalid_username")) {
			return error("No player by the name \"" + name + "\" could be found.", 404);
		}

		JsonObject playerData = minecraftProfile.getAsJsonObject("data").getAsJsonObject("player");
		response.addProperty("name", playerData.get("username").getAsString());
		response.addProperty("uuid", playerData.get("id").getAsString());

		// Add badge
		if (playerBadges.has(playerData.get("id").getAsString())) {
			response.addProperty("badge", playerBadges.get(playerData.get("id").getAsString()).getAsString());
		} else {
			response.addProperty("badge", "NONE");
		}

		textures = this.fetchTextures(playerData.get("id").getAsString());

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

		// Add cape
		try {
			// Try OF first
			HTTPUtil.RawResponse ofResponse = HTTPUtil.getRaw("http://s.optifine.net/capes/" +
				response.get("name").getAsString() + ".png");

			// Check if the cape exists
			if (ofResponse.status() == 200) {
				response.addProperty("cape", Base64.getEncoder().encodeToString(ofResponse.response()));
			}

			// Add vanilla cape, if it exists
			else if (textures != null && textures.has("CAPE")) {
				final JsonObject capeObject = textures.getAsJsonObject("CAPE");

				if (capeObject.has("url")) {
					HTTPUtil.RawResponse mojangResponse = HTTPUtil.getRaw(capeObject.get("url").getAsString());
					response.addProperty("cape", Base64.getEncoder().encodeToString(mojangResponse.response()));
				}
			}

			// Final fallback
			else {
				response.addProperty("cape", "");
			}
		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error while looking up cape for {}",
				response.get("name").getAsString(), e);
			Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
				"Encountered error while looking up cape for %s", response.get("name").getAsString());
		}

		// Add the Hypixel status
		final JsonObject hypixelStatus = this.fetchHypixelStatus(response.get("uuid").getAsString());
		response.add("status", hypixelStatus);

		// Add the Hypixel guild
		final JsonObject hypixelGuild = this.fetchHypixelGuild(response.get("uuid").getAsString());
		response.add("guild", hypixelGuild);

		// Add the stats
		final JsonObject hypixelStats = this.fetchHypixelStats(response.get("uuid").getAsString());
		if (hypixelStats != null) { // Null if the player has no stats (never logged in)
			response.add("profile", this.buildHypixelProfile(hypixelStats));


		}

		return response;
	}

	private JsonArray getProfiles(String uuid) {
		return null;
	}

	/**
	 * Fetches a SkyBlock profile
	 * @param uuid The UUID of the player to lookup
	 * @param profile The optional UUID of the profile to lookup. If none is provided, the player's selected profile
	 *                is used instead.
	 * @return The SkyBlock profile requested, or {@code null} if something went wrong.
	 */
	private JsonObject getProfile(@NonNull String uuid, String profile) {
		JsonArray profiles = this.getProfiles(uuid);

		return null; // todo
	}
}
