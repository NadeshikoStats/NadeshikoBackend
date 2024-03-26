package io.nadeshiko.nadeshiko.cards;

import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.cards.provider.impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardGame {
	BEDWARS(new BedwarsCardProvider()),
	DUELS(new DuelsCardProvider());

	public final CardProvider provider;
}
