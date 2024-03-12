package io.nadeshiko.nadeshiko.stats;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

public class Cache {

	/**
	 * The cache of player data, using usernames as keys
	 */
	private final HashMap<String, CacheEntry> cache = new HashMap<>();

	public String get(String name) {
		if (this.cache.containsKey(name) && !this.cache.get(name).isExpired()) {
			return this.cache.get(name).data;
		}

		// The player either isn't in the cache, or the cache is outdated
		final String data = this.fetchUpdated(name);
		this.cache.put(name, new CacheEntry(data));
		return data;
	}

	private String fetchUpdated(String name) {
		return name;
	}

	@Getter
	@RequiredArgsConstructor
	private static class CacheEntry {
		private final long cacheTime = System.currentTimeMillis();
		private final String data;

		/**
		 * Gets the time at which this cache entry expires - 2 minutes after it was created
		 * @return The timestamp two minutes after this cache was created
		 */
		public long getExpiryTime() {
			return this.cacheTime + (60 * 1000);
		}

		/**
		 * @return Whether this cache entry should be considered outdated
		 */
		public boolean isExpired() {
			return this.getExpiryTime() <= System.currentTimeMillis();
		}
	}
}
