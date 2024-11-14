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

package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.BaseBuilder;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.HTTPUtil;
import io.nadeshiko.nadeshiko.util.MinecraftColors;
import io.nadeshiko.nadeshiko.util.hypixel.GuildLevel;
import lombok.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chloe
 * @since 0.6.0
 */
public class GuildBuilder extends BaseBuilder {

    public JsonObject buildFromName(@NonNull String name) {
        return this.build(this.fetchGuildFromName(name));
    }

    public JsonObject buildFromPlayer(@NonNull String name) {
        return this.build(this.fetchGuildFromPlayer(name));
    }

    private JsonObject build(JsonObject guildData) {

        if (guildData == null) {
            return error("This player is not in a guild!", 404);
        }

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("name", guildData.get("name").getAsString());

        // Not all guilds have a description
        if (guildData.has("description")) {
            response.addProperty("description", guildData.get("description").getAsString());
        } else {
            response.addProperty("description", "");
        }

        // Not all guilds have tags
        if (guildData.has("tag")) {

            // Not all guilds have tag colors
            if (guildData.has("tagColor")) {
                response.addProperty("tag", MinecraftColors.getCodeFromName(guildData.get("tagColor")
                    .getAsString()) + "[" + guildData.get("tag").getAsString() + "]");
            } else { // Fallback to gray tag
                response.addProperty("tag", "§7[" + guildData.get("tag").getAsString() + "]");
            }

        } else {
            response.addProperty("tag", "");
        }

        // Not all guilds have any XP
        if (guildData.has("exp")) {
            response.addProperty("level", GuildLevel.getExactLevel(guildData.get("exp").getAsInt()));
        } else {
            response.addProperty("level", 0);
        }

        response.addProperty("created", guildData.get("created").getAsLong());
        response.add("preferred_games", guildData.getAsJsonArray("preferredGames"));
        response.add("achievements", guildData.getAsJsonObject("achievements"));
        response.add("ranks", guildData.getAsJsonArray("ranks"));

        JsonArray members = new JsonArray();

        ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Lock lock = new ReentrantLock();

        for (JsonElement rawPlayer : guildData.getAsJsonArray("members")) {
            service.submit(() -> {
                JsonObject player = rawPlayer.getAsJsonObject();
                JsonObject playerStats = Nadeshiko.INSTANCE.getStatsCache().get(player.get("uuid").getAsString(), false);

                lock.lock(); // Prevent multiple writes to the members array at once
                player.addProperty("badge", playerStats.get("badge").getAsString());
                player.add("profile", playerStats.getAsJsonObject("profile"));
                members.add(player);
                lock.unlock();
            });
        }

        try {
            service.shutdown();
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                return error("Timed out", 500);
            }
            response.add("members", members);
        } catch (Exception e) {
            return error("Internal threading error", 500);
        }

        return response;
    }

    /**
     * Fetches a Hypixel guild by name
     * @param name The name of the guild to look up
     * @return The guild, or {@code null} if none exists
     */
    private JsonObject fetchGuildFromName(@NonNull String name) {
        try {
            HTTPUtil.Response response =
                HTTPUtil.get("https://api.hypixel.net/v2/guild?name=" + name +
                    "&key=" + Nadeshiko.INSTANCE.getHypixelKey());

            JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

            // Make sure the guild exists
            if (!jsonResponse.has("guild") || jsonResponse.get("guild").isJsonNull()) {
                return null; // the guild does not exist
            }

            return jsonResponse.getAsJsonObject("guild");

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up Hypixel guild \"{}\"", name, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up Hypixel guild \"%s\"", name);

            return null;
        }
    }

    /**
     * Fetches the Hypixel guild of a given player
     * @param player The username or UUID of the player to look up
     * @return The player's guild, or {@code null} if none exists
     */
    public JsonObject fetchGuildFromPlayer(@NonNull String player) {
        try {
            String uuid = Nadeshiko.INSTANCE.getStatsCache().get(player, false).get("uuid").getAsString();
            HTTPUtil.Response response =
                HTTPUtil.get("https://api.hypixel.net/v2/guild?player=" + uuid +
                    "&key=" + Nadeshiko.INSTANCE.getHypixelKey());

            JsonObject jsonResponse = JsonParser.parseString(response.response()).getAsJsonObject();

            // Make sure the player has a guild
            if (!jsonResponse.has("guild") || jsonResponse.get("guild").isJsonNull()) {
                return null; // the player has no guild
            }

            return jsonResponse.getAsJsonObject("guild");

        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error while looking up {}'s Hypixel guild", player, e);
            Nadeshiko.INSTANCE.getDiscordMonitor().alertException(e,
                "Encountered error while looking up %s's Hypixel guild", player);

            return null;
        }
    }
}
