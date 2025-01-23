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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import io.nadeshiko.nadeshiko.BaseBuilder;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.hypixel.SkyBlockUtil;
import io.nadeshiko.networth.item.Item;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Here be bad code.
 * @since 1.1.0
 * @author chloe
 */
public class SkyBlockBuilder extends BaseBuilder {

	private final Nbt nbt = new Nbt();

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

			boolean hasRank = !response.getAsJsonObject("profile").get("tag").getAsString().isEmpty();
			response.add("skyblock_profile", this.getProfile(playerData.get("id").getAsString(), profile, hasRank));
		}

		return response;
	}

	private JsonArray getProfiles(String uuid) {
		try {
			HTTPUtil.Response response = HTTPUtil.get("https://api.hypixel.net/v2/skyblock/profiles?uuid=" + uuid +
				"&key=" + Nadeshiko.INSTANCE.getHypixelKey());
			JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

			if (jsonResponse.get("success").getAsBoolean()) {
				return jsonResponse.getAsJsonArray("profiles");
			} else {
				Nadeshiko.logger.error("Failed to request SkyBlock profiles for {}", uuid);
				return new JsonArray();
			}
		} catch (Exception e) {
            Nadeshiko.logger.error("Failed to fetch SkyBlock profiles for {}", uuid, e);
			return new JsonArray();
		}
	}

	private JsonArray decodeInventory(@NonNull String data) {
		byte[] decodedData = Base64.getDecoder().decode(data);
		JsonObject inventory;
		JsonArray decodedInventory = new JsonArray();

		// Attempt to decode inventory data
		try {
			GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decodedData));
			CompoundTag parsedNbt = this.nbt.fromByteArray(gzipInputStream.readAllBytes());
			inventory = parsedNbt.toJson(0, this.nbt.getTypeRegistry());
		} catch (Exception e) {
			Nadeshiko.logger.error("Failed to decode inventory data!", e);
			return new JsonArray();
		}

		// Clean up inventory data
		for (JsonElement element : inventory.getAsJsonObject("value").getAsJsonObject("i").getAsJsonArray("value")) {
			JsonObject oldItem = element.getAsJsonObject().getAsJsonObject("value");
			JsonObject newItem = new JsonObject();

			// Check if the slot is air first
			if (oldItem.isEmpty()) {
				decodedInventory.add(newItem);
				continue;
			}

			JsonObject tag = oldItem.getAsJsonObject("tag").getAsJsonObject("value");
			newItem.addProperty("id", oldItem.getAsJsonObject("id").get("value").getAsInt());
			newItem.addProperty("count", oldItem.getAsJsonObject("Count").get("value").getAsInt());
			newItem.addProperty("damage", oldItem.getAsJsonObject("Damage").get("value").getAsInt());
			if (tag.has("display")) {
				newItem.addProperty("name", tag.getAsJsonObject("display").getAsJsonObject("value")
					.getAsJsonObject("Name").get("value").getAsString());
				newItem.add("lore", tag.getAsJsonObject("display").getAsJsonObject("value")
					.getAsJsonObject("Lore").getAsJsonArray("value"));
			}
			if (tag.has("ExtraAttributes")) {
				newItem.add("attributes", tag.getAsJsonObject("ExtraAttributes").getAsJsonObject("value"));
			}

			// Add item value
			if (newItem.has("attributes")) {
				Item item = Item.fromAttributes(newItem.get("count").getAsInt(), newItem.get("attributes").getAsJsonObject());
				newItem.addProperty("value", Nadeshiko.INSTANCE.getNetworthCalculator().calculateItem(item));
			}

			// Add lore
			JsonArray lore = new JsonArray();
			for (JsonElement jsonElement : tag.getAsJsonObject("display").getAsJsonObject("value")
					.getAsJsonObject("Lore").getAsJsonArray("value")) {
				lore.add(jsonElement.getAsJsonObject().get("value").getAsString());
			}
			newItem.add("lore", lore);

			// Add rarity
			String lastLine = lore.get(lore.size() - 1).getAsString();
			// This is done backwards because of false contains matches with common/uncommon and special/very special.
			if (lastLine.contains("ADMIN")) {
				newItem.addProperty("rarity", "ADMIN");
			} else if (lastLine.contains("ULTIMATE")) {
				newItem.addProperty("rarity", "ULTIMATE");
			} else if (lastLine.contains("VERY SPECIAL")) {
				newItem.addProperty("rarity", "VERY_SPECIAL");
			} else if (lastLine.contains("SPECIAL")) {
				newItem.addProperty("rarity", "SPECIAL");
			} else if (lastLine.contains("DIVINE")) {
				newItem.addProperty("rarity", "DIVINE");
			} else if (lastLine.contains("MYTHIC")) {
				newItem.addProperty("rarity", "MYTHIC");
			} else if (lastLine.contains("LEGENDARY")) {
				newItem.addProperty("rarity", "LEGENDARY");
			} else if (lastLine.contains("EPIC")) {
				newItem.addProperty("rarity", "EPIC");
			} else if (lastLine.contains("RARE")) {
				newItem.addProperty("rarity", "RARE");
			} else if (lastLine.contains("UNCOMMON")) {
				newItem.addProperty("rarity", "UNCOMMON");
			} else if (lastLine.contains("COMMON")) {
				newItem.addProperty("rarity", "COMMON");
			} else {
				newItem.addProperty("rarity", "NONE");
			}

			decodedInventory.add(newItem);
		}

		return decodedInventory;
	}

	private JsonObject cleanupProfile(@NonNull JsonObject profile, @NonNull String uuid, boolean hasRank) {

		// Add networth
		try {
			profile.add("networth", Nadeshiko.INSTANCE.getNetworthCalculator().calculatePlayer(profile, uuid).serialize());
		} catch (Exception e) {
			Nadeshiko.logger.error("Failed to calculate networth for {}!", uuid, e);
		}

		// Remove other members and flatten the JSON object
		JsonObject members = profile.getAsJsonObject("members");
		JsonObject memberData = members.getAsJsonObject(uuid.replace("-", ""));
		if (memberData != null) {
			memberData.entrySet().forEach(e -> profile.add(e.getKey(), e.getValue()));
		}
		profile.remove("members");

		// Clean up skills
		JsonObject skills = new JsonObject();
		profile.getAsJsonObject("player_data").getAsJsonObject("experience").entrySet().forEach(e -> {
			String skillName = e.getKey().toLowerCase().substring("SKILL_".length());
			skills.add(skillName, SkyBlockUtil.expandSkill(e.getKey(), e.getValue().getAsDouble(), profile, hasRank));
		});

		// Remove old skills and add our new ones
		profile.getAsJsonObject("player_data").remove("experience");
		profile.add("skills", skills);

		// Add dungeons level
		JsonObject catacombs = profile.getAsJsonObject("dungeons").getAsJsonObject("dungeon_types")
			.getAsJsonObject("catacombs");
		double catacombsLevel = SkyBlockUtil.calculateCatacombs(catacombs.get("experience").getAsDouble());
		catacombs.addProperty("exact_level", catacombsLevel);
		catacombs.addProperty("level", (int) catacombsLevel);
		catacombs.addProperty("progress", catacombsLevel % 1);

		// Add class levels
		JsonObject classes = profile.getAsJsonObject("dungeons").getAsJsonObject("player_classes");
		List<Double> classLevels = new ArrayList<>();
		classes.entrySet().forEach(e -> {
			double exp = e.getValue().getAsJsonObject().get("experience").getAsDouble();
			double classLevel = SkyBlockUtil.calculateCatacombs(exp);
			classLevels.add(classLevel);

			e.getValue().getAsJsonObject().addProperty("exact_level", classLevel);
			e.getValue().getAsJsonObject().addProperty("level", (int) classLevel);
			e.getValue().getAsJsonObject().addProperty("progress", classLevel % 1);
		});

		// Add class average
		double classAverage = 0;
		for (double classLevel : classLevels) {
			classAverage += classLevel;
		}
		classAverage /= classLevels.size();
		classes.addProperty("average", classAverage);

		// Clean up slayers
		JsonObject slayerBosses = profile.getAsJsonObject("slayer").getAsJsonObject("slayer_bosses");
		slayerBosses.entrySet().forEach(e -> {
			JsonObject slayer = e.getValue().getAsJsonObject();
			double exp = slayer.has("xp") ? slayer.get("xp").getAsLong() : 0;

			slayer.add("level", SkyBlockUtil.expandSlayer(e.getKey(), exp));
			slayer.remove("xp");
		});

		// Decode per-player inventories
		JsonObject inventories = profile.getAsJsonObject("inventory");
		inventories.add("inv_contents", this.decodeInventory(
			inventories.getAsJsonObject("inv_contents").get("data").getAsString()
		));
		inventories.add("ender_chest_contents", this.decodeInventory(
			inventories.getAsJsonObject("ender_chest_contents").get("data").getAsString()
		));
		inventories.add("inv_armor", this.decodeInventory(
			inventories.getAsJsonObject("inv_armor").get("data").getAsString()
		));
		inventories.add("equipment_contents", this.decodeInventory(
			inventories.getAsJsonObject("equipment_contents").get("data").getAsString()
		));
		inventories.add("personal_vault_contents", this.decodeInventory(
			inventories.getAsJsonObject("personal_vault_contents").get("data").getAsString()
		));
		inventories.add("wardrobe_contents", this.decodeInventory(
			inventories.getAsJsonObject("wardrobe_contents").get("data").getAsString()
		));
		JsonObject backpacks = inventories.getAsJsonObject("backpack_contents");
		backpacks.entrySet().forEach(e -> backpacks.add(e.getKey(),
			this.decodeInventory(e.getValue().getAsJsonObject().get("data").getAsString())));
		inventories.add("backpack_contents", backpacks);

		JsonObject bags = inventories.getAsJsonObject("bag_contents");
		bags.add("potion_bag", this.decodeInventory(
			bags.getAsJsonObject("potion_bag").get("data").getAsString()
		));
		bags.add("talisman_bag", this.decodeInventory(
			bags.getAsJsonObject("talisman_bag").get("data").getAsString()
		));
		bags.add("fishing_bag", this.decodeInventory(
			bags.getAsJsonObject("fishing_bag").get("data").getAsString()
		));
		bags.add("sacks_bag", this.decodeInventory(
			bags.getAsJsonObject("sacks_bag").get("data").getAsString()
		));
		bags.add("quiver", this.decodeInventory(
			bags.getAsJsonObject("quiver").get("data").getAsString()
		));
		inventories.add("bag_contents", bags);

		members.add("inventory", inventories);

		// Decode shared inventories
		JsonObject sharedInventories = profile.getAsJsonObject("shared_inventory");
		sharedInventories.add("candy_inventory_contents", this.decodeInventory(
			sharedInventories.getAsJsonObject("candy_inventory_contents").get("data").getAsString()
		));
		sharedInventories.add("carnival_mask_inventory_contents", this.decodeInventory(
			sharedInventories.getAsJsonObject("carnival_mask_inventory_contents").get("data").getAsString()
		));

		// Calculate and add MP
		int mp = 0;

		// MP from accessories
		for (JsonElement element : bags.get("talisman_bag").getAsJsonArray()) {
			if (element.getAsJsonObject().isEmpty()) {
				continue;
			}

			mp += SkyBlockUtil.calculateAccessoryMp(element.getAsJsonObject());

			// Special case: for Abicases, add one MP for every two contacts the player has
			String id = element.getAsJsonObject().getAsJsonObject("attributes").getAsJsonObject("id").get("value").getAsString();
			if (id.equals("ABICASE")) {

				// Number of contacts
				JsonArray contacts = profile.getAsJsonObject("nether_island_player_data")
					.getAsJsonObject("abiphone").getAsJsonArray("active_contacts");

				mp += (int) (contacts.size() / 2d);
			}
		}

		// MP from consuming a Rift Prism
		JsonObject riftAccess = profile.getAsJsonObject("rift").getAsJsonObject("access");
		if (riftAccess.has("consumed_prism") && riftAccess.get("consumed_prism").getAsBoolean()) {
			mp += 11;
		}

		profile.getAsJsonObject("player_stats").addProperty("magical_power", mp);

		return profile;
	}

	/**
	 * Fetches a SkyBlock profile
	 * @param uuid The UUID of the player to lookup
	 * @param profileId The optional UUID of the profile to lookup. If none is provided, the player's selected profile
	 *                is used instead.
	 * @param hasRank Whether the player being looked up has a rank on Hypixel or not
	 * @return The SkyBlock profile requested, or {@code null} if something went wrong.
	 */
	private JsonObject getProfile(@NonNull String uuid, String profileId, boolean hasRank) {
		JsonArray profiles = this.getProfiles(uuid);

		// Iterate over profiles
		for (JsonElement element : profiles) {
			JsonObject profile = element.getAsJsonObject();

			// If no profile ID was provided, return the selected profile
			if (profileId == null && profile.has("selected") && profile.get("selected").getAsBoolean()) {
				return this.cleanupProfile(profile, uuid, hasRank);
			}

			// If a profile ID was provided, return that profile
			else if (profile.has("profile_id") && profile.get("profile_id").getAsString().equals(profileId)) {
				return this.cleanupProfile(profile, uuid, hasRank);
			}
		}

		Nadeshiko.logger.warn("Attempted to lookup invalid profile \"{}\" for {}!", profileId, uuid);
		return null; // Profile doesn't exist
	}
}
