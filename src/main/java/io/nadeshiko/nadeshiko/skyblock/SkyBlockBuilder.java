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

package io.nadeshiko.nadeshiko.skyblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.hypixel.SkyBlockUtil;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builder for SkyBlock profile data using the official Hypixel API.
 * @since 1.1.0-master
 * @author chloe
 */
public class SkyBlockBuilder {

	/**
	 * Builds a SkyBlock profile response for a given player
	 * @param uuid The UUID of the player to look up
	 * @param profileId The optional profile ID to look up. If none is provided, the selected profile is used
	 * @return The SkyBlock profile data
	 */
	public JsonObject build(@NonNull String uuid, String profileId) {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);

		try {
			// Get the player's name from UUID
			HTTPUtil.Response playerDbResponse = HTTPUtil.get("https://playerdb.co/api/player/minecraft/" + uuid);
			if (playerDbResponse.status() != 200) {
				return error("Failed to fetch from PlayerDB");
			}
			JsonObject minecraftProfile = JsonParser.parseString(playerDbResponse.response()).getAsJsonObject();

			if (minecraftProfile == null || !minecraftProfile.has("data") ||
				minecraftProfile.get("code").getAsString().equals("minecraft.invalid_username")) {
				return error("Could not find player with UUID " + uuid);
			}
			String name = minecraftProfile.getAsJsonObject("data").getAsJsonObject("player").get("username").getAsString();
			response.addProperty("name", name);

			// Format UUID for Hypixel API (remove dashes)
			String formattedUuid = uuid.replace("-", "");

			// Fetch the player's SkyBlock profiles
			HTTPUtil.Response profilesResponse = HTTPUtil.get(
				"https://api.hypixel.net/v2/skyblock/profiles?key=" + 
				Nadeshiko.INSTANCE.getHypixelKey() + "&uuid=" + formattedUuid);
			JsonObject profiles = JsonParser.parseString(profilesResponse.response()).getAsJsonObject();

			if (!profiles.get("success").getAsBoolean()) {
				return error("Failed to fetch SkyBlock profiles: " + profiles.get("cause").getAsString());
			}

			if (!profiles.has("profiles") || profiles.get("profiles").isJsonNull()) {
				return error("Player has no SkyBlock profiles");
			}

			// Find the active profile or use the specified one
			JsonObject selectedProfile = null;
			for (JsonElement profileElement : profiles.getAsJsonArray("profiles")) {
				JsonObject profile = profileElement.getAsJsonObject();
				
				// If a specific profile was requested, use that one
				if (profileId != null && profile.get("profile_id").getAsString().equals(profileId)) {
					selectedProfile = profile;
					break;
				}
				
				// Otherwise, look for the selected profile
				if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
					selectedProfile = profile;
					break;
				}
			}

			if (selectedProfile == null) {
				return error("Could not find the specified profile");
			}

			// Build the profile data using SkyBlockUtil
			JsonObject skyblockProfile = new JsonObject();
			
			// Add basic profile info
			JsonObject profileInfo = new JsonObject();
			if (selectedProfile.has("game_mode")) {
				profileInfo.addProperty("game_mode", selectedProfile.get("game_mode").getAsString());
			}
			skyblockProfile.add("profile", profileInfo);

			// Add skills data if available
			if (selectedProfile.has("members") && selectedProfile.getAsJsonObject("members").has(formattedUuid)) {
				JsonObject memberData = selectedProfile.getAsJsonObject("members").getAsJsonObject(formattedUuid);
				
				// Add SkyBlock level
				double exactSkyBlockLevel = 0;
				double levelingExp = 0;

				if (memberData.has("leveling") && memberData.getAsJsonObject("leveling").has("experience")) {
					levelingExp = memberData.getAsJsonObject("leveling").get("experience").getAsDouble();
					exactSkyBlockLevel = levelingExp / 100;
				}

				JsonObject skyblockLevel = new JsonObject();
				skyblockLevel.addProperty("level", (int)exactSkyBlockLevel);
				skyblockLevel.addProperty("progress", exactSkyBlockLevel % 1);
				skyblockProfile.add("skyblock_level", skyblockLevel);

				// Process skills
				JsonObject skillsWrapper = new JsonObject();
				JsonObject skills = new JsonObject();
				double totalSkillLevel = 0;
				int skillCount = 0;
				
				if (memberData.has("player_data") && memberData.getAsJsonObject("player_data").has("experience")) {
					JsonObject experience = memberData.getAsJsonObject("player_data").getAsJsonObject("experience");
					for (Map.Entry<String, JsonElement> entry : experience.entrySet()) {
						if (entry.getKey().startsWith("SKILL_")) {
							String skillName = entry.getKey().substring("SKILL_".length()).toLowerCase();
							double exp = entry.getValue().getAsDouble();
							// Skip runecrafting and social skills for average
							if (!skillName.equals("runecrafting") && !skillName.equals("social")) {
								JsonObject skillData = SkyBlockUtil.expandSkill(entry.getKey(), exp, memberData, true);
								if (skillData.has("level")) {
									totalSkillLevel += skillData.get("level").getAsInt();
									skillCount++;
								}
							}
							skills.add(skillName, SkyBlockUtil.expandSkill(entry.getKey(), exp, memberData, true));
						}
					}
				}
				
				// Calculate and add skill average
				if (skillCount > 0) {
					skillsWrapper.addProperty("average", totalSkillLevel / skillCount);
				}
				skillsWrapper.add("skills", skills);
				skyblockProfile.add("skills", skillsWrapper);

				// Process slayer data
				if (memberData.has("slayer") && memberData.getAsJsonObject("slayer").has("slayer_bosses")) {
					JsonObject slayerWrapper = new JsonObject();
					JsonObject slayers = new JsonObject();
					JsonObject slayerData = memberData.getAsJsonObject("slayer").getAsJsonObject("slayer_bosses");
					for (Map.Entry<String, JsonElement> entry : slayerData.entrySet()) {
						JsonObject bossData = entry.getValue().getAsJsonObject();
						if (bossData.has("xp")) {
							JsonObject expandedSlayer = SkyBlockUtil.expandSlayer(entry.getKey(), bossData.get("xp").getAsDouble());
							JsonObject slayerLevel = new JsonObject();
							slayerLevel.addProperty("current_level", expandedSlayer.get("level").getAsInt());
							slayerLevel.addProperty("max_level", expandedSlayer.get("max_level").getAsInt());
							slayerLevel.addProperty("progress", expandedSlayer.get("progress").getAsFloat());
							
							JsonObject slayerInfo = new JsonObject();
							slayerInfo.add("level", slayerLevel);
							slayers.add(entry.getKey().toLowerCase(), slayerInfo);
						}
					}
					slayerWrapper.add("slayers", slayers);
					skyblockProfile.add("slayer", slayerWrapper);
				}

				// Process dungeon data
				if (memberData.has("dungeons")) {
					JsonObject dungeons = new JsonObject();
					JsonObject dungeonsData = memberData.getAsJsonObject("dungeons");
					
					// Process catacombs level
					if (dungeonsData.has("dungeon_types") && 
						dungeonsData.getAsJsonObject("dungeon_types").has("catacombs") &&
						dungeonsData.getAsJsonObject("dungeon_types").getAsJsonObject("catacombs").has("experience")) {
						double catacombsExp = dungeonsData.getAsJsonObject("dungeon_types")
							.getAsJsonObject("catacombs").get("experience").getAsDouble();
						double exactLevel = SkyBlockUtil.calculateCatacombs(catacombsExp);
						
						JsonObject catacombsLevel = new JsonObject();
						catacombsLevel.addProperty("level", (int)exactLevel);
						catacombsLevel.addProperty("progress", exactLevel % 1);
						catacombsLevel.addProperty("max_level", 50);
						
						JsonObject catacombs = new JsonObject();
						catacombs.add("level", catacombsLevel);
						dungeons.add("catacombs", catacombs);
					}

					// Process class levels
					if (dungeonsData.has("player_classes")) {
						JsonObject classesWrapper = new JsonObject();
						JsonObject classes = new JsonObject();
						JsonObject classData = dungeonsData.getAsJsonObject("player_classes");
						double totalClassLevel = 0;
						int classCount = 0;
						boolean allMaxed = true;

						// Calculate level for each class
						String[] dungeonClasses = {"archer", "healer", "mage", "berserk", "tank"};
						for (String className : dungeonClasses) {
							if (classData.has(className) && 
								classData.getAsJsonObject(className).has("experience")) {
								double classExp = classData.getAsJsonObject(className).get("experience").getAsDouble();
								double exactLevel = SkyBlockUtil.calculateCatacombs(classExp);
								
								JsonObject classLevel = new JsonObject();
								classLevel.addProperty("level", (int)exactLevel);
								classLevel.addProperty("progress", exactLevel % 1);
								classLevel.addProperty("max_level", 50);
								
								JsonObject classInfo = new JsonObject();
								classInfo.add("level", classLevel);
								classes.add(className, classInfo);
								
								totalClassLevel += (int)exactLevel;
								classCount++;
								allMaxed = allMaxed && (int)exactLevel >= 50;
							}
						}

						// Calculate average class level
						if (classCount > 0) {
							classes.addProperty("average_level", totalClassLevel / classCount);
							classes.addProperty("maxed", allMaxed);
						}
						
						classesWrapper.add("classes", classes);
						dungeons.add("classes", classesWrapper);
					}

					skyblockProfile.add("dungeons", dungeons);
				}

				// Add magical power stat
				if (memberData.has("accessory_bag_storage") && 
					memberData.getAsJsonObject("accessory_bag_storage").has("highest_magical_power")) {
					JsonObject accessories = new JsonObject();
					JsonObject magicalPower = new JsonObject();
					magicalPower.addProperty("total", 
						memberData.getAsJsonObject("accessory_bag_storage").get("highest_magical_power").getAsInt());
					accessories.add("magical_power", magicalPower);
					skyblockProfile.add("accessories", accessories);
				}

				// Add purse balance
				if (memberData.has("currencies") && 
					memberData.getAsJsonObject("currencies").has("coin_purse")) {
					double purseBalance = memberData.getAsJsonObject("currencies").get("coin_purse").getAsDouble();
					skyblockProfile.addProperty("purse", purseBalance);
				} else {
					skyblockProfile.addProperty("purse", 0.0);
				}

				// Add fairy souls
				if (memberData.has("fairy_soul") && 
					memberData.getAsJsonObject("fairy_soul").has("total_collected")) {
					skyblockProfile.addProperty("fairy_souls", 
						memberData.getAsJsonObject("fairy_soul").get("total_collected").getAsInt());
				} else {
					skyblockProfile.addProperty("fairy_souls", 0);
				}
			}

			// Add bank balance (from root profile data)
			if (selectedProfile.has("banking") && 
				selectedProfile.getAsJsonObject("banking").has("balance")) {
				skyblockProfile.addProperty("bank", 
					selectedProfile.getAsJsonObject("banking").get("balance").getAsDouble());
			} else {
				skyblockProfile.addProperty("bank", 0.0);
			}

			response.add("skyblock_profile", skyblockProfile);
			return response;

		} catch (Exception e) {
			Nadeshiko.logger.error("Failed to build SkyBlock profile for {}", uuid, e);
			return error("An error occurred while building the profile");
		}
	}

	private JsonObject error(String cause) {
		JsonObject response = new JsonObject();
		response.addProperty("success", false);
		response.addProperty("cause", cause);
		response.addProperty("status", 400);  // Add default status code for errors
		return response;
	}
}
