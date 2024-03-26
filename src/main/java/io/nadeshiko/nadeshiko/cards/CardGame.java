package io.nadeshiko.nadeshiko.cards;

import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.cards.provider.impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardGame {
	BEDWARS(BedwarsCardProvider.class),
	DUELS(DuelsCardProvider.class);

	public final Class<? extends CardProvider> provider;
}
