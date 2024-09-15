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

package io.nadeshiko.nadeshiko.leaderboards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Leaderboard {
    FIRST_LOGIN("first_login", 1),
    NETWORK_LEVEL("network_level", -1),
    ACHIEVEMENT_POINTS("achievement_points", -1),
    KARMA("karma", -1),
    RANKS_GIFTED("ranks_gifted", -1),
    QUESTS_COMPLETED("quests_completed", -1);

    private final String key;
    private final int sortDirection;

    public static Leaderboard getByKey(String key) {
        for (Leaderboard leaderboard : Leaderboard.values()) {
            if (leaderboard.key.equals(key)) {
                return leaderboard;
            }
        }
        return null;
    }
}
