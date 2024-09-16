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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
     */
    NETWORK_FIRST_LOGIN(profile -> profile.get("first_login").getAsLong(), 1),
    NETWORK_NETWORK_LEVEL(profile -> profile.get("network_level").getAsFloat()),
    NETWORK_ACHIEVEMENT_POINTS(profile -> profile.get("achievement_points").getAsInt()),
    NETWORK_KARMA(profile -> profile.get("karma").getAsInt()),
    NETWORK_RANKS_GIFTED(profile -> profile.get("ranks_gifted").getAsInt()),
    NETWORK_QUESTS_COMPLETED(profile -> profile.get("quests_completed").getAsInt()),

    /**
     * BedWars leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Bedwars object.
     */
    BEDWARS_EXP(bw -> bw.get("Experience").getAsLong()),
    BEDWARS_TICKETS_EARNED(bw -> bw.getAsJsonObject("slumber").get("total_tickets_earned").getAsLong()),
    BEDWARS_COMPLETED_CHALLENGES(bw -> bw.get("total_challenges_completed").getAsInt()),
    BEDWARS_COLLECTED_EMERALDS(bw -> bw.get("emerald_resources_collected_bedwars").getAsInt()),
    BEDWARS_COLLECTED_DIAMONDS(bw -> bw.get("diamond_resources_collected_bedwars").getAsInt()),
    BEDWARS_WINS(bw -> bw.get("wins_bedwars").getAsInt()),
    BEDWARS_WLR(bw -> bw.get("wins_bedwars").getAsDouble() / bw.get("losses_bedwars").getAsDouble()),
    BEDWARS_FINALS(bw -> bw.get("final_kills_bedwars").getAsInt()),
    BEDWARS_FKDR(bw -> bw.get("final_kills_bedwars").getAsDouble() / bw.get("final_deaths_bedwars").getAsDouble()),
    BEDWARS_KILLS(bw -> bw.get("kills_bedwars").getAsInt()),
    BEDWARS_KDR(bw -> bw.get("kills_bedwars").getAsDouble() / bw.get("deaths_bedwars").getAsDouble()),
    BEDWARS_BEDS(bw -> bw.get("beds_broken_bedwars").getAsInt()),
    BEDWARS_BBLR(bw -> bw.get("beds_broken_bedwars").getAsDouble() / bw.get("beds_lost_bedwars").getAsDouble()),
    BEDWARS_SOLO_WINS(bw -> bw.get("eight_one_wins_bedwars").getAsInt()),
    BEDWARS_SOLO_WLR(bw -> bw.get("eight_one_wins_bedwars").getAsDouble() / bw.get("eight_one_losses_bedwars").getAsDouble()),
    BEDWARS_SOLO_FINALS(bw -> bw.get("eight_one_final_kills_bedwars").getAsInt()),
    BEDWARS_SOLO_FKDR(bw -> bw.get("eight_one_final_kills_bedwars").getAsDouble() / bw.get("eight_one_final_deaths_bedwars").getAsDouble()),
    BEDWARS_DOUBLES_WINS(bw -> bw.get("eight_two_wins_bedwars").getAsInt()),
    BEDWARS_DOUBLES_WLR(bw -> bw.get("eight_two_wins_bedwars").getAsDouble() / bw.get("eight_two_losses_bedwars").getAsDouble()),
    BEDWARS_DOUBLES_FINALS(bw -> bw.get("eight_two_final_kills_bedwars").getAsInt()),
    BEDWARS_DOUBLES_FKDR(bw -> bw.get("eight_two_final_kills_bedwars").getAsDouble() / bw.get("eight_two_final_deaths_bedwars").getAsDouble()),
    BEDWARS_THREES_WINS(bw -> bw.get("four_three_wins_bedwars").getAsInt()),
    BEDWARS_THREES_WLR(bw -> bw.get("four_three_wins_bedwars").getAsDouble() / bw.get("four_three_losses_bedwars").getAsDouble()),
    BEDWARS_THREES_FINALS(bw -> bw.get("four_three_final_kills_bedwars").getAsInt()),
    BEDWARS_THREES_FKDR(bw -> bw.get("four_three_final_kills_bedwars").getAsDouble() / bw.get("four_three_final_deaths_bedwars").getAsDouble()),
    BEDWARS_FOURS_WINS(bw -> bw.get("four_four_wins_bedwars").getAsInt()),
    BEDWARS_FOURS_WLR(bw -> bw.get("four_four_wins_bedwars").getAsDouble() / bw.get("four_four_losses_bedwars").getAsDouble()),
    BEDWARS_FOURS_FINALS(bw -> bw.get("four_four_final_kills_bedwars").getAsInt()),
    BEDWARS_FOURS_FKDR(bw -> bw.get("four_four_final_kills_bedwars").getAsDouble() / bw.get("four_four_final_deaths_bedwars").getAsDouble()),
    BEDWARS_FOURVFOUR_WINS(bw -> bw.get("two_four_wins_bedwars").getAsInt()),
    BEDWARS_FOURVFOUR_WLR(bw -> bw.get("two_four_wins_bedwars").getAsDouble() / bw.get("two_four_losses_bedwars").getAsDouble()),
    BEDWARS_FOURVFOUR_FINALS(bw -> bw.get("two_four_final_kills_bedwars").getAsInt()),
    BEDWARS_FOURVFOUR_FKDR(bw -> bw.get("two_four_final_kills_bedwars").getAsDouble() / bw.get("two_four_final_deaths_bedwars").getAsDouble()),

    /**
     * Duels leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Duels object.
     */
    DUELS_CLICKS(duels -> duels.get("melee_swings").getAsInt()),
    DUELS_WINS(duels -> duels.get("wins").getAsInt()),
    DUELS_WLR(duels -> duels.get("wins").getAsDouble() / duels.get("losses").getAsDouble()),
    DUELS_KILLS(duels -> duels.get("kills").getAsInt()),
    DUELS_DAMAGE_DEALT(duels -> duels.get("damage_dealt").getAsLong()),
    DUELS_HEALTH_REGENERATED(duels -> duels.get("health_regenerated").getAsLong()),
    DUELS_WINSTREAK(duels -> duels.get("current_winstreak").getAsInt()),
    DUELS_BEST_WINSTREAK(duels -> duels.get("best_overall_winstreak").getAsInt()),
    DUELS_BRIDGE_WINS(duels -> duels.get("bridge_duel_wins").getAsInt()),
    DUELS_BRIDGE_GOALS(duels -> duels.get("bridge_duel_goals").getAsInt()),
    DUELS_SW_WINS(duels -> duels.get("sw_duel_wins").getAsInt()),
    DUELS_CLASSIC_WINS(duels -> duels.get("classic_duel_wins").getAsInt()),
    DUELS_UHC_WINS(duels -> duels.get("uhc_duel_wins").getAsInt()),
    DUELS_SUMO_WINS(duels -> duels.get("sumo_duel_wins").getAsInt()),
    DUELS_PARKOUR_WINS(duels -> duels.get("parkour_eight_wins").getAsInt()),
    DUELS_BLITZ_WINS(duels -> duels.get("blitz_duel_wins").getAsInt()),
    DUELS_BOW_WINS(duels -> duels.get("bow_duel_wins").getAsInt()),
    DUELS_MW_WINS(duels -> duels.get("mw_duel_wins").getAsInt()),
    DUELS_BOWSPLEEF_WINS(duels -> duels.get("bowspleef_duel_wins").getAsInt()),
    DUELS_OP_WINS(duels -> duels.get("op_duel_wins").getAsInt()),
    DUELS_COMBO_WINS(duels -> duels.get("combo_duel_wins").getAsInt()),
    DUELS_BOXING_WINS(duels -> duels.get("boxing_duel_wins").getAsInt()),
    DUELS_NODEBUFF_WINS(duels -> duels.get("potion_duel_wins").getAsInt()),
    DUELS_ARENA_WINS(duels -> duels.get("duel_arena_wins").getAsInt()),

    /**
     * Pit leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/Pit/pit_stats_ptl object.
     */
    PIT_JOINS(pit -> pit.get("joins").getAsInt()),
    PIT_PLAYTIME(pit -> pit.get("playtime_minutes").getAsInt()),
    PIT_CHAT_MESSAGES(pit -> pit.get("chat_messages").getAsInt()),
    PIT_CLICKS(pit -> pit.get("left_clicks").getAsInt()),
    PIT_KILLS(pit -> pit.get("kills").getAsInt()),
    PIT_KDR(pit -> pit.get("kills").getAsDouble() / pit.get("deaths").getAsDouble()),
    PIT_WHEAT_FARMED(pit -> pit.get("wheat_farmed").getAsInt()),

    /**
     * SkyWars leaderboards.
     * Derivation functions of leaderboards in this category take in the /stats/SkyWars object.
     */
    SKYWARS_EXP(sw -> sw.get("skywars_experience").getAsInt()),
    SKYWARS_LONGEST_BOW_KILL(sw -> Math.max(sw.get("longest_bow_kill").getAsInt(), sw.get("longest_bow_kill_team").getAsInt())),
    SKYWARS_WINSTREAK(sw -> sw.get("win_streak").getAsInt()),
    SKYWARS_WINS(sw -> sw.get("wins").getAsInt()),
    SKYWARS_WLR(sw -> sw.get("wins").getAsDouble() / sw.get("losses").getAsDouble()),
    SKYWARS_KILLS(sw -> sw.get("kills").getAsInt()),
    SKYWARS_KDR(sw -> sw.get("kills").getAsDouble() / sw.get("deaths").getAsDouble()),
    SKYWARS_SOLO_NORMAL_WINS(sw -> sw.get("wins_solo_normal").getAsInt()),
    SKYWARS_SOLO_NORMAL_WLR(sw -> sw.get("wins_solo_normal").getAsDouble() / sw.get("losses_solo_normal").getAsDouble()),
    SKYWARS_SOLO_NORMAL_KILLS(sw -> sw.get("kills_solo_normal").getAsInt()),
    SKYWARS_SOLO_NORMAL_KDR(sw -> sw.get("kills_solo_normal").getAsDouble() / sw.get("deaths_solo_normal").getAsDouble()),
    SKYWARS_SOLO_INSANE_WINS(sw -> sw.get("wins_solo_insane").getAsInt()),
    SKYWARS_SOLO_INSANE_WLR(sw -> sw.get("wins_solo_insane").getAsDouble() / sw.get("losses_solo_insane").getAsDouble()),
    SKYWARS_SOLO_INSANE_KILLS(sw -> sw.get("kills_solo_insane").getAsInt()),
    SKYWARS_SOLO_INSANE_KDR(sw -> sw.get("kills_solo_insane").getAsDouble() / sw.get("deaths_solo_insane").getAsDouble()),
    SKYWARS_TEAM_NORMAL_WINS(sw -> sw.get("wins_team_normal").getAsInt()),
    SKYWARS_TEAM_NORMAL_WLR(sw -> sw.get("wins_team_normal").getAsDouble() / sw.get("losses_team_normal").getAsDouble()),
    SKYWARS_TEAM_NORMAL_KILLS(sw -> sw.get("kills_team_normal").getAsInt()),
    SKYWARS_TEAM_NORMAL_KDR(sw -> sw.get("kills_team_normal").getAsDouble() / sw.get("deaths_team_normal").getAsDouble()),
    SKYWARS_TEAM_INSANE_WINS(sw -> sw.get("wins_team_insane").getAsInt()),
    SKYWARS_TEAM_INSANE_WLR(sw -> sw.get("wins_team_insane").getAsDouble() / sw.get("losses_team_insane").getAsDouble()),
    SKYWARS_TEAM_INSANE_KILLS(sw -> sw.get("kills_team_insane").getAsInt()),
    SKYWARS_TEAM_INSANE_KDR(sw -> sw.get("kills_team_insane").getAsDouble() / sw.get("deaths_team_insane").getAsDouble()),



    ;

    /**
     * Create a new leaderboard with a default sort direction of descending
     * @param deriveFunction The function used to capture the leaderboard stat from the appropriate JSON object
     */
    Leaderboard(Function<JsonObject, Object> deriveFunction) {
        this(deriveFunction, -1); // default to descending
    }

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
}
