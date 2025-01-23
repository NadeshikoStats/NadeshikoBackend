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

package io.nadeshiko.nadeshiko;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.MinecraftColors;
import io.nadeshiko.nadeshiko.util.hypixel.GuildLevel;
import io.nadeshiko.nadeshiko.util.hypixel.NetworkLevel;
import io.nadeshiko.nadeshiko.util.hypixel.RankHelper;
import lombok.NonNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

/**
 * Base class of all response builders ({@link io.nadeshiko.nadeshiko.skyblock.SkyBlockBuilder},
 * {@link io.nadeshiko.nadeshiko.stats.StatsBuilder}, etc.), including base methods for the Mojang and Hypixel APIs.
 *
 * @since 1.1.0
 * @author chloe
 */
public abstract class BaseBuilder {

    /**
     * The cache of player badges, updated every hour
     */
    public static JsonObject playerBadges;
    static {
        playerBadges = readPlayerBadges();
        Nadeshiko.logger.info("Loaded and cached player badges");
    }

    /**
     * The time at which the player badge cache was last updated
     */
    private static long lastCacheTime = System.currentTimeMillis();

    /**
     * Reads the badges.json file
     * @return The contents of the badge.json file, or an empty JsonObject
     */
    private static JsonObject readPlayerBadges() {
        File configFile = new File("badges.json");

        // Verify that the config file exists
        if (!configFile.exists()) {
            Nadeshiko.logger.warn("No badges.json was found! Is this intentional? Badges will not function.");
            return new JsonObject();
        }

        // Read the config file, parse it, and store it in memory
        try {
            return JsonParser.parseString(Files.readString(configFile.toPath())).getAsJsonObject();
        } catch (Exception e) {
            Nadeshiko.logger.error("Failed to read badges.json! Badges will not function.");
            return new JsonObject();
        }
    }

    protected void updateBadges() {
        // Update the badge cache if needed (5 minutes)
        if (System.currentTimeMillis() - lastCacheTime > 1000 * 60 * 5) {
            playerBadges = readPlayerBadges();
            Nadeshiko.logger.info("Loaded and updated player badges");
            lastCacheTime = System.currentTimeMillis();
        }
    }

    /**
     * Generate a response, as a JsonObject, to indicate a failure with the given cause
     * @param cause The reason for the failure - returned to the client in the response
     * @return The response, as a JsonObject
     */
    protected JsonObject error(@NonNull String cause, int status) {

        JsonObject response = new JsonObject();

        response.addProperty("success", false);
        response.addProperty("status", status);
        response.addProperty("cause", cause);

        return response;
    }

    /**
     * Fetch the Minecraft profile from PlayerDB, grabbing the
     * players UUID, properly capitalized name, and textures
     * @param name The name of the player to look up
     * @return The response from PlayerDB
     */
    protected JsonObject fetchMinecraftProfile(@NonNull String name) {
        try {
            HTTPUtil.Response response =
                HTTPUtil.get("https://playerdb.co/api/player/minecraft/" + name);

            return JsonParser.parseString(response.response()).getAsJsonObject();
        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up Minecraft profile for {}", name, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Minecraft profile for %s", name);

            return null;
        }
    }

    protected JsonObject fetchTextures(@NonNull String uuid) {
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
            Nadeshiko.logger.error("Encountered error while looking up Minecraft textures for {}", uuid, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Minecraft textures for %s", uuid);

            return null;
        }
    }

    // TODO error handling
    protected JsonObject fetchHypixelStatus(@NonNull String uuid) {
        try {
            HTTPUtil.Response response =
                HTTPUtil.get("https://api.hypixel.net/v2/status?uuid=" + uuid +
                    "&key=" + Nadeshiko.INSTANCE.getHypixelKey());

            JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();
            JsonObject session = jsonResponse.getAsJsonObject("session");
            JsonObject object = new JsonObject();

            if (session.has("online")) {
                object.addProperty("online", session.get("online").getAsBoolean());
            } else {
                object.addProperty("online", false);
            }

            if (session.has("gameType")) {
                object.addProperty("game", session.get("gameType").getAsString());
            }

            if (session.has("mode")) {
                object.addProperty("mode", session.get("mode").getAsString());
            }

            return object;

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up Hypixel status for {}", uuid, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Hypixel status for %s", uuid);

            return new JsonObject();
        }
    }

    protected JsonObject fetchHypixelGuild(@NonNull String uuid) {
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

                // Not all guilds have tag colors
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

            int guildXP = guild.has("exp") ? guild.get("exp").getAsInt() : 0;
            object.addProperty("level", GuildLevel.getExactLevel(guildXP));
            object.addProperty("members", guild.getAsJsonArray("members").size());
            object.addProperty("joined", joined);

            return object;

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up Hypixel guild for {}", uuid, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Hypixel guild for %s", uuid);

            return null;
        }
    }

    protected JsonObject fetchHypixelStats(@NonNull String uuid) {
        try {
            HTTPUtil.Response response =
                HTTPUtil.get("https://api.hypixel.net/v2/player?uuid=" + uuid +
                    "&key=" + Nadeshiko.INSTANCE.getHypixelKey());

            JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

            // If the player hasn't even been on Hypixel before, player will be null
            if (jsonResponse.has("player") && !jsonResponse.get("player").isJsonNull()) {
                return jsonResponse.get("player").getAsJsonObject();
            } else {
                return null; // The player exists but has never been on Hypixel before
            }

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up Hypixel stats for {}", uuid, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Hypixel stats for %s", uuid);

            return null;
        }
    }

    protected JsonObject buildHypixelProfile(@NonNull JsonObject playerObj) {
        try {
            JsonObject profile = new JsonObject();

            RankHelper rankHelper = new RankHelper(playerObj);
            String tag = rankHelper.getTag();

            profile.addProperty("tag", tag);
            profile.addProperty("tagged_name",
                tag.replace("]", "] ") +
                    playerObj.get("displayname").getAsString());

            // Add the first login
            // For some reason, it seems as if this field can be missing in extremely rare cases. I've only been able
            //   to observe it on "AaAkKkEeErRrAaA". More investigation is likely needed.
            if (playerObj.has("firstLogin")) {
                profile.addProperty("first_login", playerObj.get("firstLogin").getAsLong());
            } else {
                profile.addProperty("first_login", 0); // Default
            }

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
                    NetworkLevel.getCoinMultiplier(profile.get("network_level").getAsInt()));
            } else {
                profile.addProperty("network_level", 1d); // Default
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

                // Some players have a slightly differently formatted social media, for some reason...
                if (playerObj.getAsJsonObject("socialMedia").has("links")) {
                    profile.add("social_media", playerObj.getAsJsonObject(
                        "socialMedia").getAsJsonObject("links"));
                } else {
                    profile.add("social_media", playerObj.getAsJsonObject("socialMedia"));
                }


            } else {
                profile.add("social_media", new JsonObject()); // Default
            }

            return profile;

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while building Hypixel profile for {}!",
                playerObj.get("displayname").getAsString(), e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while building Hypixel profile for %s",
                playerObj.get("displayname").getAsString());

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
