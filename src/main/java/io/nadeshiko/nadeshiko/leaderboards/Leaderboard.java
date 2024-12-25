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
import io.nadeshiko.nadeshiko.util.JsonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Enum of leaderboards and functions to capture leaderboard data
 *
 * @author chloe
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
     * BedWars leaderboards.
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
    PIT_KDR(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("kills").getAsDouble() /
            Math.max(1, JsonUtil.getNullableDouble(pit.getAsJsonObject("pit_stats_ptl").get("deaths")))),
    PIT_WHEAT_FARMED(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("wheat_farmed").getAsInt()),
    PIT_RENOWN(PIT, pit -> pit.getAsJsonObject("profile").getAsJsonObject().get("renown").getAsInt()),
    PIT_ITEMS_FISHED(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").getAsJsonObject().get("fished_anything").getAsInt()),
    PIT_INGOTS_PICKED_UP(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").getAsJsonObject().get("ingots_picked_up").getAsInt()),
    PIT_HIGHEST_KILLSTREAK(PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").getAsJsonObject().get("max_streak").getAsInt()),
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


    /**
     * Arcade leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Arcade object.
     * @see LeaderboardCategory#ARCADE
     */
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
    ARCADE_COINS(ARCADE, ar -> ar.get("coins").getAsInt()),
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
    ARCADE_GALAXY_WARS_KDR(ARCADE, ar -> ar.get("sw_kills").getAsDouble() / ar.get("sw_deaths").getAsDouble()),
    ARCADE_HIDE_AND_SEEK_WINS(ARCADE, ar -> JsonUtil.getNullableInt(ar.get("party_pooper_hider_wins_hide_and_seek")) + JsonUtil.getNullableInt(ar.get("hider_wins_hide_and_seek"))),
    //ARCADE_HIDE_AND_SEEK_PROP_HUNT_WINS(ARCADE, ar -> ar.get("prop_hunt_hider_wins_hide_and_seek").getAsInt() + ar.get("prop_hunt_seeker_wins_hide_and_seek").getAsInt()),
    //ARCADE_HIDE_AND_SEEK_PARTY_POOPER_WINS(ARCADE, ar -> ar.get("party_pooper_hider_wins_hide_and_seek").getAsInt() + ar.get("party_pooper_seeker_wins_hide_and_seek").getAsInt()),
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
    Leaderboard(LeaderboardCategory category, Function<JsonObject, Object> deriveFunction) {
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
    private final Function<JsonObject, Object> derive;

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
    public Object derive(JsonObject object) {
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
