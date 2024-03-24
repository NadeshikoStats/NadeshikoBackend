package io.nadeshiko.nadeshiko.stats;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * Simple cache implementation to save API responses for five minutes before invalidating them.
 * <p>
 *
 * The primary purpose of the cache is to reduce the load on the Hypixel and Mojang APIs, as well as saving time
 * by not regenerating the whole response (a heavy operation) every time.
 * <p>
 *
 * The /stats endpoint controller ({@link StatsController}) utilizes the {@link Cache#get(String)} method to fetch
 * the API response for a given player. If the response is not in the cache, it relies upon the {@link Builder}
 * instance to build a new response, which is then cached and returned.
 *
 * @see Builder
 * @author chloe
 */
public class Cache {

	/**
	 * The cache of player data, using usernames as keys
	 * @see CacheEntry
	 */
	private final HashMap<String, CacheEntry> cache = new HashMap<>();

	/**
	 * The Builder instance used to generate responses
	 * @see Builder
	 */
	private final Builder builder = new Builder();

	/**
	 * Gets the response for the provided player.
	 * <p>
	 *
	 * If the player is already in the cache, return the cached version. If the player is not in the cache, or
	 * the cached response is over five minutes old, generate a new response, update the cache, and return
	 * that instead.
	 *
	 * @param name The name of the player to look up
	 * @return The response for the given player
	 */
	public JsonObject get(@NonNull String name) {

		// Take this opportunity to remove all outdated cache entries to save memory
		this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

		// If the player is already in the cache, and the cache isn't outdated, use that instead
		if (this.cache.containsKey(name)) {
			return this.cache.get(name).data;
		}

		// The player either isn't in the cache, or the cache is outdated. Build a new response
		final JsonObject data = this.builder.build(name);

		// Only cache the response if it was successful
		if (data.get("success").getAsBoolean()) {
			this.cache.put(name, new CacheEntry(data));
		}

		return data;
	}

	/**
	 * An entry within the cache, mapped to in {@link Cache#cache} using player names as keys. Stores the time
	 * the entry was generated at, along with the data itself.
	 */
	@Getter
	@RequiredArgsConstructor
	private static class CacheEntry {

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
