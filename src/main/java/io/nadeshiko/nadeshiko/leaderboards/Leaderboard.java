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

    // Network
    FIRST_LOGIN(1),
    NETWORK_LEVEL,
    ACHIEVEMENT_POINTS,
    KARMA,
    RANKS_GIFTED,
    QUESTS_COMPLETED,

    // Bed Wars
    BEDWARS_EXP,
    BEDWARS_TICKETS,
    BEDWARS_TICKETS_EARNED,
    BEDWARS_COMPLETED_CHALLENGES,
    BEDWARS_COLLECTED_EMERALDS,
    BEDWARS_COLLECTED_DIAMONDS,
    BEDWARS_WINS,
    BEDWARS_WLR,
    BEDWARS_FINALS,
    BEDWARS_FKDR,
    BEDWARS_KILLS,
    BEDWARS_KDR,
    BEDWARS_BEDS,
    BEDWARS_BBLR,
    BEDWARS_SOLO_WINS,
    BEDWARS_SOLO_WLR,
    BEDWARS_SOLO_FINALS,
    BEDWARS_SOLO_FKDR,
    BEDWARS_DOUBLES_WINS,
    BEDWARS_DOUBLES_WLR,
    BEDWARS_DOUBLES_FINALS,
    BEDWARS_DOUBLES_FKDR,
    BEDWARS_THREES_WINS,
    BEDWARS_THREES_WLR,
    BEDWARS_THREES_FINALS,
    BEDWARS_THREES_FKDR,
    BEDWARS_FOURS_WINS,
    BEDWARS_FOURS_WLR,
    BEDWARS_FOURS_FINALS,
    BEDWARS_FOURS_FKDR,
    BEDWARS_FOURVFOUR_WINS,
    BEDWARS_FOURVFOUR_WLR,
    BEDWARS_FOURVFOUR_FINALS,
    BEDWARS_FOURVFOUR_FKDR;

    Leaderboard() {
        this(-1); // default to descending
    }

    /**
     * The direction this leaderboard sorts in: -1 is descending, 1 is ascending. Defaults to descending.
     */
    private final int sortDirection;

    public static Leaderboard get(String name) {
        for (Leaderboard leaderboard : Leaderboard.values()) {
            if (leaderboard.name().equals(name)) {
                return leaderboard;
            }
        }
        return null;
    }
}
