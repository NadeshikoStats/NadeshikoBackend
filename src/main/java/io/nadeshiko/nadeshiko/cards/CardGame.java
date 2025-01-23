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

import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.cards.provider.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public enum CardGame {
	BEDWARS(BedwarsCardProvider.class),
	BUILD_BATTLE(BuildBattleCardProvider.class),
	DUELS(DuelsCardProvider.class),
	NETWORK(NetworkCardProvider.class),
	SKYBLOCK_GENERAL(SkyBlockGeneralCardProvider.class),
	SKYWARS(SkywarsCardProvider.class);

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
