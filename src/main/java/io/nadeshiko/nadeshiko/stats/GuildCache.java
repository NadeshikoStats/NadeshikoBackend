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

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.api.GuildController;
import io.nadeshiko.nadeshiko.util.Cache;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Simple cache implementation to save API responses for an hour before invalidating them.
 * <p>
 *
 * The primary purpose of the cache is to reduce the load on the Hypixel and Mojang APIs, as well as saving time
 * by not regenerating the whole response (a heavy operation) every time.
 * <p>
 *
 * The /guild endpoint controller ({@link GuildController}) utilizes the {@link GuildCache#getByName(String)}
 * and {@link GuildCache#getByPlayer(String)} methods to fetch the API response for a given guild. If the response is
 * not in the cache, it relies upon the {@link GuildBuilder} instance to build a new response, which is then cached
 * and returned.
 *
 * @see GuildBuilder
 * @since 0.6.0
 * @author chloe
 */
public class GuildCache extends Cache<String, GuildCache.CacheEntry> {

    /**
     * The Builder instance used to generate responses
     * @see GuildBuilder
     */
    private final GuildBuilder builder = new GuildBuilder();

    /**
     * Gets the response for the provided guild by name.
     * <p>
     *
     * If the guild is already in the cache, return the cached version. If the player is not in the cache, or the
     * cached response is over an hour old, generate a new response, update the cache, and return that instead.
     *
     * @param name The name of the guild to look up
     * @return The response for the given player
     */
    public JsonObject getByName(@NonNull String name) {

        // Take this opportunity to remove all outdated cache entries to save memory
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // If the guild is already in the cache, and the cache isn't outdated, use that instead
        if (this.cache.containsKey(name)) {
            return this.cache.get(name).data;
        }

        // The guild either isn't in the cache, or the cache is outdated. Build a new response
        final JsonObject data = this.builder.buildFromName(name);

        // Only cache the response if it was successful
        if (data.get("success").getAsBoolean()) {
            this.cache.put(name, new GuildCache.CacheEntry(data));
        }

        return data;
    }

    /**
     * Gets the response for the provided guild by a member's UUID.
     * <p>
     *
     * If the guild is already in the cache, return the cached version. If the player is not in the cache, or the
     * cached response is over an hour old, generate a new response, update the cache, and return that instead.
     *
     * @param player The UUID of the player to look up
     * @return The response for the given player
     */
    public JsonObject getByPlayer(@NonNull String player) {

        // Take this opportunity to remove all outdated cache entries to save memory
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // If the guild is already in the cache, and the cache isn't outdated, use that instead
        if (this.cache.containsKey(player)) {
            return this.cache.get(player).data;
        }

        // The guild either isn't in the cache, or the cache is outdated. Build a new response
        final JsonObject data = this.builder.buildFromPlayer(player);

        // Only cache the response if it was successful
        if (data.get("success").getAsBoolean()) {
            this.cache.put(player, new GuildCache.CacheEntry(data));
        }

        return data;
    }

    /**
     * An entry within the cache, mapped to in {@link StatsCache#cache} using player names as keys. Stores the time
     * the entry was generated at, along with the data itself.
     */
    @Getter
    @RequiredArgsConstructor
    public static class CacheEntry {

        /**
         * The time at which this cache entry was generated
         */
        private final long cacheTime = System.currentTimeMillis();

        private final JsonObject data;

        /**
         * Gets the time at which this cache entry expires - one hour after it was created
         * @return The timestamp one hour after this cache was created
         */
        public long getExpiryTime() {
            return this.cacheTime + (60 * 60 * 1000);
        }

        /**
         * @return Whether this cache entry should be considered outdated
         */
        public boolean isExpired() {
            return this.getExpiryTime() <= System.currentTimeMillis();
        }
    }
}
