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
import io.nadeshiko.nadeshiko.api.StatsController;
import io.nadeshiko.nadeshiko.util.Cache;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Simple cache implementation to save API responses for five minutes before invalidating them.
 * <p>
 *
 * The primary purpose of the cache is to reduce the load on the Hypixel and Mojang APIs, as well as saving time
 * by not regenerating the whole response (a heavy operation) every time.
 * <p>
 *
 * The /stats endpoint controller ({@link StatsController}) utilizes the {@link StatsCache#get(String, boolean)} method
 * to fetch the API response for a given player. If the response is not in the cache, it relies upon the
 * {@link StatsBuilder} instance to build a new response, which is then cached and returned.
 *
 * @see StatsBuilder
 * @author chloe
 */
public class StatsCache extends Cache<String, StatsCache.CacheEntry> {

	/**
	 * The Builder instance used to generate responses
	 * @see StatsBuilder
	 */
	private final StatsBuilder builder = new StatsBuilder();

	/**
	 * Gets the response for the provided player.
	 * <p>
	 *
	 * If the player is already in the cache, return the cached version. If the player is not in the cache, or
	 * the cached response is over five minutes old, generate a new response, update the cache, and return
	 * that instead.
	 *
	 * @param name The name of the player to look up
	 * @param full Whether the response should include extra information on the player's status and guild. This is
	 *             significantly slower.
	 * @return The response for the given player
	 */
	public JsonObject get(@NonNull String name, boolean full) {

		// Take this opportunity to remove all outdated cache entries to save memory
		this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

		// If the player is already in the cache, and the cache isn't outdated, use that instead
		// TODO: this naive method can result in players being cached multiple times (dashed uuid, name, undashed uuid)
		if (this.cache.containsKey(name)) {
			return this.cache.get(name).data;
		}

		// The player either isn't in the cache, or the cache is outdated. Build a new response
		final JsonObject data = this.builder.build(name, full);

		// Only cache the response if it was successful, and it was a full request
		if (full && data.get("success").getAsBoolean()) {
			this.cache.put(name, new CacheEntry(data));
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
		 * Gets the time at which this cache entry expires - five minutes after it was created
		 * @return The timestamp five minutes after this cache was created
		 */
		public long getExpiryTime() {
			return this.cacheTime + (5 * 60 * 1000);
		}

		/**
		 * @return Whether this cache entry should be considered outdated
		 */
		public boolean isExpired() {
			return this.getExpiryTime() <= System.currentTimeMillis();
		}
	}
}
