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

package io.nadeshiko.nadeshiko.cards;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * Simple cache implementation to save API responses for fifteen minutes before invalidating them.
 * <p>
 *
 * The primary purpose of the cache is to reduce the load on the Hypixel and Mojang APIs, as well as saving time
 * by not regenerating the whole response (a heavy operation) every time.
 * <p>
 *
 * The /card endpoint controller ({@link io.nadeshiko.nadeshiko.api.CardController}) utilizes the
 * {@link CardsCache#get(JsonObject, CardGame)} method to fetch the card for a given data set. If the card is not
 * in the cache, it relies upon the {@link CardGenerator} instance to draw a new card, which is then cached and returned.
 *
 * @see CardGenerator
 * @author chloe
 */
public class CardsCache {

	/**
	 * The cache of player data, using data as keys.
	 * @see CacheEntry
	 */
	private final HashMap<JsonObject, CacheEntry> cache = new HashMap<>();

	/**
	 * The Card Generator instance used to generate cards
	 * @see CardGenerator
	 */
	private final CardGenerator generator = new CardGenerator();

	/**
	 * Gets the specified card
	 * <p>
	 *
	 * If the card is already in the cache, return the cached version. If the card is not in the cache, or
	 * the cached response is over five minutes old, generate a new response, update the cache, and return
	 * that instead.
	 *
	 * @param data The data passed along, including the player and any custom settings.
	 * @return The response for the given player
	 */
	public byte[] get(@NonNull JsonObject data, @NonNull CardGame game) throws Exception {

		// Take this opportunity to remove all outdated cache entries to save memory
		this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

		// If the data is already in the cache, and the cache isn't outdated, use that instead
		if (this.cache.containsKey(data)) {
			return this.cache.get(data).getCard();
		}

		// The player either isn't in the cache, or the cache is outdated. Build a new response
		final byte[] card = this.generator.generateCard(game, data);

		this.cache.put(data, new CacheEntry(card));

		return card;
	}

	/**
	 * An entry within the cache, mapped to in {@link CardsCache#cache} using player names with the game appended
	 * as keys. Stores the time the entry was generated at, along with the card itself.
	 */
	@Getter
	@RequiredArgsConstructor
	private static class CacheEntry {

		/**
		 * The time at which this cache entry was generated
		 */
		private final long cacheTime = System.currentTimeMillis();

		private final byte[] card;

		/**
		 * Gets the time at which this cache entry expires - fifteen minutes after it was created
		 * @return The timestamp fifteen minutes after this cache was created
		 */
		public long getExpiryTime() {
			return this.cacheTime + (15 * 60 * 1000);
		}

		/**
		 * @return Whether this cache entry should be considered outdated
		 */
		public boolean isExpired() {
			return this.getExpiryTime() <= System.currentTimeMillis();
		}
	}
}
