package io.nadeshiko.nadeshiko.cards;

import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.cards.provider.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public enum CardGame {
	BEDWARS(BedwarsCardProvider.class),
	DUELS(DuelsCardProvider.class),
	NETWORK(NetworkCardProvider.class);

	private final Class<? extends CardProvider> providerClass;

	private CardProvider provider = null;

	@SneakyThrows
	public CardProvider getProvider() {
		if (this.provider == null) {
			this.provider = this.providerClass.getDeclaredConstructor().newInstance();
		}

		return this.provider;
	}
}
