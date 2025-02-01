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

import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

import io.nadeshiko.nadeshiko.util.JsonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Enum of leaderboards and functions to capture leaderboard data
 *
 * @author chloe, Brooke
 * @since 0.9.0
 */
@RequiredArgsConstructor
public enum Leaderboard {

    /**
     * Network leaderboards.
     * Derivation functions of leaderboards in this category take in the /profile object.
     * @see LeaderboardCategory#NETWORK
     */
    NETWORK_FIRST_LOGIN(NETWORK, profile -> profile.get("first_login").getAsLong(), 1),
    NETWORK_NETWORK_LEVEL(NETWORK, profile -> profile.get("network_level").getAsFloat()),
    NETWORK_ACHIEVEMENT_POINTS(NETWORK, profile -> profile.get("achievement_points").getAsInt()),
    NETWORK_KARMA(NETWORK, profile -> profile.get("karma").getAsInt()),
    NETWORK_RANKS_GIFTED(NETWORK, profile -> profile.get("ranks_gifted").getAsInt()),
    NETWORK_QUESTS_COMPLETED(NETWORK, profile -> profile.get("quests_completed").getAsInt()),

    /**
     * Bed Wars leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Bedwars object.
     * @see LeaderboardCategory#BEDWARS
     */
    BEDWARS_EXP(BEDWARS, bw -> bw.get("Experience").getAsLong()),
    BEDWARS_TICKETS_EARNED(BEDWARS, bw -> bw.getAsJsonObject("slumber").get("total_tickets_earned").getAsLong()),
    BEDWARS_COMPLETED_CHALLENGES(BEDWARS, bw -> bw.get("total_challenges_completed").getAsInt()),
    BEDWARS_COLLECTED_EMERALDS(BEDWARS, bw -> bw.get("emerald_resources_collected_bedwars").getAsInt()),
    BEDWARS_COLLECTED_DIAMONDS(BEDWARS, bw -> bw.get("diamond_resources_collected_bedwars").getAsInt()),
    BEDWARS_WINSTREAK(BEDWARS, bw -> bw.get("winstreak").getAsInt()),
    BEDWARS_WINS(BEDWARS, bw -> bw.get("wins_bedwars").getAsInt()),
    BEDWARS_WLR(BEDWARS, bw -> bw.get("wins_bedwars").getAsDouble() / bw.get("losses_bedwars").getAsDouble()),
    BEDWARS_FINALS(BEDWARS, bw -> bw.get("final_kills_bedwars").getAsInt()),
    BEDWARS_FKDR(BEDWARS, bw -> bw.get("final_kills_bedwars").getAsDouble() / bw.get("final_deaths_bedwars").getAsDouble()),
    BEDWARS_KILLS(BEDWARS, bw -> bw.get("kills_bedwars").getAsInt()),
    BEDWARS_KDR(BEDWARS, bw -> bw.get("kills_bedwars").getAsDouble() / bw.get("deaths_bedwars").getAsDouble()),
    BEDWARS_BEDS(BEDWARS, bw -> bw.get("beds_broken_bedwars").getAsInt()),
    BEDWARS_BBLR(BEDWARS, bw -> bw.get("beds_broken_bedwars").getAsDouble() / bw.get("beds_lost_bedwars").getAsDouble()),
    BEDWARS_SOLO_WINSTREAK(BEDWARS, bw -> bw.get("eight_one_winstreak").getAsInt()),
    BEDWARS_SOLO_WINS(BEDWARS, bw -> bw.get("eight_one_wins_bedwars").getAsInt()),
    BEDWARS_SOLO_WLR(BEDWARS, bw -> bw.get("eight_one_wins_bedwars").getAsDouble() / bw.get("eight_one_losses_bedwars").getAsDouble()),
    BEDWARS_SOLO_FINALS(BEDWARS, bw -> bw.get("eight_one_final_kills_bedwars").getAsInt()),
    BEDWARS_SOLO_FKDR(BEDWARS, bw -> bw.get("eight_one_final_kills_bedwars").getAsDouble() / bw.get("eight_one_final_deaths_bedwars").getAsDouble()),
    BEDWARS_DOUBLES_WINSTREAK(BEDWARS, bw -> bw.get("eight_two_winstreak").getAsInt()),
    BEDWARS_DOUBLES_WINS(BEDWARS, bw -> bw.get("eight_two_wins_bedwars").getAsInt()),
    BEDWARS_DOUBLES_WLR(BEDWARS, bw -> bw.get("eight_two_wins_bedwars").getAsDouble() / bw.get("eight_two_losses_bedwars").getAsDouble()),
    BEDWARS_DOUBLES_FINALS(BEDWARS, bw -> bw.get("eight_two_final_kills_bedwars").getAsInt()),
    BEDWARS_DOUBLES_FKDR(BEDWARS, bw -> bw.get("eight_two_final_kills_bedwars").getAsDouble() / bw.get("eight_two_final_deaths_bedwars").getAsDouble()),
    BEDWARS_THREES_WINSTREAK(BEDWARS, bw -> bw.get("four_three_winstreak").getAsInt()),
    BEDWARS_THREES_WINS(BEDWARS, bw -> bw.get("four_three_wins_bedwars").getAsInt()),
    BEDWARS_THREES_WLR(BEDWARS, bw -> bw.get("four_three_wins_bedwars").getAsDouble() / bw.get("four_three_losses_bedwars").getAsDouble()),
    BEDWARS_THREES_FINALS(BEDWARS, bw -> bw.get("four_three_final_kills_bedwars").getAsInt()),
    BEDWARS_THREES_FKDR(BEDWARS, bw -> bw.get("four_three_final_kills_bedwars").getAsDouble() / bw.get("four_three_final_deaths_bedwars").getAsDouble()),
    BEDWARS_FOURS_WINSTREAK(BEDWARS, bw -> bw.get("four_four_winstreak").getAsInt()),
    BEDWARS_FOURS_WINS(BEDWARS, bw -> bw.get("four_four_wins_bedwars").getAsInt()),
    BEDWARS_FOURS_WLR(BEDWARS, bw -> bw.get("four_four_wins_bedwars").getAsDouble() / bw.get("four_four_losses_bedwars").getAsDouble()),
    BEDWARS_FOURS_FINALS(BEDWARS, bw -> bw.get("four_four_final_kills_bedwars").getAsInt()),
    BEDWARS_FOURS_FKDR(BEDWARS, bw -> bw.get("four_four_final_kills_bedwars").getAsDouble() / bw.get("four_four_final_deaths_bedwars").getAsDouble()),
    BEDWARS_FOURVFOUR_WINSTREAK(BEDWARS, bw -> bw.get("two_four_winstreak").getAsInt()),
    BEDWARS_FOURVFOUR_WINS(BEDWARS, bw -> bw.get("two_four_wins_bedwars").getAsInt()),
    BEDWARS_FOURVFOUR_WLR(BEDWARS, bw -> bw.get("two_four_wins_bedwars").getAsDouble() / bw.get("two_four_losses_bedwars").getAsDouble()),
    BEDWARS_FOURVFOUR_FINALS(BEDWARS, bw -> bw.get("two_four_final_kills_bedwars").getAsInt()),
    BEDWARS_FOURVFOUR_FKDR(BEDWARS, bw -> bw.get("two_four_final_kills_bedwars").getAsDouble() / bw.get("two_four_final_deaths_bedwars").getAsDouble()),
    BEDWARS_COLLECTED_IRON(BEDWARS, bw -> bw.get("iron_resources_collected_bedwars").getAsInt()),
    BEDWARS_COLLECTED_GOLD(BEDWARS, bw -> bw.get("gold_resources_collected_bedwars").getAsInt()),
    BEDWARS_TOKENS(BEDWARS, bw -> bw.get("coins").getAsInt()),
    /**
     * Duels leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Duels object.
     * @see LeaderboardCategory#DUELS
     */
    DUELS_CLICKS(DUELS, duels -> duels.get("melee_swings").getAsInt()),
    DUELS_WINS(DUELS, duels -> duels.get("wins").getAsInt()),
    DUELS_WLR(DUELS, duels -> duels.get("wins").getAsDouble() / duels.get("losses").getAsDouble()),
    DUELS_KILLS(DUELS, duels -> duels.get("kills").getAsInt()),
    DUELS_DAMAGE_DEALT(DUELS, duels -> duels.get("damage_dealt").getAsLong()),
    DUELS_HEALTH_REGENERATED(DUELS, duels -> duels.get("health_regenerated").getAsLong()),
    DUELS_WINSTREAK(DUELS, duels -> duels.get("current_winstreak").getAsInt()),
    DUELS_BEST_WINSTREAK(DUELS, duels -> duels.get("best_overall_winstreak").getAsInt()),
    DUELS_BRIDGE_WINS(DUELS, duels -> duels.get("bridge_duel_wins").getAsInt()),
    DUELS_BRIDGE_GOALS(DUELS, duels -> duels.get("bridge_duel_goals").getAsInt()),
    DUELS_SW_WINS(DUELS, duels -> duels.get("sw_duel_wins").getAsInt()),
    DUELS_CLASSIC_WINS(DUELS, duels -> duels.get("classic_duel_wins").getAsInt()),
    DUELS_UHC_WINS(DUELS, duels -> duels.get("uhc_duel_wins").getAsInt()),
    DUELS_SUMO_WINS(DUELS, duels -> duels.get("sumo_duel_wins").getAsInt()),
    DUELS_PARKOUR_WINS(DUELS, duels -> duels.get("parkour_eight_wins").getAsInt()),
    DUELS_BLITZ_WINS(DUELS, duels -> duels.get("blitz_duel_wins").getAsInt()),
    DUELS_BOW_WINS(DUELS, duels -> duels.get("bow_duel_wins").getAsInt()),
    DUELS_MW_WINS(DUELS, duels -> duels.get("mw_duel_wins").getAsInt()),
    DUELS_BOWSPLEEF_WINS(DUELS, duels -> duels.get("bowspleef_duel_wins").getAsInt()),
    DUELS_OP_WINS(DUELS, duels -> duels.get("op_duel_wins").getAsInt()),
    DUELS_COMBO_WINS(DUELS, duels -> duels.get("combo_duel_wins").getAsInt()),
    DUELS_BOXING_WINS(DUELS, duels -> duels.get("boxing_duel_wins").getAsInt()),
    DUELS_NODEBUFF_WINS(DUELS, duels -> duels.get("potion_duel_wins").getAsInt()),
    DUELS_ARENA_WINS(DUELS, duels -> duels.get("duel_arena_wins").getAsInt()),
    DUELS_TOKENS(DUELS, duels -> duels.get("coins").getAsInt()),

    /**
     * SkyWars leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/SkyWars object.
     * @see LeaderboardCategory#SKYWARS
     */
    SKYWARS_EXP(SKYWARS, sw -> sw.get("skywars_experience").getAsInt()),
    SKYWARS_WINS(SKYWARS, sw -> sw.get("wins").getAsInt()),
    SKYWARS_WLR(SKYWARS, sw -> sw.get("wins").getAsDouble() / sw.get("losses").getAsDouble()),
    SKYWARS_KILLS(SKYWARS, sw -> sw.get("kills").getAsInt()),
    SKYWARS_KDR(SKYWARS, sw -> sw.get("kills").getAsDouble() / sw.get("deaths").getAsDouble()),
    SKYWARS_SOLO_NORMAL_WINS(SKYWARS, sw -> sw.get("wins_solo_normal").getAsInt()),
    SKYWARS_SOLO_NORMAL_WLR(SKYWARS, sw -> sw.get("wins_solo_normal").getAsDouble() / Math.max(sw.get("losses_solo_normal").getAsDouble(), 1)),
    SKYWARS_SOLO_NORMAL_KILLS(SKYWARS, sw -> sw.get("kills_solo_normal").getAsInt()),
    SKYWARS_SOLO_NORMAL_KDR(SKYWARS, sw -> sw.get("kills_solo_normal").getAsDouble() / Math.max(sw.get("deaths_solo_normal").getAsDouble(), 1)),
    SKYWARS_SOLO_INSANE_WINS(SKYWARS, sw -> sw.get("wins_solo_insane").getAsInt()),
    SKYWARS_SOLO_INSANE_WLR(SKYWARS, sw -> sw.get("wins_solo_insane").getAsDouble() / Math.max(sw.get("losses_solo_insane").getAsDouble(), 1)),
    SKYWARS_SOLO_INSANE_KILLS(SKYWARS, sw -> sw.get("kills_solo_insane").getAsInt()),
    SKYWARS_SOLO_INSANE_KDR(SKYWARS, sw -> sw.get("kills_solo_insane").getAsDouble() / Math.max(sw.get("deaths_solo_insane").getAsDouble(), 1)),
    SKYWARS_TEAM_NORMAL_WINS(SKYWARS, sw -> sw.get("wins_team_normal").getAsInt()),
    SKYWARS_TEAM_NORMAL_WLR(SKYWARS, sw -> sw.get("wins_team_normal").getAsDouble() / Math.max(sw.get("losses_team_normal").getAsDouble(), 1)),
    SKYWARS_TEAM_NORMAL_KILLS(SKYWARS, sw -> sw.get("kills_team_normal").getAsInt()),
    SKYWARS_TEAM_NORMAL_KDR(SKYWARS, sw -> sw.get("kills_team_normal").getAsDouble() / Math.max(sw.get("deaths_team_normal").getAsDouble(), 1)),
    SKYWARS_TEAM_INSANE_WINS(SKYWARS, sw -> sw.get("wins_team_insane").getAsInt()),
    SKYWARS_TEAM_INSANE_WLR(SKYWARS, sw -> sw.get("wins_team_insane").getAsDouble() / Math.max(sw.get("losses_team_insane").getAsDouble(), 1)),
    SKYWARS_TEAM_INSANE_KILLS(SKYWARS, sw -> sw.get("kills_team_insane").getAsInt()),
    SKYWARS_TEAM_INSANE_KDR(SKYWARS, sw -> sw.get("kills_team_insane").getAsDouble() / Math.max(sw.get("deaths_team_insane").getAsDouble(), 1)),
    SKYWARS_COINS(SKYWARS, sw -> sw.get("coins").getAsInt()),
    SKYWARS_TOKENS(SKYWARS, sw -> sw.get("cosmetic_tokens").getAsInt()),
    SKYWARS_LAB_WINS(SKYWARS, sw -> sw.get("wins_lab").getAsInt()),
    SKYWARS_LAB_WLR(SKYWARS, sw -> sw.get("wins_lab").getAsDouble() / Math.max(sw.get("losses_lab").getAsDouble(), 1)),
    SKYWARS_LAB_KILLS(SKYWARS, sw -> sw.get("kills_lab").getAsInt()),
    SKYWARS_LAB_KDR(SKYWARS, sw -> sw.get("kills_lab").getAsDouble() / Math.max(sw.get("deaths_lab").getAsDouble(), 1)),
    SKYWARS_LUCKY_BLOCK_WINS(SKYWARS, sw -> sw.get("lab_win_lucky_blocks_lab").getAsInt()),

    /**
     * Pit leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Pit/pit_stats_ptl object.
     * @see LeaderboardCategory#PIT
     */
    PIT_EXP(PIT, pit -> pit.getAsJsonObject("profile").get("xp").getAsLong()),
    PIT_GOLD(PIT, pit -> pit.getAsJsonObject("profile").get("cash").getAsDouble()),
    PIT_DAMAGE_DEALT(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("damage_dealt").getAsInt()),
    PIT_JOINS(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("joins").getAsInt()),
    PIT_PLAYTIME(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("playtime_minutes").getAsInt()),
    PIT_CHAT_MESSAGES(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("chat_messages").getAsInt()),
    PIT_CLICKS(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("left_clicks").getAsInt()),
    PIT_KILLS(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("kills").getAsInt()),
    PIT_KDR(PIT, pit -> PIT_KILLS.derive(pit).intValue() /
            Math.max(1, JsonUtil.getNullableDouble(pit.getAsJsonObject("pit_stats_ptl").get("deaths")))),
    PIT_NIGHT_QUESTS_COMPLETED(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("night_quests_completed").getAsInt()),
    PIT_WHEAT_FARMED(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("wheat_farmed").getAsInt()),
    PIT_RENOWN(PIT, pit -> pit.getAsJsonObject("profile").get("renown").getAsInt()),
    PIT_ITEMS_FISHED(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("fished_anything").getAsInt()),
    PIT_INGOTS_PICKED_UP(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("ingots_picked_up").getAsInt()),
    PIT_LAUNCHER_LAUNCHES(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("launched_by_launchers").getAsInt()),
    PIT_HIGHEST_KILLSTREAK(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("max_streak").getAsInt()),
    PIT_BOUNTY(PIT, pit -> {
        int totalBounty = 0;

        JsonArray bounties = pit.getAsJsonObject("profile").getAsJsonArray("bounties");
        for (JsonElement element : bounties.getAsJsonArray()) {
            JsonObject bounty = element.getAsJsonObject();
            totalBounty += bounty.get("amount").getAsInt();
        }
        return totalBounty;
    }),
    PIT_ITEMS_ENCHANTED(PIT, pit ->
        JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier1")) +
        JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier2")) +
        JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier3"))
    ),
    /**
     * Build Battle leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/BuildBattle object.
     * @see LeaderboardCategory#BUILD_BATTLE
     */
    BUILD_BATTLE_WINS(BUILD_BATTLE, bb -> bb.get("wins").getAsInt()),
    BUILD_BATTLE_SCORE(BUILD_BATTLE, bb -> bb.get("score").getAsInt()),
    BUILD_BATTLE_VOTES(BUILD_BATTLE, bb -> bb.get("total_votes").getAsInt()),
    BUILD_BATTLE_GTB_WINS(BUILD_BATTLE, bb -> bb.get("wins_guess_the_build").getAsInt()),
    BUILD_BATTLE_GTB_CORRECT_GUESSES(BUILD_BATTLE, bb -> bb.get("correct_guesses").getAsInt()),
    BUILD_BATTLE_SPEED_BUILDERS_WINS(BUILD_BATTLE, bb -> bb.get("wins_speed_builders").getAsInt()),
    BUILD_BATTLE_TOKENS(BUILD_BATTLE, bb -> bb.get("coins").getAsInt()),
    BUILD_BATTLE_SOLO_WINS(BUILD_BATTLE, bb -> bb.get("wins_solo_normal").getAsInt()),
    BUILD_BATTLE_TEAM_WINS(BUILD_BATTLE, bb -> bb.get("wins_teams_normal").getAsInt()),
    BUILD_BATTLE_PRO_WINS(BUILD_BATTLE, bb -> bb.get("wins_solo_pro").getAsInt()),

    /**
     * Murder Mystery leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/MurderMystery object.
     * @see LeaderboardCategory#MURDER_MYSTERY
     */
    MURDER_MYSTERY_KILLS(MURDER_MYSTERY, mm -> mm.get("kills").getAsInt()),
    MURDER_MYSTERY_WINS(MURDER_MYSTERY, mm -> mm.get("wins").getAsInt()),
    MURDER_MYSTERY_MURDERER_WINS(MURDER_MYSTERY, mm -> mm.get("murderer_wins").getAsInt()),
    MURDER_MYSTERY_DETECTIVE_WINS(MURDER_MYSTERY, mm -> mm.get("detective_wins").getAsInt()),
    MURDER_MYSTERY_CLASSIC_WINS(MURDER_MYSTERY, mm -> mm.get("wins_MURDER_CLASSIC").getAsInt()),
    MURDER_MYSTERY_DOUBLE_UP_WINS(MURDER_MYSTERY, mm -> mm.get("wins_MURDER_DOUBLE_UP").getAsInt()),
    MURDER_MYSTERY_ASSASSINS_WINS(MURDER_MYSTERY, mm -> mm.get("wins_MURDER_ASSASSINS").getAsInt()),
    MURDER_MYSTERY_INFECTION_WINS(MURDER_MYSTERY, mm -> mm.get("wins_MURDER_INFECTION").getAsInt()),

    /**
     * TNT Games leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/TNTGames object.
     * @see LeaderboardCategory#TNT_GAMES
     */
    TNT_GAMES_WINS(TNT_GAMES, tnt -> tnt.get("wins").getAsInt()),
    TNT_GAMES_BOWSPLEEF_WINS(TNT_GAMES, tnt -> tnt.get("wins_bowspleef").getAsInt()),
    TNT_GAMES_PVPRUN_WINS(TNT_GAMES, tnt -> tnt.get("wins_pvprun").getAsInt()),
    TNT_GAMES_PVPRUN_KILLS(TNT_GAMES, tnt -> tnt.get("kills_pvprun").getAsInt()),
    TNT_GAMES_PVPRUN_LONGEST(TNT_GAMES, tnt -> tnt.get("record_pvprun").getAsInt()),
    TNT_GAMES_TNTRUN_WINS(TNT_GAMES, tnt -> tnt.get("wins_tntrun").getAsInt()),
    TNT_GAMES_TNTRUN_LONGEST(TNT_GAMES, tnt -> tnt.get("record_tntrun").getAsInt()),
    TNT_GAMES_TNTTAG_WINS(TNT_GAMES, tnt -> tnt.get("wins_tntag").getAsInt()),
    TNT_GAMES_TNTTAG_KILLS(TNT_GAMES, tnt -> tnt.get("kills_tntag").getAsInt()),
    TNT_GAMES_WIZARDS_WINS(TNT_GAMES, tnt -> tnt.get("wins_capture").getAsInt()),
    TNT_GAMES_WIZARDS_KILLS(TNT_GAMES, tnt -> tnt.get("kills_capture").getAsInt()),
    TNT_GAMES_TOKENS(TNT_GAMES, tnt -> tnt.get("coins").getAsInt()),
    TNT_GAMES_WIZARDS_POINTS_CAPTURED(TNT_GAMES, tnt -> tnt.get("points_capture").getAsInt()),


    /**
     * Arcade leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Arcade object.
     * @see LeaderboardCategory#ARCADE
     */
    ARCADE_COINS(ARCADE, ar -> ar.get("coins").getAsInt()),

    ARCADE_DROPPER_BEST_TIME(ARCADE, ar -> ar.getAsJsonObject("dropper").get("fastest_game").getAsInt(), 1),
    ARCADE_DROPPER_WINS(ARCADE, ar -> ar.getAsJsonObject("dropper").get("wins").getAsInt()),
    ARCADE_HYPIXEL_SAYS_WINS(ARCADE, ar -> ar.get("wins_simon_says").getAsInt()),
    ARCADE_MINI_WALLS_WINS(ARCADE, ar -> ar.get("wins_mini_walls").getAsInt()),
    ARCADE_MINI_WALLS_KILLS(ARCADE, ar -> ar.get("kills_mini_walls").getAsInt()),
    ARCADE_PARTY_WINS(ARCADE, ar -> ar.get("wins_party").getAsInt()),
    ARCADE_PIXEL_PARTY_WINS(ARCADE, ar -> ar.getAsJsonObject("pixel_party").get("wins").getAsInt()),
    ARCADE_THROW_OUT_WINS(ARCADE, ar -> ar.get("wins_throw_out").getAsInt()),
    ARCADE_THROW_OUT_KILLS(ARCADE, ar -> ar.get("kills_throw_out").getAsInt()),
    ARCADE_ZOMBIES_WINS(ARCADE, ar -> ar.get("wins_zombies").getAsInt()),
    ARCADE_ZOMBIES_KILLS(ARCADE, ar -> ar.get("zombie_kills_zombies").getAsInt()),
    ARCADE_ZOMBIES_WINDOWS_REPAIRED(ARCADE, ar -> ar.get("windows_repaired_zombies").getAsInt()),
    ARCADE_ZOMBIES_PLAYERS_REVIVED(ARCADE, ar -> ar.get("players_revived_zombies").getAsInt()),
    ARCADE_ZOMBIES_DOORS_OPENED(ARCADE, ar -> ar.get("doors_opened_zombies").getAsInt()),
    ARCADE_BLOCKING_DEAD_WINS(ARCADE, ar -> ar.get("wins_dayone").getAsInt()),
    ARCADE_BLOCKING_DEAD_KILLS(ARCADE, ar -> ar.get("kills_dayone").getAsInt()),
    ARCADE_BOUNTY_HUNTERS_WINS(ARCADE, ar -> ar.get("wins_oneinthequiver").getAsInt()),
    ARCADE_BOUNTY_HUNTERS_KILLS(ARCADE, ar -> ar.get("kills_oneinthequiver").getAsInt()),
    ARCADE_CREEPER_ATTACK_MAX_WAVE(ARCADE, ar -> ar.get("max_wave").getAsInt()),
    ARCADE_DRAGON_WARS_WINS(ARCADE, ar -> ar.get("wins_dragonwars2").getAsInt()),
    ARCADE_DRAGON_WARS_KILLS(ARCADE, ar -> ar.get("kills_dragonwars2").getAsInt()),
    ARCADE_ENDER_SPLEEF_WINS(ARCADE, ar -> ar.get("wins_ender").getAsInt()),
    ARCADE_ENDER_SPLEEF_BLOCKS_DESTROYED(ARCADE, ar -> ar.get("blocks_destroyed_ender").getAsInt()),
    ARCADE_FARM_HUNT_WINS(ARCADE, ar -> ar.get("wins_farm_hunt").getAsInt()),
    ARCADE_FARM_HUNT_HUNTER_WINS(ARCADE, ar -> ar.get("hunter_wins_farm_hunt").getAsInt()),
    ARCADE_FARM_HUNT_ANIMAL_WINS(ARCADE, ar -> ar.get("animal_wins_farm_hunt").getAsInt()),
    ARCADE_FARM_HUNT_KILLS(ARCADE, ar -> ar.get("kills_farm_hunt").getAsInt()),
    ARCADE_FARM_HUNT_TAUNTS_USED(ARCADE, ar -> ar.get("taunts_used_farm_hunt").getAsInt()),
    ARCADE_FARM_HUNT_POOP_COLLECTED(ARCADE, ar -> ar.get("poop_collected_farm_hunt").getAsInt()),
    ARCADE_FOOTBALL_WINS(ARCADE, ar -> ar.get("wins_soccer").getAsInt()),
    ARCADE_FOOTBALL_GOALS(ARCADE, ar -> ar.get("goals_soccer").getAsInt()),
    ARCADE_FOOTBALL_KICKS(ARCADE, ar -> ar.get("kicks_soccer").getAsInt()),
    ARCADE_FOOTBALL_POWER_KICKS(ARCADE, ar -> ar.get("powerkicks_soccer").getAsInt()),
    ARCADE_GALAXY_WARS_WINS(ARCADE, ar -> ar.get("sw_game_wins").getAsInt()),
    ARCADE_GALAXY_WARS_KILLS(ARCADE, ar -> ar.get("sw_kills").getAsInt()),
    ARCADE_GALAXY_WARS_KDR(ARCADE, ar -> ar.get("sw_kills").getAsDouble() / Math.max(ar.get("sw_deaths").getAsDouble(), 1)),
    ARCADE_HIDE_AND_SEEK_PARTY_POOPER_WINS(ARCADE, ar ->
        JsonUtil.getNullableInt(ar.get("party_pooper_hider_wins_hide_and_seek")) +
        JsonUtil.getNullableInt(ar.get("party_pooper_seeker_wins_hide_and_seek"))
    ),
    ARCADE_HIDE_AND_SEEK_PROP_HUNT_WINS(ARCADE, ar ->
        JsonUtil.getNullableInt(ar.get("prop_hunt_hider_wins_hide_and_seek")) +
        JsonUtil.getNullableInt(ar.get("prop_hunt_seeker_wins_hide_and_seek"))
    ),
    ARCADE_HIDE_AND_SEEK_WINS(ARCADE, ar -> ARCADE_HIDE_AND_SEEK_PARTY_POOPER_WINS.derive(ar).intValue() + ARCADE_HIDE_AND_SEEK_PROP_HUNT_WINS.derive(ar).intValue()),
    ARCADE_HOLE_IN_THE_WALL_WINS(ARCADE, ar -> ar.get("wins_hole_in_the_wall").getAsInt()),
    ARCADE_HOLE_IN_THE_WALL_QUALIFICATIONS_RECORD(ARCADE, ar -> ar.get("hitw_record_q").getAsInt()),
    ARCADE_HOLE_IN_THE_WALL_FINALS_RECORD(ARCADE, ar -> ar.get("hitw_record_f").getAsInt()),
    ARCADE_MINI_WALLS_FINAL_KILLS(ARCADE, ar -> ar.get("final_kills_mini_walls").getAsInt()),
    ARCADE_PIXEL_PARTY_POWERUPS_COLLECTED(ARCADE, ar -> ar.getAsJsonObject("pixel_party").get("power_ups_collected").getAsInt()),
    ARCADE_PIXEL_PARTY_NORMAL_WINS(ARCADE, ar -> ar.getAsJsonObject("pixel_party").get("wins_normal").getAsInt()),
    ARCADE_PIXEL_PARTY_HYPER_WINS(ARCADE, ar -> ar.getAsJsonObject("pixel_party").get("wins_hyper").getAsInt()),
    ARCADE_PIXEL_PAINTERS_WINS(ARCADE, ar -> ar.get("wins_draw_their_thing").getAsInt()),
    ARCADE_GRINCH_SIMULATOR_WINS(ARCADE, ar -> ar.get("wins_grinch_simulator_v2").getAsInt()),
    ARCADE_GRINCH_SIMULATOR_GIFTS_STOLEN(ARCADE, ar -> ar.get("gifts_grinch_simulator_v2").getAsInt()),
    ARCADE_SCUBA_SIMULATOR_WINS(ARCADE, ar -> ar.get("wins_scuba_simulator").getAsInt()),
    ARCADE_SANTA_SIMULATOR_WINS(ARCADE, ar -> ar.get("wins_santa_simulator").getAsInt()),
    ARCADE_HALLOWEEN_SIMULATOR_WINS(ARCADE, ar -> ar.get("wins_halloween_simulator").getAsInt()),
    ARCADE_EASTER_SIMULATOR_WINS(ARCADE, ar -> ar.get("wins_easter_simulator").getAsInt()),

    ARCADE_WINS(ARCADE, ar ->
        ARCADE_BLOCKING_DEAD_WINS.derive(ar).intValue() +
        ARCADE_BOUNTY_HUNTERS_WINS.derive(ar).intValue() +
        ARCADE_DRAGON_WARS_WINS.derive(ar).intValue() +
        ARCADE_ENDER_SPLEEF_WINS.derive(ar).intValue() +
        ARCADE_FARM_HUNT_WINS.derive(ar).intValue() +
        ARCADE_FOOTBALL_WINS.derive(ar).intValue() +
        ARCADE_GALAXY_WARS_WINS.derive(ar).intValue() +
        ARCADE_HIDE_AND_SEEK_WINS.derive(ar).intValue() +
        ARCADE_HOLE_IN_THE_WALL_WINS.derive(ar).intValue() +
        ARCADE_MINI_WALLS_WINS.derive(ar).intValue() +
        ARCADE_PARTY_WINS.derive(ar).intValue() +
        ARCADE_HYPIXEL_SAYS_WINS.derive(ar).intValue() +
        ARCADE_PIXEL_PAINTERS_WINS.derive(ar).intValue() +
        ARCADE_THROW_OUT_WINS.derive(ar).intValue() +
        ARCADE_ZOMBIES_WINS.derive(ar).intValue() +
        ARCADE_EASTER_SIMULATOR_WINS.derive(ar).intValue() +
        ARCADE_HALLOWEEN_SIMULATOR_WINS.derive(ar).intValue() +
        ARCADE_SANTA_SIMULATOR_WINS.derive(ar).intValue() +
        ARCADE_SCUBA_SIMULATOR_WINS.derive(ar).intValue() +
        ARCADE_GRINCH_SIMULATOR_WINS.derive(ar).intValue() +
        ARCADE_DROPPER_WINS.derive(ar).intValue() +
        ARCADE_PIXEL_PARTY_WINS.derive(ar).intValue()
    ),
    /**
     * Blitz leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/HungerGames object.
     * @see LeaderboardCategory#BLITZ
     */

    BLITZ_COINS(BLITZ, bsg -> bsg.get("coins").getAsInt()),
    BLITZ_SOLO_WINS(BLITZ, bsg -> bsg.get("wins_solo_normal").getAsInt()),
    BLITZ_TEAM_WINS(BLITZ, bsg -> bsg.get("wins_team_normal").getAsInt()),
    BLITZ_WINS(BLITZ, bsg -> BLITZ_SOLO_WINS.derive(bsg).intValue() + BLITZ_TEAM_WINS.derive(bsg).intValue()),

    BLITZ_KILLS(BLITZ, bsg -> bsg.get("kills").getAsInt()),
    BLITZ_TEAM_KILLS(BLITZ, bsg -> bsg.get("kills_team_normal").getAsInt()),
    BLITZ_SOLO_KILLS(BLITZ, bsg -> BLITZ_TEAM_KILLS.derive(bsg).intValue() - BLITZ_KILLS.derive(bsg).intValue()),
    BLITZ_KDR(BLITZ, bsg -> BLITZ_KILLS.derive(bsg).doubleValue() / Math.max(bsg.get("deaths").getAsDouble(), 1)),
    BLITZ_DAMAGE_DEALT(BLITZ, bsg -> bsg.get("damage").getAsInt()),

    /**
     * Arena Brawl leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Arena object.
     * @see LeaderboardCategory#ARENA_BRAWL
     */

    ARENA_BRAWL_COINS(ARENA_BRAWL, ab -> ab.get("coins").getAsInt()),
    ARENA_BRAWL_WINS(ARENA_BRAWL, ab -> ab.get("wins").getAsInt()),
    ARENA_BRAWL_KILLS(ARENA_BRAWL, ab -> JsonUtil.getNullableInt(ab.get("kills_1v1")) + JsonUtil.getNullableInt(ab.get("kills_2v2")) + JsonUtil.getNullableInt(ab.get("kills_4v4"))),
    ARENA_BRAWL_KDR(ARENA_BRAWL, ab -> (JsonUtil.getNullableDouble(ab.get("kills_1v1")) + JsonUtil.getNullableDouble(ab.get("kills_2v2")) + JsonUtil.getNullableDouble(ab.get("kills_4v4"))) / Math.max(1, JsonUtil.getNullableDouble(ab.get("deaths_1v1")) + JsonUtil.getNullableDouble(ab.get("deaths_2v2")) + JsonUtil.getNullableDouble(ab.get("deaths_4v4")))),
    ARENA_BRAWL_WLR(ARENA_BRAWL, ab -> ARENA_BRAWL_WINS.derive(ab).doubleValue() / ((JsonUtil.getNullableInt(ab.get("losses_1v1")) + JsonUtil.getNullableInt(ab.get("losses_2v2")) + JsonUtil.getNullableInt(ab.get("losses_4v4"))))),
    ARENA_BRAWL_MAGICAL_CHESTS(ARENA_BRAWL, ab -> ab.get("magical_chest").getAsInt()),

    /**
     * Paintball leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Paintball object.
     * @see LeaderboardCategory#PAINTBALL
     */

    PAINTBALL_COINS(PAINTBALL, pb -> pb.get("coins").getAsInt()),
    PAINTBALL_WINS(PAINTBALL, pb -> pb.get("wins").getAsInt()),
    PAINTBALL_KILLS(PAINTBALL, pb -> pb.get("kills").getAsInt()),
    PAINTBALL_KDR(PAINTBALL, pb -> pb.get("kills").getAsDouble() / Math.max(pb.get("deaths").getAsDouble(), 1)),
    PAINTBALL_KILLSTREAKS(PAINTBALL, pb -> pb.get("killstreaks").getAsInt()),

    /**
     * Quakecraft leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Quake object.
     * @see LeaderboardCategory#QUAKECRAFT
     */

    QUAKECRAFT_COINS(QUAKECRAFT, qc -> qc.get("coins").getAsInt()),
    QUAKECRAFT_WINS(QUAKECRAFT, qc -> JsonUtil.getNullableInt(qc.get("wins")) + JsonUtil.getNullableInt(qc.get("wins_teams"))),
    QUAKECRAFT_KILLS(QUAKECRAFT, qc -> JsonUtil.getNullableInt(qc.get("kills")) + JsonUtil.getNullableInt(qc.get("kills_teams"))),
    QUAKECRAFT_KDR(QUAKECRAFT, qc -> QUAKECRAFT_KILLS.derive(qc).doubleValue() / Math.max(1, JsonUtil.getNullableDouble(qc.get("deaths")) + JsonUtil.getNullableDouble(qc.get("deaths_teams")))),
    QUAKECRAFT_DISTANCE_TRAVELLED(QUAKECRAFT, qc -> qc.get("distance_travelled").getAsInt()),

    /**
     * Turbo Kart Racers leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Gingerbread object.
     * @see LeaderboardCategory#TURBO_KART_RACERS
     */

    TURBO_KART_RACERS_COINS(TURBO_KART_RACERS, tkr -> tkr.get("coins").getAsInt()),
    TURBO_KART_RACERS_TROPHIES(TURBO_KART_RACERS, tkr -> JsonUtil.getNullableInt(tkr.get("gold_trophy")) + JsonUtil.getNullableInt(tkr.get("silver_trophy")) + JsonUtil.getNullableInt(tkr.get("bronze_trophy"))),
    TURBO_KART_RACERS_GOLD_TROPHIES(TURBO_KART_RACERS, tkr -> tkr.get("gold_trophy").getAsInt()),
    TURBO_KART_RACERS_LAPS(TURBO_KART_RACERS, tkr -> tkr.get("laps_completed").getAsInt()),
    TURBO_KART_RACERS_ITEM_BOX_PICKUPS(TURBO_KART_RACERS, tkr -> tkr.get("box_pickups").getAsInt()),

    /**
     * VampireZ leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/VampireZ object.
     * @see LeaderboardCategory#VAMPIREZ
     */

    VAMPIREZ_COINS(VAMPIREZ, vz -> vz.get("coins").getAsInt()),
    VAMPIREZ_HUMAN_WINS(VAMPIREZ, vz -> vz.get("wins_human").getAsInt()),
    VAMPIREZ_HUMAN_KILLS(VAMPIREZ, vz -> vz.get("human_kills").getAsInt()),
    VAMPIREZ_VAMPIRE_WINS(VAMPIREZ, vz -> vz.get("wins_vampire").getAsInt()),
    VAMPIREZ_VAMPIRE_KILLS(VAMPIREZ, vz -> vz.get("vampire_kills").getAsInt()),
    VAMPIREZ_ZOMBIE_KILLS(VAMPIREZ, vz -> vz.get("zombie_kills").getAsInt()),


    /**
     * Walls leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Walls object.
     * @see LeaderboardCategory#WALLS
     */

    WALLS_COINS(WALLS, wl -> wl.get("coins").getAsInt()),
    WALLS_WINS(WALLS, wl -> wl.get("wins").getAsInt()),
    WALLS_KILLS(WALLS, wl -> wl.get("kills").getAsInt()),
    WALLS_KDR(WALLS, wl -> wl.get("kills").getAsDouble() / Math.max(wl.get("deaths").getAsDouble(), 1)),
    WALLS_ASSISTS(WALLS, wl -> wl.get("assists").getAsInt()),

    /**
     * Cops and Crims leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/MCGO object.
     * @see LeaderboardCategory#COPS_AND_CRIMS
     */

    COPS_AND_CRIMS_SCORE(COPS_AND_CRIMS, cc -> cc.get("score").getAsInt()),
    COPS_AND_CRIMS_COINS(COPS_AND_CRIMS, cc -> cc.get("coins").getAsInt()),

    COPS_AND_CRIMS_DEFUSAL_WINS(COPS_AND_CRIMS, cc -> cc.get("game_wins").getAsInt()),
    COPS_AND_CRIMS_DEFUSAL_KILLS(COPS_AND_CRIMS, cc -> cc.get("kills").getAsInt()),
    COPS_AND_CRIMS_DEFUSAL_BOMBS_PLANTED(COPS_AND_CRIMS, cc -> cc.get("bombs_planted").getAsInt()),
    COPS_AND_CRIMS_DEFUSAL_BOMBS_DEFUSED(COPS_AND_CRIMS, cc -> cc.get("bombs_defused").getAsInt()),
    COPS_AND_CRIMS_DEFUSAL_ROUND_WINS(COPS_AND_CRIMS, cc -> cc.get("round_wins").getAsInt()),
    COPS_AND_CRIMS_DEFUSAL_KDR(COPS_AND_CRIMS, cc -> cc.get("kills").getAsDouble() / Math.max(cc.get("deaths").getAsDouble(), 1)),
    COPS_AND_CRIMS_TEAM_DEATHMATCH_WINS(COPS_AND_CRIMS, cc -> cc.get("game_wins_deathmatch").getAsInt()),
    COPS_AND_CRIMS_TEAM_DEATHMATCH_KILLS(COPS_AND_CRIMS, cc -> cc.get("kills_deathmatch").getAsInt()),
    COPS_AND_CRIMS_TEAM_DEATHMATCH_KDR(COPS_AND_CRIMS, cc -> cc.get("kills_deathmatch").getAsDouble() / Math.max(cc.get("deaths_deathmatch").getAsDouble(), 1)),
    COPS_AND_CRIMS_GUN_GAME_WINS(COPS_AND_CRIMS, cc -> cc.get("game_wins_gungame").getAsInt()),
    COPS_AND_CRIMS_GUN_GAME_KILLS(COPS_AND_CRIMS, cc -> cc.get("kills_gungame").getAsInt()),
    COPS_AND_CRIMS_GUN_GAME_KDR(COPS_AND_CRIMS, cc -> cc.get("kills_gungame").getAsDouble() / Math.max(cc.get("deaths_gungame").getAsDouble(), 1)),
    COPS_AND_CRIMS_GUN_GAME_FASTEST_WIN(COPS_AND_CRIMS, cc -> cc.get("fastest_win_gungame").getAsInt(), 1),

    COPS_AND_CRIMS_WINS(COPS_AND_CRIMS, cc -> COPS_AND_CRIMS_DEFUSAL_WINS.derive(cc).intValue() + COPS_AND_CRIMS_TEAM_DEATHMATCH_WINS.derive(cc).intValue() + COPS_AND_CRIMS_GUN_GAME_WINS.derive(cc).intValue()),
    COPS_AND_CRIMS_KILLS(COPS_AND_CRIMS, cc -> COPS_AND_CRIMS_DEFUSAL_KILLS.derive(cc).intValue() + COPS_AND_CRIMS_TEAM_DEATHMATCH_KILLS.derive(cc).intValue() + COPS_AND_CRIMS_GUN_GAME_KILLS.derive(cc).intValue()),
    COPS_AND_CRIMS_KDR(COPS_AND_CRIMS, cc -> COPS_AND_CRIMS_KILLS.derive(cc).doubleValue() / Math.max(1, JsonUtil.getNullableDouble(cc.get("deaths")) + JsonUtil.getNullableDouble(cc.get("deaths_deathmatch")) + JsonUtil.getNullableDouble(cc.get("deaths_gungame")))),

    /**
     * Mega Walls leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/MegaWalls object.
     * @see LeaderboardCategory#MEGA_WALLS
     */

    MEGA_WALLS_COINS(MEGA_WALLS, mw -> mw.get("coins").getAsInt()),
    MEGA_WALLS_CLASS_POINTS(MEGA_WALLS, mw -> mw.get("class_points").getAsInt()),

    MEGA_WALLS_WINS(MEGA_WALLS, mw -> mw.get("wins").getAsInt()),
    MEGA_WALLS_WITHER_KILLS(MEGA_WALLS, mw -> mw.get("wither_kills").getAsInt()),
    MEGA_WALLS_WLR(MEGA_WALLS, mw -> mw.get("wins").getAsDouble() / Math.max(mw.get("losses").getAsDouble(), 1)),
    MEGA_WALLS_FINAL_KILLS(MEGA_WALLS, mw -> JsonUtil.getNullableInt(mw.get("final_kills")) + JsonUtil.getNullableInt(mw.get("finalKills"))),
    MEGA_WALLS_FKDR(MEGA_WALLS, mw -> MEGA_WALLS_FINAL_KILLS.derive(mw).doubleValue() / Math.max(1, JsonUtil.getNullableDouble(mw.get("final_deaths")) + JsonUtil.getNullableDouble(mw.get("finalDeaths")))),
    MEGA_WALLS_KILLS(MEGA_WALLS, mw -> mw.get("kills").getAsInt()),
    MEGA_WALLS_KDR(MEGA_WALLS, mw -> mw.get("kills").getAsDouble() / Math.max(mw.get("deaths").getAsDouble(), 1)),
    MEGA_WALLS_MYTHIC_FAVOR(MEGA_WALLS, mw -> mw.get("mythic_favor").getAsInt()),

    MEGA_WALLS_STANDARD_WINS(MEGA_WALLS, mw -> mw.get("wins_standard").getAsInt()),
    MEGA_WALLS_STANDARD_WITHER_KILLS(MEGA_WALLS, mw -> mw.get("wither_kills_standard").getAsInt()),
    MEGA_WALLS_STANDARD_WLR(MEGA_WALLS, mw -> mw.get("wins_standard").getAsDouble() / Math.max(mw.get("losses_standard").getAsDouble(), 1)),
    MEGA_WALLS_STANDARD_FINAL_KILLS(MEGA_WALLS, mw -> mw.get("final_kills_standard").getAsInt()),
    MEGA_WALLS_STANDARD_FKDR(MEGA_WALLS, mw -> mw.get("final_kills_standard").getAsDouble() / Math.max(mw.get("final_deaths_standard").getAsDouble(), 1)),
    MEGA_WALLS_STANDARD_KILLS(MEGA_WALLS, mw -> mw.get("kills_standard").getAsInt()),
    MEGA_WALLS_STANDARD_KDR(MEGA_WALLS, mw -> mw.get("kills_standard").getAsDouble() / Math.max(mw.get("deaths_standard").getAsDouble(), 1)),

    MEGA_WALLS_FACEOFF_WINS(MEGA_WALLS, mw -> mw.get("wins_face_off").getAsInt()),
    MEGA_WALLS_FACEOFF_WITHER_KILLS(MEGA_WALLS, mw -> mw.get("wither_kills_face_off").getAsInt()),
    MEGA_WALLS_FACEOFF_WLR(MEGA_WALLS, mw -> mw.get("wins_face_off").getAsDouble() / Math.max(mw.get("losses_face_off").getAsDouble(), 1)),
    MEGA_WALLS_FACEOFF_FINAL_KILLS(MEGA_WALLS, mw -> mw.get("final_kills_face_off").getAsInt()),
    MEGA_WALLS_FACEOFF_FKDR(MEGA_WALLS, mw -> mw.get("final_kills_face_off").getAsDouble() / Math.max(mw.get("final_deaths_face_off").getAsDouble(), 1)),
    MEGA_WALLS_FACEOFF_KILLS(MEGA_WALLS, mw -> mw.get("kills_face_off").getAsInt()),
    MEGA_WALLS_FACEOFF_KDR(MEGA_WALLS, mw -> mw.get("kills_face_off").getAsDouble() / Math.max(mw.get("deaths_face_off").getAsDouble(), 1)),

    /**
     * Smash Heroes leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/SuperSmash object.
     * @see LeaderboardCategory#SMASH_HEROES
     */

    SMASH_HEROES_COINS(SMASH_HEROES, sh -> sh.get("coins").getAsInt()),
    SMASH_HEROES_WINS(SMASH_HEROES, sh -> sh.get("wins").getAsInt()),
    SMASH_HEROES_WLR(SMASH_HEROES, sh -> sh.get("wins").getAsDouble() / Math.max(sh.get("losses").getAsDouble(), 1)),
    SMASH_HEROES_KDR(SMASH_HEROES, sh -> sh.get("kills").getAsDouble() / Math.max(sh.get("deaths").getAsDouble(), 1)),
    SMASH_HEROES_SMASH_LEVEL(SMASH_HEROES, sh -> sh.get("smashLevel").getAsInt()),

    /**
     * UHC Champions leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/UHC object.
     * @see LeaderboardCategory#UHC
     */

    UHC_COINS(UHC, uhc -> uhc.get("coins").getAsInt()),
    UHC_SCORE(UHC, uhc -> uhc.get("score").getAsInt()),
    //* wins + wins_solo + wins_no_diamonds + wins_brawl + wins_solo_brawl + wins_duo_brawl + wins_vanilla_doubles
    UHC_WINS(UHC, uhc -> JsonUtil.getNullableInt(uhc.get("wins")) + JsonUtil.getNullableInt(uhc.get("wins_solo")) + JsonUtil.getNullableInt(uhc.get("wins_no_diamonds")) + JsonUtil.getNullableInt(uhc.get("wins_brawl")) + JsonUtil.getNullableInt(uhc.get("wins_solo_brawl")) + JsonUtil.getNullableInt(uhc.get("wins_duo_brawl")) + JsonUtil.getNullableInt(uhc.get("wins_vanilla_doubles"))),
    UHC_KILLS(UHC, uhc -> JsonUtil.getNullableInt(uhc.get("kills")) + JsonUtil.getNullableInt(uhc.get("kills_solo")) + JsonUtil.getNullableInt(uhc.get("kills_no_diamonds")) + JsonUtil.getNullableInt(uhc.get("kills_brawl")) + JsonUtil.getNullableInt(uhc.get("kills_solo_brawl")) + JsonUtil.getNullableInt(uhc.get("kills_duo_brawl")) + JsonUtil.getNullableInt(uhc.get("kills_vanilla_doubles"))),
    UHC_KDR(UHC, uhc -> UHC_KILLS.derive(uhc).doubleValue() / Math.max(1, JsonUtil.getNullableDouble(uhc.get("deaths")) + JsonUtil.getNullableDouble(uhc.get("deaths_solo")) + JsonUtil.getNullableDouble(uhc.get("deaths_no_diamonds")) + JsonUtil.getNullableDouble(uhc.get("deaths_brawl")) + JsonUtil.getNullableDouble(uhc.get("deaths_solo_brawl")) + JsonUtil.getNullableDouble(uhc.get("deaths_duo_brawl")) + JsonUtil.getNullableDouble(uhc.get("deaths_vanilla_doubles")))),
    UHC_TEAMS_WINS(UHC, uhc -> uhc.get("wins").getAsInt()),
    UHC_TEAMS_KILLS(UHC, uhc -> uhc.get("kills").getAsInt()),
    UHC_TEAMS_KDR(UHC, uhc -> uhc.get("kills").getAsDouble() / Math.max(uhc.get("deaths").getAsDouble(), 1)),
    UHC_SOLO_WINS(UHC, uhc -> uhc.get("wins_solo").getAsInt()),
    UHC_SOLO_KILLS(UHC, uhc -> uhc.get("kills_solo").getAsInt()),
    UHC_SOLO_KDR(UHC, uhc -> uhc.get("kills_solo").getAsDouble() / Math.max(uhc.get("deaths_solo").getAsDouble(), 1)),

    /**
     * Speed UHC leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/SpeedUHC object.
     * @see LeaderboardCategory#SPEED_UHC
     */

    SPEED_UHC_SCORE(SPEED_UHC, suhc -> suhc.get("score").getAsInt()),
    SPEED_UHC_KILLS(SPEED_UHC, suhc -> suhc.get("kills").getAsInt()),
    SPEED_UHC_WINS(SPEED_UHC, suhc -> suhc.get("wins").getAsInt()),
    SPEED_UHC_WLR(SPEED_UHC, suhc -> suhc.get("wins").getAsDouble() / Math.max(suhc.get("losses").getAsDouble(), 1)),
    SPEED_UHC_KDR(SPEED_UHC, suhc -> suhc.get("kills").getAsDouble() / Math.max(suhc.get("deaths").getAsDouble(), 1)),

    /**
     * Warlords leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Battleground object.
     * @see LeaderboardCategory#WARLORDS
     */

    WARLORDS_COINS(WARLORDS, wl -> wl.get("coins").getAsInt()),
    WARLORDS_KILLS(WARLORDS, wl -> wl.get("kills").getAsInt()),
    WARLORDS_WINS(WARLORDS, wl -> wl.get("wins").getAsInt()),
    WARLORDS_WLR(WARLORDS, wl -> wl.get("wins").getAsDouble() / (JsonUtil.getNullableDouble(wl.get("mage_plays")) + JsonUtil.getNullableDouble(wl.get("warrior_plays")) + JsonUtil.getNullableDouble(wl.get("paladin_plays")) + JsonUtil.getNullableDouble(wl.get("shaman_plays")) - JsonUtil.getNullableDouble(wl.get("losses")))),
    WARLORDS_KDR(WARLORDS, wl -> wl.get("kills").getAsDouble() / Math.max(wl.get("deaths").getAsDouble(), 1)),
    WARLORDS_CAPTURE_THE_FLAG_WINS(WARLORDS, wl -> wl.get("wins_capturetheflag").getAsInt()),
    WARLORDS_CAPTURE_THE_FLAG_KILLS(WARLORDS, wl -> wl.get("kills_capturetheflag").getAsInt()),
    WARLORDS_CAPTURE_THE_FLAG_CAPTURES(WARLORDS, wl -> wl.get("flag_conquer_self").getAsInt()),
    WARLORDS_CAPTURE_THE_FLAG_RETURNS(WARLORDS, wl -> wl.get("flag_returns").getAsInt()),
    WARLORDS_DOMINATION_WINS(WARLORDS, wl -> wl.get("wins_domination").getAsInt()),
    WARLORDS_DOMINATION_KILLS(WARLORDS, wl -> wl.get("kills_domination").getAsInt()),
    WARLORDS_DOMINATION_CAPTURES(WARLORDS, wl -> wl.get("dom_point_captures").getAsInt()),
    WARLORDS_TEAM_DEATHMATCH_WINS(WARLORDS, wl -> wl.get("wins_teamdeathmatch").getAsInt()),
    WARLORDS_TEAM_DEATHMATCH_KILLS(WARLORDS, wl -> wl.get("kills_teamdeathmatch").getAsInt()),

    /**
     * Wool Games leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/WoolGames object.
     * @see LeaderboardCategory#WOOL_GAMES
     */

    WOOL_GAMES_WOOL(WOOL_GAMES, wg -> wg.get("coins").getAsInt()),
    WOOL_GAMES_LEVEL(WOOL_GAMES, wg -> wg.getAsJsonObject("progression").get("experience").getAsInt()),

    WOOL_GAMES_SHEEP_WARS_WINS(WOOL_GAMES, wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("wins").getAsInt()),
    WOOL_GAMES_SHEEP_WARS_KDR(WOOL_GAMES, wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("kills").getAsDouble() / Math.max(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("deaths").getAsDouble(), 1)),
    WOOL_GAMES_SHEEP_WARS_KILLS(WOOL_GAMES, wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("kills").getAsInt()),
    WOOL_GAMES_SHEEP_WARS_WLR(WOOL_GAMES, wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("wins").getAsDouble() / Math.max(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("losses").getAsDouble(), 1)),

    WOOL_GAMES_WOOL_WARS_WINS(WOOL_GAMES, wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins").getAsInt()),
    WOOL_GAMES_WOOL_WARS_KDR(WOOL_GAMES, wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("kills").getAsDouble() / Math.max(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("deaths").getAsDouble(), 1)),
    WOOL_GAMES_WOOL_WARS_KILLS(WOOL_GAMES, wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("kills").getAsInt()),
    WOOL_GAMES_WOOL_WARS_WLR(WOOL_GAMES, wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins").getAsDouble() / Math.max(JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("games_played")) - JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins")), 1)),

    WOOL_GAMES_CAPTURE_THE_WOOL_WINS(WOOL_GAMES, wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_wins").getAsInt()),
    WOOL_GAMES_CAPTURE_THE_WOOL_KDR(WOOL_GAMES, wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("kills").getAsDouble() / Math.max(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("deaths").getAsDouble(), 1)),
    WOOL_GAMES_CAPTURE_THE_WOOL_KILLS(WOOL_GAMES, wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("kills").getAsInt()),
    WOOL_GAMES_CAPTURE_THE_WOOL_WLR(WOOL_GAMES, wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_wins").getAsDouble() / Math.max(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_losses").getAsDouble(), 1)),

    WOOL_GAMES_WINS(WOOL_GAMES, wg -> WOOL_GAMES_SHEEP_WARS_WINS.derive(wg).intValue() + WOOL_GAMES_WOOL_WARS_WINS.derive(wg).intValue() + WOOL_GAMES_CAPTURE_THE_WOOL_WINS.derive(wg).intValue()),
    WOOL_GAMES_KILLS(WOOL_GAMES, wg -> WOOL_GAMES_SHEEP_WARS_KILLS.derive(wg).intValue() + WOOL_GAMES_WOOL_WARS_KILLS.derive(wg).intValue() + WOOL_GAMES_CAPTURE_THE_WOOL_KILLS.derive(wg).intValue()),

    WOOL_GAMES_WLR(WOOL_GAMES, wg -> WOOL_GAMES_WINS.derive(wg).doubleValue() / Math.max(1, JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("games_played")) - JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins")) + JsonUtil.getNullableInt(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("losses")) + JsonUtil.getNullableInt(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_losses")))),

    WOOL_GAMES_KDR(WOOL_GAMES, wg -> WOOL_GAMES_KILLS.derive(wg).doubleValue() / Math.max(1, JsonUtil.getNullableDouble(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("deaths")) + JsonUtil.getNullableDouble(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("deaths")) + JsonUtil.getNullableDouble(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("deaths")))),


    /**
     * Fishing leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/MainLobby/fishing object.
     * @see LeaderboardCategory#FISHING
     */

    FISHING_FISH_CAUGHT(FISHING, fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("fish")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("fish")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("fish"))),
    FISHING_TREASURE_CAUGHT(FISHING, fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("treasure")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("treasure")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("treasure"))),
    FISHING_JUNK_CAUGHT(FISHING, fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("junk")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("junk")) + JsonUtil.getNullableInt(fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("junk"))),
    FISHING_MYTHICAL_FISH_CAUGHT(FISHING, fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("selene")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("helios")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("nyx")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("zeus")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("aphrodite")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("archimedes")) + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("hades"))),
    FISHING_TOTAL_CAUGHT(FISHING, fish -> FISHING_FISH_CAUGHT.derive(fish).intValue() + FISHING_TREASURE_CAUGHT.derive(fish).intValue() + FISHING_JUNK_CAUGHT.derive(fish).intValue() + FISHING_MYTHICAL_FISH_CAUGHT.derive(fish).intValue()),

//    /**
//     * SkyBlock leaderboards.
//     * Derivation functions of leaderboards in this category take in /data of the active profile from SkyCrypt
//     * @see LeaderboardCategory#SKYBLOCK
//     */
//    SKYBLOCK_XP(SKYBLOCK, sb -> sb.getAsJsonObject("skyblock_level").get("xp").getAsInt()),
//    SKYBLOCK_CATACOMBS_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("catacombs")
//        .getAsJsonObject("level").get("xp").getAsDouble()),
//    SKYBLOCK_ARCHER_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("classes")
//        .getAsJsonObject("archer").getAsJsonObject("level").get("xp").getAsDouble()),
//    SKYBLOCK_BERSERK_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("classes")
//        .getAsJsonObject("berserk").getAsJsonObject("level").get("xp").getAsDouble()),
//    SKYBLOCK_HEALER_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("classes")
//        .getAsJsonObject("healer").getAsJsonObject("level").get("xp").getAsDouble()),
//    SKYBLOCK_MAGE_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("classes")
//        .getAsJsonObject("mage").getAsJsonObject("level").get("xp").getAsDouble()),
//    SKYBLOCK_TANK_XP(SKYBLOCK, sb -> sb.getAsJsonObject("dungeons").getAsJsonObject("classes")
//        .getAsJsonObject("tank").getAsJsonObject("level").get("xp").getAsDouble()),

    ;

    /**
     * Dynamically generated leaderboards
     */
    private static final List<DynamicLeaderboard> DYNAMIC_LEADERBOARDS = new ArrayList<>();

    /**
     * Create a new leaderboard with a default sort direction of descending
     * @param category The {@link LeaderboardCategory} this leaderboard should belong to
     * @param deriveFunction The function used to capture the leaderboard stat from the appropriate JSON object
     */
    Leaderboard(LeaderboardCategory category, Function<JsonObject, Number> deriveFunction) {
        this(category, deriveFunction, -1); // default to descending
    }

    /**
     * The {@link LeaderboardCategory} this leaderboard belongs to
     */
    @Getter
    private final LeaderboardCategory category;

    /**
     * Function that takes in a JsonObject of stats, and outputs the stat appropriate to the leaderboard.
     * <p>
     * The exact JsonObject which this function takes in depends on the leaderboard category.
     * For example, network leaderboards take in the {@code profile} object, whereas BedWars leaderboards take in the
     * {@code stats/Bedwars} object.
     */
    private final Function<JsonObject, Number> derive;

    /**
     * The direction this leaderboard sorts in: -1 is descending, 1 is ascending. Defaults to descending.
     */
    @Getter
    private final int sortDirection;

    /**
     * Derive a stat. Use this instead of the derive member, as this has additional safety.
     * @param object The JsonObject to derive the stat from
     * @return The player's stat, or 0 if none exists.
     */
    public Number derive(JsonObject object) {
        try {
            return this.derive.apply(object);
        } catch (Exception e) {
            return 0; // player doesn't have that stat
        }
    }

    /**
     * Get a leaderboard by name
     * @param name The name of the leaderboard to look up
     * @return The leaderboard matching the provided name, or null if none exists
     */
    public static Leaderboard get(String name) {
        for (Leaderboard leaderboard : Leaderboard.values()) {
            if (leaderboard.name().equals(name)) {
                return leaderboard;
            }
        }
        return null;
    }

    public static DynamicLeaderboard getDynamic(String name) {
        for (DynamicLeaderboard leaderboard : DYNAMIC_LEADERBOARDS) {
            if (leaderboard.name().equals(name)) {
                return leaderboard;
            }
        }
        return null;
    }

    /**
     * @param category      The {@link LeaderboardCategory} this leaderboard belongs to
     * @param derive        Function that takes in a JsonObject of stats, and outputs the stat appropriate to the leaderboard.
     *                      <p>
     *                      The exact JsonObject which this function takes in depends on the leaderboard category.
     *                      For example, network leaderboards take in the {@code profile} object, whereas BedWars leaderboards take in the
     *                      {@code stats/Bedwars} object.
     * @param sortDirection The direction this leaderboard sorts in: -1 is descending, 1 is ascending. Defaults to descending.
     */
    public record DynamicLeaderboard(String name, LeaderboardCategory category, Function<JsonObject, Object> derive,
                                     int sortDirection) {
    }
}
