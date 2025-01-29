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

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * Enum of leaderboard categories and functions to capture the data used by leaderboards within the
 * category from the whole player data.
 *
 * @author chloe
 * @since 0.9.0
 * @see Leaderboard
 */
@RequiredArgsConstructor
public enum LeaderboardCategory {
    NETWORK(data -> data.getAsJsonObject("profile")),
    BEDWARS(data -> getStats(data).getAsJsonObject("Bedwars")),
    DUELS(data -> getStats(data).getAsJsonObject("Duels")),
    SKYWARS(data -> getStats(data).getAsJsonObject("SkyWars")),
    PIT(data -> getStats(data).getAsJsonObject("Pit")),
    BUILD_BATTLE(data -> getStats(data).getAsJsonObject("BuildBattle")),
    MURDER_MYSTERY(data -> getStats(data).getAsJsonObject("MurderMystery")),
    TNT_GAMES(data -> getStats(data).getAsJsonObject("TNTGames")),
    ARCADE(data -> getStats(data).getAsJsonObject("Arcade")),
    BLITZ(data -> getStats(data).getAsJsonObject("HungerGames")),
    ARENA_BRAWL(data -> getStats(data).getAsJsonObject("Arena")),
    PAINTBALL(data -> getStats(data).getAsJsonObject("Paintball")),
    QUAKECRAFT(data -> getStats(data).getAsJsonObject("Quake")),
    TURBO_KART_RACERS(data -> getStats(data).getAsJsonObject("Gingerbread")),
    VAMPIREZ(data -> getStats(data).getAsJsonObject("VampireZ")),
    WALLS(data -> getStats(data).getAsJsonObject("Walls")),
    COPS_AND_CRIMS(data -> getStats(data).getAsJsonObject("MCGO")),
    MEGA_WALLS(data -> getStats(data).getAsJsonObject("Walls3")),
    SMASH_HEROES(data -> getStats(data).getAsJsonObject("SuperSmash")),
    SPEED_UHC(data -> getStats(data).getAsJsonObject("SpeedUHC")),
    UHC(data -> getStats(data).getAsJsonObject("UHC")),
    WARLORDS(data -> getStats(data).getAsJsonObject("Battleground")),
    WOOL_GAMES(data -> getStats(data).getAsJsonObject("WoolGames")),
    FISHING(data -> getStats(data).getAsJsonObject("MainLobby").getAsJsonObject("fishing"));


    // Different from the others. Takes in the active profile data from the SkyCrypt API.
//    SKYBLOCK(profile -> profile.getAsJsonObject("data"));

    private final Function<JsonObject, JsonObject> deriveInput;

    /**
     * Shorthand to get the stats object from player data
     */
    private static JsonObject getStats(JsonObject playerData) {
        return playerData.getAsJsonObject("stats");
    }

    public JsonObject getDeriveInput(JsonObject playerData) {
        try {
            return this.deriveInput.apply(playerData);
        } catch (Exception e) {
            return new JsonObject(); // don't throw NPE if the player is missing the object
        }
    }
}
