package io.nadeshiko.nadeshiko.cards;

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
 * {@link CardsCache#get(String, CardGame)} method to fetch the card for a given player. If the card is not in the cache, it
 * relies upon the {@link CardGenerator} instance to draw a new card, which is then cached and returned.
 *
 * @see CardGenerator
 * @author chloe
 */
public class CardsCache {

	/**
	 * The cache of player data, using usernames + games as keys. Example: {@code Minikloon:BEDWARS}
	 * @see CacheEntry
	 */
	private final HashMap<String, CacheEntry> cache = new HashMap<>();

	/**
	 * The Card Generator instance used to generate cards
	 * @see CardGenerator
	 */
	private final CardGenerator generator = new CardGenerator();

	/**
	 * Gets the specified card for the provided player.
	 * <p>
	 *
	 * If the player is already in the cache, return the cached version. If the player is not in the cache, or
	 * the cached response is over five minutes old, generate a new response, update the cache, and return
	 * that instead.
	 *
	 * @param name The name of the player to look up
	 * @return The response for the given player
	 */
	public byte[] get(@NonNull String name, @NonNull CardGame game) throws Exception {

		// Take this opportunity to remove all outdated cache entries to save memory
		this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

		// If the player + game combination is already in the cache,
		//   and the cache isn't outdated, use that instead
		if (this.cache.containsKey(name + ":" + game)) {
			return this.cache.get(name + ":" + game).getCard();
		}

		// The player either isn't in the cache, or the cache is outdated. Build a new response
		final byte[] card = this.generator.generateCard(game, name);

		this.cache.put(name + ":" + game, new CacheEntry(card));

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
