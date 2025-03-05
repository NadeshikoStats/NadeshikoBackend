/*
 * This file is a part of the nadeshiko project. nadeshiko is free software, licensed under the MIT license.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.nadeshiko.nadeshiko.util.JsonUtil;

import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.NETWORK;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.BLITZ;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.BEDWARS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.DUELS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.SKYWARS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.PIT;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.BUILD_BATTLE;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.WOOL_GAMES;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.FISHING;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.WARLORDS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.MURDER_MYSTERY;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.TNT_GAMES;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.ARENA_BRAWL;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.ARCADE;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.WALLS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.VAMPIREZ;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.QUAKECRAFT;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.PAINTBALL;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.SPEED_UHC;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.UHC;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.TURBO_KART_RACERS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.COPS_AND_CRIMS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.MEGA_WALLS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.SMASH_HEROES;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.ACHIEVEMENTS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.REWARDS;
import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.GUILDS;

public class LeaderboardRegistry {
    private static final List<String> BLITZ_KITS = Arrays.asList(
            "arachnologist", "archer", "armorer", "astronaut", "baker", "blaze", "creepertamer", "diver", "donkeytamer",
            "farmer", "fisherman", "florist", "golem", "guardian", "horsetamer", "hunter", "hype train", "jockey",
            "knight", "meatmaster", "milkman", "necromancer", "paladin", "phoenix", "pigman", "ranger", "reaper",
            "reddragon", "rogue", "scout", "shadow knight", "shark", "slimeyslime", "snowman", "speleologist", "tim",
            "toxicologist", "troll", "viking", "warlock", "warrior", "wolftamer", "rambo"
    // Add all kit names here
    );

    private static final List<String> COPS_AND_CRIMS_GUNS = Arrays.asList(
            "autoShotgun", "bullpup", "carbine", "handgun", "magnum", "pistol", "rifle", "scopedRifle", "shotgun",
            "smg", "sniper");

    private static final List<String> MEGA_WALLS_CLASSES = Arrays.asList(
            "angel", "arcanist", "assassin", "automaton", "blaze", "cow", "creeper", "dreadlord", "dragon", "enderman",
            "golem", "herobrine", "hunter", "moleman", "phoenix", "pigman", "pirate", "renegade", "shaman", "shark",
            "skeleton", "snowman", "spider", "squid", "werewolf", "zombie");

    // Make the map public static so Leaderboard can access it
    public static final Map<String, Leaderboard> LEADERBOARDS = new HashMap<>();

    // Leaderboard caps. Default cap is 20,000.
    private static final int UNLIMITED_CAP = -1; // For unlimited leaderboards. A bad idea!
    private static final int MASSIVE_CAP = 100_000; // For network-wide and the main stat for a game
    private static final int LARGE_CAP = 50_000; // For leaderboards that people care about more than the average leaderboard
    private static final int RATIO_CAP = 10_000; // For K/D R and W/L R
    private static final int KIT_CAP = 5_000; // For systematically generated stats (CvC gun stats, Blitz kit stats,
                                              // etc.)
    private static final int TESTING_CAP = 10;

    /*
     * Adding a leaderboard to the lookup table allows us to use it in other
     * leaderboards' derive functions.
     */
    private static Leaderboard addToLookup(Leaderboard lb) {
        LEADERBOARDS.put(lb.getName(), lb);
        return lb;
    }

    public static void registerAll() {
        registerNetworkLeaderboards();
        registerBedWarsLeaderboards();
        registerDuelsLeaderboards();
        registerSkyWarsLeaderboards();
        registerPitLeaderboards();
        registerBuildBattleLeaderboards();
        registerMurderMysteryLeaderboards();
        registerTNTGamesLeaderboards();
        registerArcadeLeaderboards();
        registerArenaBrawlLeaderboards();
        registerPaintballLeaderboards();
        registerQuakecraftLeaderboards();
        registerTurboKartRacersLeaderboards();
        registerVampireZLeaderboards();
        registerWallsLeaderboards();
        registerCopsAndCrimsLeaderboards();
        registerMegaWallsLeaderboards();
        registerSmashHeroesLeaderboards();
        registerUHCLeaderboards();
        registerSpeedUHCLeaderboards();
        registerWarlordsLeaderboards();
        registerWoolGamesLeaderboards();
        registerFishingLeaderboards();
        registerBlitzLeaderboards();
        registerGuildLeaderboards();


        // System.out.println("=== All the leaderboards ===");
        // LEADERBOARDS.keySet().stream()
        // .sorted()
        // .forEach(System.out::println);
        // System.out.println("Total leaderboards: " + LEADERBOARDS.size());
    }

    private static void registerBlitzLeaderboards() {
        // Register base stats first
        addToLookup(new Leaderboard("BLITZ_SOLO_WINS", BLITZ,
                bsg -> bsg.get("wins_solo_normal").getAsInt()));

        addToLookup(new Leaderboard("BLITZ_TEAM_WINS", BLITZ,
                bsg -> bsg.get("wins_teams_normal").getAsInt()));

        addToLookup(new Leaderboard("BLITZ_KILLS", BLITZ,
                bsg -> bsg.get("kills").getAsInt()));

        addToLookup(new Leaderboard("BLITZ_TEAM_KILLS", BLITZ,
                bsg -> bsg.get("kills_teams_normal").getAsInt()));

        // Then add derived stats that depend on the base stats
        new Leaderboard("BLITZ_WINS", BLITZ, bsg -> LEADERBOARDS.get("BLITZ_SOLO_WINS").derive(bsg).intValue() +
                LEADERBOARDS.get("BLITZ_TEAM_WINS").derive(bsg).intValue());

        new Leaderboard("BLITZ_SOLO_KILLS", BLITZ, bsg -> LEADERBOARDS.get("BLITZ_KILLS").derive(bsg).intValue() -
                LEADERBOARDS.get("BLITZ_TEAM_KILLS").derive(bsg).intValue());

        new Leaderboard("BLITZ_KDR", BLITZ, bsg -> LEADERBOARDS.get("BLITZ_KILLS").derive(bsg).doubleValue() /
                Math.max(bsg.get("deaths").getAsDouble(), 1));

        new Leaderboard("BLITZ_COINS", BLITZ,
                bsg -> bsg.get("coins").getAsInt());

        new Leaderboard("BLITZ_DAMAGE_DEALT", BLITZ,
                bsg -> bsg.get("damage").getAsInt());

        new Leaderboard("BLITZ_CHESTS_OPENED", BLITZ,
                bsg -> bsg.get("chests_opened").getAsInt());

        new Leaderboard("BLITZ_STARS_FOUND", ACHIEVEMENTS,
                ap -> ap.get("blitz_treasure_seeker").getAsInt());

        // Number of kits you have with prestige 2+
        new Leaderboard("BLITZ_PRESTIGE_TWOS", BLITZ,
                bsg -> {
                    int prestigeTwos = 0;
                    for (String kit : BLITZ_KITS) {
                        String prestigeKey = "p" + kit;
                        if (bsg.has(prestigeKey) && bsg.get(prestigeKey).getAsInt() >= 2) {
                            prestigeTwos++;
                        }
                    }
                    return prestigeTwos;
                }, -1);

        // Kit-specific leaderboards!
        for (String kit : BLITZ_KITS) {

            String blitzKitUpper = kit.toUpperCase().replace(" ", "_");
            new Leaderboard("BLITZ_" + blitzKitUpper + "_EXP", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("exp_" + kit)), -1, KIT_CAP);

            new Leaderboard("BLITZ_" + blitzKitUpper + "_KILLS", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("kills_" + kit)), -1, KIT_CAP);

            new Leaderboard("BLITZ_" + blitzKitUpper + "_WINS", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("wins_" + kit)) +
                            JsonUtil.getNullableInt(bsg.get("wins_teams_" + kit)),
                    -1, KIT_CAP);

            new Leaderboard("BLITZ_" + blitzKitUpper + "_PLAYTIME", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("time_played_" + kit)), -1, KIT_CAP);

            new Leaderboard("BLITZ_" + blitzKitUpper + "_DAMAGE_DEALT", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("damage_" + kit)), -1, KIT_CAP);

            new Leaderboard("BLITZ_" + blitzKitUpper + "_DAMAGE_TAKEN", BLITZ,
                    bsg -> JsonUtil.getNullableInt(bsg.get("damage_taken_" + kit)), -1, KIT_CAP);

        }

        new Leaderboard("BLITZ_RANDOM_KILLS", BLITZ, // These have different formatting for some reason???
                bsg -> bsg.get("kills_random").getAsInt(), -1, KIT_CAP);

        new Leaderboard("BLITZ_RANDOM_WINS", BLITZ,
                bsg -> bsg.get("random_wins").getAsInt(), -1, KIT_CAP);

    }

    private static void registerNetworkLeaderboards() {
        new Leaderboard("NETWORK_FIRST_LOGIN", NETWORK,
                profile -> profile.get("first_login").getAsLong(), 1, MASSIVE_CAP);
        new Leaderboard("NETWORK_NETWORK_LEVEL", NETWORK,
                profile -> profile.get("network_level").getAsFloat(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_ACHIEVEMENT_POINTS", NETWORK,
                profile -> profile.get("achievement_points").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_KARMA", NETWORK,
                profile -> profile.get("karma").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_RANKS_GIFTED", NETWORK,
                profile -> profile.get("ranks_gifted").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_QUESTS_COMPLETED", NETWORK,
                profile -> profile.get("quests_completed").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_REWARDS_CURRENT_STREAK", REWARDS,
                profile -> profile.get("current_reward_streak").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("NETWORK_REWARDS_HIGHEST_STREAK", REWARDS,
                profile -> profile.get("highest_reward_streak").getAsInt(), -1, MASSIVE_CAP);
    }

    private static void registerBedWarsLeaderboards() {
        // General stats
        new Leaderboard("BEDWARS_EXP", BEDWARS, bw -> bw.get("Experience").getAsLong(), -1, MASSIVE_CAP);
        new Leaderboard("BEDWARS_TICKETS_EARNED", BEDWARS,
                bw -> bw.getAsJsonObject("slumber").get("total_tickets_earned").getAsLong());
        new Leaderboard("BEDWARS_COMPLETED_CHALLENGES", BEDWARS, bw -> bw.get("total_challenges_completed").getAsInt());
        new Leaderboard("BEDWARS_COLLECTED_EMERALDS", BEDWARS,
                bw -> bw.get("emerald_resources_collected_bedwars").getAsInt());
        new Leaderboard("BEDWARS_COLLECTED_DIAMONDS", BEDWARS,
                bw -> bw.get("diamond_resources_collected_bedwars").getAsInt());
        new Leaderboard("BEDWARS_WINSTREAK", BEDWARS, bw -> bw.get("winstreak").getAsInt());
        new Leaderboard("BEDWARS_WINS", BEDWARS, bw -> bw.get("wins_bedwars").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("BEDWARS_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("losses_bedwars")), 1));
        new Leaderboard("BEDWARS_FINALS", BEDWARS, bw -> bw.get("final_kills_bedwars").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("BEDWARS_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_KILLS", BEDWARS, bw -> bw.get("kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_KDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_BEDS", BEDWARS, bw -> bw.get("beds_broken_bedwars").getAsInt());
        new Leaderboard("BEDWARS_BBLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("beds_broken_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("beds_lost_bedwars")), 1));
        new Leaderboard("BEDWARS_SOLO_WINSTREAK", BEDWARS, bw -> bw.get("eight_one_winstreak").getAsInt());
        new Leaderboard("BEDWARS_SOLO_WINS", BEDWARS, bw -> bw.get("eight_one_wins_bedwars").getAsInt());
        new Leaderboard("BEDWARS_SOLO_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("eight_one_wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("eight_one_losses_bedwars")), 1));
        new Leaderboard("BEDWARS_SOLO_FINALS", BEDWARS, bw -> bw.get("eight_one_final_kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_SOLO_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("eight_one_final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("eight_one_final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_DOUBLES_WINSTREAK", BEDWARS, bw -> bw.get("eight_two_winstreak").getAsInt());
        new Leaderboard("BEDWARS_DOUBLES_WINS", BEDWARS, bw -> bw.get("eight_two_wins_bedwars").getAsInt());
        new Leaderboard("BEDWARS_DOUBLES_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("eight_two_wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("eight_two_losses_bedwars")), 1));
        new Leaderboard("BEDWARS_DOUBLES_FINALS", BEDWARS, bw -> bw.get("eight_two_final_kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_DOUBLES_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("eight_two_final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("eight_two_final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_THREES_WINSTREAK", BEDWARS, bw -> bw.get("four_three_winstreak").getAsInt());
        new Leaderboard("BEDWARS_THREES_WINS", BEDWARS, bw -> bw.get("four_three_wins_bedwars").getAsInt());
        new Leaderboard("BEDWARS_THREES_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("four_three_wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("four_three_losses_bedwars")), 1));
        new Leaderboard("BEDWARS_THREES_FINALS", BEDWARS, bw -> bw.get("four_three_final_kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_THREES_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("four_three_final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("four_three_final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_FOURS_WINSTREAK", BEDWARS, bw -> bw.get("four_four_winstreak").getAsInt());
        new Leaderboard("BEDWARS_FOURS_WINS", BEDWARS, bw -> bw.get("four_four_wins_bedwars").getAsInt());
        new Leaderboard("BEDWARS_FOURS_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("four_four_wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("four_four_losses_bedwars")), 1));
        new Leaderboard("BEDWARS_FOURS_FINALS", BEDWARS, bw -> bw.get("four_four_final_kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_FOURS_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("four_four_final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("four_four_final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_FOURVFOUR_WINSTREAK", BEDWARS, bw -> bw.get("two_four_winstreak").getAsInt());
        new Leaderboard("BEDWARS_FOURVFOUR_WINS", BEDWARS, bw -> bw.get("two_four_wins_bedwars").getAsInt());
        new Leaderboard("BEDWARS_FOURVFOUR_WLR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("two_four_wins_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("two_four_losses_bedwars")), 1));
        new Leaderboard("BEDWARS_FOURVFOUR_FINALS", BEDWARS, bw -> bw.get("two_four_final_kills_bedwars").getAsInt());
        new Leaderboard("BEDWARS_FOURVFOUR_FKDR", BEDWARS,
                bw -> JsonUtil.getNullableDouble(bw.get("two_four_final_kills_bedwars"))
                        / Math.max(JsonUtil.getNullableDouble(bw.get("two_four_final_deaths_bedwars")), 1));
        new Leaderboard("BEDWARS_COLLECTED_IRON", BEDWARS, bw -> bw.get("iron_resources_collected_bedwars").getAsInt());
        new Leaderboard("BEDWARS_COLLECTED_GOLD", BEDWARS, bw -> bw.get("gold_resources_collected_bedwars").getAsInt());
        new Leaderboard("BEDWARS_TOKENS", BEDWARS, bw -> bw.get("coins").getAsInt());
    }

    private static void registerDuelsLeaderboards() {
        new Leaderboard("DUELS_CLICKS", DUELS, duels -> duels.get("melee_swings").getAsInt());
        new Leaderboard("DUELS_WINS", DUELS, duels -> duels.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("DUELS_WLR", DUELS,
                duels -> JsonUtil.getNullableDouble(duels.get("wins"))
                        / Math.max(JsonUtil.getNullableDouble(duels.get("losses")), 1));
        new Leaderboard("DUELS_KILLS", DUELS, duels -> duels.get("kills").getAsInt());
        new Leaderboard("DUELS_DAMAGE_DEALT", DUELS, duels -> duels.get("damage_dealt").getAsLong());
        new Leaderboard("DUELS_HEALTH_REGENERATED", DUELS, duels -> duels.get("health_regenerated").getAsLong());
        new Leaderboard("DUELS_WINSTREAK", DUELS, duels -> duels.get("current_winstreak").getAsInt());
        new Leaderboard("DUELS_BEST_WINSTREAK", DUELS, duels -> duels.get("best_overall_winstreak").getAsInt());
        new Leaderboard("DUELS_BRIDGE_WINS", DUELS, duels -> duels.get("bridge_duel_wins").getAsInt());
        new Leaderboard("DUELS_BRIDGE_GOALS", DUELS, duels -> duels.get("bridge_duel_goals").getAsInt());
        new Leaderboard("DUELS_SW_WINS", DUELS, duels -> duels.get("sw_duel_wins").getAsInt());
        new Leaderboard("DUELS_CLASSIC_WINS", DUELS, duels -> duels.get("classic_duel_wins").getAsInt());
        new Leaderboard("DUELS_UHC_WINS", DUELS, duels -> duels.get("uhc_duel_wins").getAsInt());
        new Leaderboard("DUELS_SUMO_WINS", DUELS, duels -> duels.get("sumo_duel_wins").getAsInt());
        new Leaderboard("DUELS_PARKOUR_WINS", DUELS, duels -> duels.get("parkour_eight_wins").getAsInt());
        new Leaderboard("DUELS_BLITZ_WINS", DUELS, duels -> duels.get("blitz_duel_wins").getAsInt());
        new Leaderboard("DUELS_BOW_WINS", DUELS, duels -> duels.get("bow_duel_wins").getAsInt());
        new Leaderboard("DUELS_MW_WINS", DUELS, duels -> duels.get("mw_duel_wins").getAsInt());
        new Leaderboard("DUELS_BOWSPLEEF_WINS", DUELS, duels -> duels.get("bowspleef_duel_wins").getAsInt());
        new Leaderboard("DUELS_OP_WINS", DUELS, duels -> duels.get("op_duel_wins").getAsInt());
        new Leaderboard("DUELS_COMBO_WINS", DUELS, duels -> duels.get("combo_duel_wins").getAsInt());
        new Leaderboard("DUELS_BOXING_WINS", DUELS, duels -> duels.get("boxing_duel_wins").getAsInt());
        new Leaderboard("DUELS_NODEBUFF_WINS", DUELS, duels -> duels.get("potion_duel_wins").getAsInt());
        new Leaderboard("DUELS_ARENA_WINS", DUELS, duels -> duels.get("duel_arena_wins").getAsInt());
        new Leaderboard("DUELS_TOKENS", DUELS, duels -> duels.get("coins").getAsInt());
    }

    private static void registerSkyWarsLeaderboards() {
        new Leaderboard("SKYWARS_EXP", SKYWARS, sw -> sw.get("skywars_experience").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("SKYWARS_WINS", SKYWARS, sw -> sw.get("wins").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("SKYWARS_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses")), 1));
        new Leaderboard("SKYWARS_KILLS", SKYWARS, sw -> sw.get("kills").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("SKYWARS_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths")), 1));
        new Leaderboard("SKYWARS_SOLO_NORMAL_WINS", SKYWARS, sw -> sw.get("wins_solo_normal").getAsInt());
        new Leaderboard("SKYWARS_SOLO_NORMAL_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins_solo_normal"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses_solo_normal")), 1));
        new Leaderboard("SKYWARS_SOLO_NORMAL_KILLS", SKYWARS, sw -> sw.get("kills_solo_normal").getAsInt());
        new Leaderboard("SKYWARS_SOLO_NORMAL_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills_solo_normal"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths_solo_normal")), 1));
        new Leaderboard("SKYWARS_SOLO_INSANE_WINS", SKYWARS, sw -> sw.get("wins_solo_insane").getAsInt());
        new Leaderboard("SKYWARS_SOLO_INSANE_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins_solo_insane"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses_solo_insane")), 1));
        new Leaderboard("SKYWARS_SOLO_INSANE_KILLS", SKYWARS, sw -> sw.get("kills_solo_insane").getAsInt());
        new Leaderboard("SKYWARS_SOLO_INSANE_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills_solo_insane"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths_solo_insane")), 1));
        new Leaderboard("SKYWARS_TEAM_NORMAL_WINS", SKYWARS, sw -> sw.get("wins_team_normal").getAsInt());
        new Leaderboard("SKYWARS_TEAM_NORMAL_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins_team_normal"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses_team_normal")), 1));
        new Leaderboard("SKYWARS_TEAM_NORMAL_KILLS", SKYWARS, sw -> sw.get("kills_team_normal").getAsInt());
        new Leaderboard("SKYWARS_TEAM_NORMAL_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills_team_normal"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths_team_normal")), 1));
        new Leaderboard("SKYWARS_TEAM_INSANE_WINS", SKYWARS, sw -> sw.get("wins_team_insane").getAsInt());
        new Leaderboard("SKYWARS_TEAM_INSANE_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins_team_insane"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses_team_insane")), 1));
        new Leaderboard("SKYWARS_TEAM_INSANE_KILLS", SKYWARS, sw -> sw.get("kills_team_insane").getAsInt());
        new Leaderboard("SKYWARS_TEAM_INSANE_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills_team_insane"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths_team_insane")), 1));
        new Leaderboard("SKYWARS_COINS", SKYWARS, sw -> sw.get("coins").getAsInt());
        new Leaderboard("SKYWARS_TOKENS", SKYWARS, sw -> sw.get("cosmetic_tokens").getAsInt());
        new Leaderboard("SKYWARS_LAB_WINS", SKYWARS, sw -> sw.get("wins_lab").getAsInt());
        new Leaderboard("SKYWARS_LAB_WLR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("wins_lab"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("losses_lab")), 1));
        new Leaderboard("SKYWARS_LAB_KILLS", SKYWARS, sw -> sw.get("kills_lab").getAsInt());
        new Leaderboard("SKYWARS_LAB_KDR", SKYWARS,
                sw -> JsonUtil.getNullableDouble(sw.get("kills_lab"))
                        / Math.max(JsonUtil.getNullableDouble(sw.get("deaths_lab")), 1));
        new Leaderboard("SKYWARS_LUCKY_BLOCK_WINS", SKYWARS, sw -> sw.get("lab_win_lucky_blocks_lab").getAsInt());
    }

    private static void registerPitLeaderboards() {
        addToLookup(new Leaderboard("PIT_KILLS", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("kills").getAsInt()));

        addToLookup(new Leaderboard("PIT_KDR", PIT, pit -> LEADERBOARDS.get("PIT_KILLS").derive(pit).intValue() /
                Math.max(1, JsonUtil.getNullableDouble(pit.getAsJsonObject("pit_stats_ptl").get("deaths")))));
        new Leaderboard("PIT_EXP", PIT, pit -> pit.getAsJsonObject("profile").get("xp").getAsLong(), -1, MASSIVE_CAP);
        new Leaderboard("PIT_GOLD", PIT, pit -> pit.getAsJsonObject("profile").get("cash").getAsDouble());
        new Leaderboard("PIT_DAMAGE_DEALT", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("damage_dealt").getAsInt());
        new Leaderboard("PIT_JOINS", PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("joins").getAsInt());
        new Leaderboard("PIT_PLAYTIME", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("playtime_minutes").getAsInt());
        new Leaderboard("PIT_CHAT_MESSAGES", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("chat_messages").getAsInt());
        new Leaderboard("PIT_CLICKS", PIT, pit -> pit.getAsJsonObject("pit_stats_ptl").get("left_clicks").getAsInt());
        new Leaderboard("PIT_NIGHT_QUESTS_COMPLETED", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("night_quests_completed").getAsInt());
        new Leaderboard("PIT_CONTRACTS_COMPLETED", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("contracts_completed").getAsInt());
        new Leaderboard("PIT_WHEAT_FARMED", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("wheat_farmed").getAsInt());
        new Leaderboard("PIT_RENOWN", PIT, pit -> pit.getAsJsonObject("profile").get("renown").getAsInt());
        new Leaderboard("PIT_ITEMS_FISHED", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("fished_anything").getAsInt());
        new Leaderboard("PIT_INGOTS_PICKED_UP", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("ingots_picked_up").getAsInt());
        new Leaderboard("PIT_LAUNCHER_LAUNCHES", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("launched_by_launchers").getAsInt());
        new Leaderboard("PIT_HIGHEST_KILLSTREAK", PIT,
                pit -> pit.getAsJsonObject("pit_stats_ptl").get("max_streak").getAsInt());
        new Leaderboard("PIT_BOUNTY", PIT, pit -> {
            int totalBounty = 0;
            JsonArray bounties = pit.getAsJsonObject("profile").getAsJsonArray("bounties");
            for (JsonElement element : bounties) {
                JsonObject bounty = element.getAsJsonObject();
                totalBounty += bounty.get("amount").getAsInt();
            }
            return totalBounty;
        });
        new Leaderboard("PIT_ITEMS_ENCHANTED", PIT,
                pit -> JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier1")) +
                        JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier2")) +
                        JsonUtil.getNullableInt(pit.getAsJsonObject("pit_stats_ptl").get("enchanted_tier3")));
    }

    private static void registerBuildBattleLeaderboards() {
        new Leaderboard("BUILD_BATTLE_WINS", BUILD_BATTLE, bb -> bb.get("wins").getAsInt());
        new Leaderboard("BUILD_BATTLE_SCORE", BUILD_BATTLE, bb -> bb.get("score").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("BUILD_BATTLE_VOTES", BUILD_BATTLE, bb -> bb.get("total_votes").getAsInt());
        new Leaderboard("BUILD_BATTLE_GTB_WINS", BUILD_BATTLE, bb -> bb.get("wins_guess_the_build").getAsInt());
        new Leaderboard("BUILD_BATTLE_GTB_CORRECT_GUESSES", BUILD_BATTLE, bb -> bb.get("correct_guesses").getAsInt());
        new Leaderboard("BUILD_BATTLE_SPEED_BUILDERS_WINS", BUILD_BATTLE,
                bb -> bb.get("wins_speed_builders").getAsInt());
        new Leaderboard("BUILD_BATTLE_TOKENS", BUILD_BATTLE, bb -> bb.get("coins").getAsInt());
        new Leaderboard("BUILD_BATTLE_SOLO_WINS", BUILD_BATTLE, bb -> bb.get("wins_solo_normal").getAsInt());
        new Leaderboard("BUILD_BATTLE_TEAM_WINS", BUILD_BATTLE, bb -> bb.get("wins_teams_normal").getAsInt());
        new Leaderboard("BUILD_BATTLE_PRO_WINS", BUILD_BATTLE, bb -> bb.get("wins_solo_pro").getAsInt());
    }

    private static void registerMurderMysteryLeaderboards() {
        new Leaderboard("MURDER_MYSTERY_KILLS", MURDER_MYSTERY, mm -> mm.get("kills").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("MURDER_MYSTERY_WINS", MURDER_MYSTERY, mm -> mm.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("MURDER_MYSTERY_MURDERER_WINS", MURDER_MYSTERY, mm -> mm.get("murderer_wins").getAsInt());
        new Leaderboard("MURDER_MYSTERY_DETECTIVE_WINS", MURDER_MYSTERY, mm -> mm.get("detective_wins").getAsInt());
        new Leaderboard("MURDER_MYSTERY_CLASSIC_WINS", MURDER_MYSTERY, mm -> mm.get("wins_MURDER_CLASSIC").getAsInt());
        new Leaderboard("MURDER_MYSTERY_DOUBLE_UP_WINS", MURDER_MYSTERY,
                mm -> mm.get("wins_MURDER_DOUBLE_UP").getAsInt());
        new Leaderboard("MURDER_MYSTERY_ASSASSINS_WINS", MURDER_MYSTERY,
                mm -> mm.get("wins_MURDER_ASSASSINS").getAsInt());
        new Leaderboard("MURDER_MYSTERY_INFECTION_WINS", MURDER_MYSTERY,
                mm -> mm.get("wins_MURDER_INFECTION").getAsInt());
    }

    private static void registerTNTGamesLeaderboards() {
        new Leaderboard("TNT_GAMES_WINS", TNT_GAMES, tnt -> tnt.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("TNT_GAMES_BOWSPLEEF_WINS", TNT_GAMES, tnt -> tnt.get("wins_bowspleef").getAsInt());
        new Leaderboard("TNT_GAMES_PVPRUN_WINS", TNT_GAMES, tnt -> tnt.get("wins_pvprun").getAsInt());
        new Leaderboard("TNT_GAMES_PVPRUN_KILLS", TNT_GAMES, tnt -> tnt.get("kills_pvprun").getAsInt());
        new Leaderboard("TNT_GAMES_PVPRUN_LONGEST", TNT_GAMES, tnt -> tnt.get("record_pvprun").getAsInt());
        new Leaderboard("TNT_GAMES_TNTRUN_WINS", TNT_GAMES, tnt -> tnt.get("wins_tntrun").getAsInt());
        new Leaderboard("TNT_GAMES_TNTRUN_LONGEST", TNT_GAMES, tnt -> tnt.get("record_tntrun").getAsInt());
        new Leaderboard("TNT_GAMES_TNTTAG_WINS", TNT_GAMES, tnt -> tnt.get("wins_tntag").getAsInt());
        new Leaderboard("TNT_GAMES_TNTTAG_KILLS", TNT_GAMES, tnt -> tnt.get("kills_tntag").getAsInt());
        new Leaderboard("TNT_GAMES_WIZARDS_WINS", TNT_GAMES, tnt -> tnt.get("wins_capture").getAsInt());
        new Leaderboard("TNT_GAMES_WIZARDS_KILLS", TNT_GAMES, tnt -> tnt.get("kills_capture").getAsInt());
        new Leaderboard("TNT_GAMES_TOKENS", TNT_GAMES, tnt -> tnt.get("coins").getAsInt());
        new Leaderboard("TNT_GAMES_WIZARDS_POINTS_CAPTURED", TNT_GAMES, tnt -> tnt.get("points_capture").getAsInt());
    }

    private static void registerArcadeLeaderboards() {
        new Leaderboard("ARCADE_COINS", ARCADE, ar -> ar.get("coins").getAsInt());

        new Leaderboard("ARCADE_DROPPER_BEST_TIME", ARCADE,
                ar -> ar.getAsJsonObject("dropper").get("fastest_game").getAsInt(), 1);
        addToLookup(new Leaderboard("ARCADE_DROPPER_WINS", ARCADE,
                ar -> ar.getAsJsonObject("dropper").get("wins").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_HYPIXEL_SAYS_WINS", ARCADE, ar -> ar.get("wins_simon_says").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_MINI_WALLS_WINS", ARCADE, ar -> ar.get("wins_mini_walls").getAsInt()));
        new Leaderboard("ARCADE_MINI_WALLS_KILLS", ARCADE, ar -> ar.get("kills_mini_walls").getAsInt());
        addToLookup(new Leaderboard("ARCADE_PARTY_WINS", ARCADE, ar -> ar.get("wins_party").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_PIXEL_PARTY_WINS", ARCADE,
                ar -> ar.getAsJsonObject("pixel_party").get("wins").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_THROW_OUT_WINS", ARCADE, ar -> ar.get("wins_throw_out").getAsInt()));
        new Leaderboard("ARCADE_THROW_OUT_KILLS", ARCADE, ar -> ar.get("kills_throw_out").getAsInt());
        addToLookup(new Leaderboard("ARCADE_ZOMBIES_WINS", ARCADE, ar -> ar.get("wins_zombies").getAsInt()));
        new Leaderboard("ARCADE_ZOMBIES_KILLS", ARCADE, ar -> ar.get("zombie_kills_zombies").getAsInt());
        new Leaderboard("ARCADE_ZOMBIES_WINDOWS_REPAIRED", ARCADE, ar -> ar.get("windows_repaired_zombies").getAsInt());
        new Leaderboard("ARCADE_ZOMBIES_PLAYERS_REVIVED", ARCADE, ar -> ar.get("players_revived_zombies").getAsInt());
        new Leaderboard("ARCADE_ZOMBIES_DOORS_OPENED", ARCADE, ar -> ar.get("doors_opened_zombies").getAsInt());
        addToLookup(new Leaderboard("ARCADE_BLOCKING_DEAD_WINS", ARCADE, ar -> ar.get("wins_dayone").getAsInt()));
        new Leaderboard("ARCADE_BLOCKING_DEAD_KILLS", ARCADE, ar -> ar.get("kills_dayone").getAsInt());
        addToLookup(
                new Leaderboard("ARCADE_BOUNTY_HUNTERS_WINS", ARCADE, ar -> ar.get("wins_oneinthequiver").getAsInt()));
        new Leaderboard("ARCADE_BOUNTY_HUNTERS_KILLS", ARCADE, ar -> ar.get("kills_oneinthequiver").getAsInt());
        new Leaderboard("ARCADE_CREEPER_ATTACK_MAX_WAVE", ARCADE, ar -> ar.get("max_wave").getAsInt());
        addToLookup(new Leaderboard("ARCADE_DRAGON_WARS_WINS", ARCADE, ar -> ar.get("wins_dragonwars2").getAsInt()));
        new Leaderboard("ARCADE_DRAGON_WARS_KILLS", ARCADE, ar -> ar.get("kills_dragonwars2").getAsInt());
        addToLookup(new Leaderboard("ARCADE_ENDER_SPLEEF_WINS", ARCADE, ar -> ar.get("wins_ender").getAsInt()));
        new Leaderboard("ARCADE_ENDER_SPLEEF_BLOCKS_DESTROYED", ARCADE,
                ar -> ar.get("blocks_destroyed_ender").getAsInt());
        addToLookup(new Leaderboard("ARCADE_FARM_HUNT_WINS", ARCADE, ar -> ar.get("wins_farm_hunt").getAsInt()));
        new Leaderboard("ARCADE_FARM_HUNT_HUNTER_WINS", ARCADE, ar -> ar.get("hunter_wins_farm_hunt").getAsInt());
        new Leaderboard("ARCADE_FARM_HUNT_ANIMAL_WINS", ARCADE, ar -> ar.get("animal_wins_farm_hunt").getAsInt());
        new Leaderboard("ARCADE_FARM_HUNT_KILLS", ARCADE, ar -> ar.get("kills_farm_hunt").getAsInt());
        new Leaderboard("ARCADE_FARM_HUNT_TAUNTS_USED", ARCADE, ar -> ar.get("taunts_used_farm_hunt").getAsInt());
        new Leaderboard("ARCADE_FARM_HUNT_POOP_COLLECTED", ARCADE, ar -> ar.get("poop_collected_farm_hunt").getAsInt());
        addToLookup(new Leaderboard("ARCADE_FOOTBALL_WINS", ARCADE, ar -> ar.get("wins_soccer").getAsInt()));
        new Leaderboard("ARCADE_FOOTBALL_GOALS", ARCADE, ar -> ar.get("goals_soccer").getAsInt());
        new Leaderboard("ARCADE_FOOTBALL_KICKS", ARCADE, ar -> ar.get("kicks_soccer").getAsInt());
        new Leaderboard("ARCADE_FOOTBALL_POWER_KICKS", ARCADE, ar -> ar.get("powerkicks_soccer").getAsInt());
        addToLookup(new Leaderboard("ARCADE_GALAXY_WARS_WINS", ARCADE, ar -> ar.get("sw_game_wins").getAsInt()));
        new Leaderboard("ARCADE_GALAXY_WARS_KILLS", ARCADE, ar -> ar.get("sw_kills").getAsInt());
        new Leaderboard("ARCADE_GALAXY_WARS_KDR", ARCADE,
                ar -> JsonUtil.getNullableDouble(ar.get("sw_kills"))
                        / Math.max(JsonUtil.getNullableDouble(ar.get("sw_deaths")), 1));
        addToLookup(new Leaderboard("ARCADE_HIDE_AND_SEEK_PARTY_POOPER_WINS", ARCADE,
                ar -> JsonUtil.getNullableInt(ar.get("party_pooper_hider_wins_hide_and_seek"))
                        + JsonUtil.getNullableInt(ar.get("party_pooper_seeker_wins_hide_and_seek"))));
        addToLookup(new Leaderboard("ARCADE_HIDE_AND_SEEK_PROP_HUNT_WINS", ARCADE,
                ar -> JsonUtil.getNullableInt(ar.get("prop_hunt_hider_wins_hide_and_seek"))
                        + JsonUtil.getNullableInt(ar.get("prop_hunt_seeker_wins_hide_and_seek"))));
        addToLookup(new Leaderboard("ARCADE_HIDE_AND_SEEK_WINS", ARCADE,
                ar -> LEADERBOARDS.get("ARCADE_HIDE_AND_SEEK_PARTY_POOPER_WINS").derive(ar).intValue()
                        + LEADERBOARDS.get("ARCADE_HIDE_AND_SEEK_PROP_HUNT_WINS").derive(ar).intValue()));
        addToLookup(new Leaderboard("ARCADE_HOLE_IN_THE_WALL_WINS", ARCADE,
                ar -> ar.get("wins_hole_in_the_wall").getAsInt()));
        new Leaderboard("ARCADE_HOLE_IN_THE_WALL_QUALIFICATIONS_RECORD", ARCADE,
                ar -> ar.get("hitw_record_q").getAsInt());
        new Leaderboard("ARCADE_HOLE_IN_THE_WALL_FINALS_RECORD", ARCADE, ar -> ar.get("hitw_record_f").getAsInt());
        new Leaderboard("ARCADE_MINI_WALLS_FINAL_KILLS", ARCADE, ar -> ar.get("final_kills_mini_walls").getAsInt());
        new Leaderboard("ARCADE_PIXEL_PARTY_POWERUPS_COLLECTED", ARCADE,
                ar -> ar.getAsJsonObject("pixel_party").get("power_ups_collected").getAsInt());
        new Leaderboard("ARCADE_PIXEL_PARTY_NORMAL_WINS", ARCADE,
                ar -> ar.getAsJsonObject("pixel_party").get("wins_normal").getAsInt());
        new Leaderboard("ARCADE_PIXEL_PARTY_HYPER_WINS", ARCADE,
                ar -> ar.getAsJsonObject("pixel_party").get("wins_hyper").getAsInt());
        addToLookup(new Leaderboard("ARCADE_PIXEL_PAINTERS_WINS", ARCADE,
                ar -> ar.get("wins_draw_their_thing").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_GRINCH_SIMULATOR_WINS", ARCADE,
                ar -> ar.get("wins_grinch_simulator_v2").getAsInt()));
        new Leaderboard("ARCADE_GRINCH_SIMULATOR_GIFTS_STOLEN", ARCADE,
                ar -> ar.get("gifts_grinch_simulator_v2").getAsInt());
        addToLookup(new Leaderboard("ARCADE_SCUBA_SIMULATOR_WINS", ARCADE,
                ar -> ar.get("wins_scuba_simulator").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_SANTA_SIMULATOR_WINS", ARCADE,
                ar -> ar.get("wins_santa_simulator").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_HALLOWEEN_SIMULATOR_WINS", ARCADE,
                ar -> ar.get("wins_halloween_simulator").getAsInt()));
        addToLookup(new Leaderboard("ARCADE_EASTER_SIMULATOR_WINS", ARCADE,
                ar -> ar.get("wins_easter_simulator").getAsInt()));

        new Leaderboard("ARCADE_WINS", ARCADE,
                ar -> LEADERBOARDS.get("ARCADE_BLOCKING_DEAD_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_BOUNTY_HUNTERS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_DRAGON_WARS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_ENDER_SPLEEF_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_FARM_HUNT_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_FOOTBALL_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_GALAXY_WARS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_HIDE_AND_SEEK_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_HOLE_IN_THE_WALL_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_MINI_WALLS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_PARTY_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_HYPIXEL_SAYS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_PIXEL_PAINTERS_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_THROW_OUT_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_ZOMBIES_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_EASTER_SIMULATOR_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_HALLOWEEN_SIMULATOR_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_SANTA_SIMULATOR_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_SCUBA_SIMULATOR_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_GRINCH_SIMULATOR_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_DROPPER_WINS").derive(ar).intValue() +
                        LEADERBOARDS.get("ARCADE_PIXEL_PARTY_WINS").derive(ar).intValue(),
                -1, MASSIVE_CAP);
    }

    private static void registerArenaBrawlLeaderboards() {
        new Leaderboard("ARENA_BRAWL_COINS", ARENA_BRAWL, ab -> ab.get("coins").getAsInt());
        addToLookup(new Leaderboard("ARENA_BRAWL_WINS", ARENA_BRAWL, ab -> ab.get("wins").getAsInt(), -1, MASSIVE_CAP));
        new Leaderboard("ARENA_BRAWL_KILLS", ARENA_BRAWL, ab -> JsonUtil.getNullableInt(ab.get("kills_1v1"))
                + JsonUtil.getNullableInt(ab.get("kills_2v2")) + JsonUtil.getNullableInt(ab.get("kills_4v4")));
        new Leaderboard("ARENA_BRAWL_KDR", ARENA_BRAWL,
                ab -> (JsonUtil.getNullableDouble(ab.get("kills_1v1")) + JsonUtil.getNullableDouble(ab.get("kills_2v2"))
                        + JsonUtil.getNullableDouble(ab.get("kills_4v4")))
                        / Math.max(1,
                                JsonUtil.getNullableDouble(ab.get("deaths_1v1"))
                                        + JsonUtil.getNullableDouble(ab.get("deaths_2v2"))
                                        + JsonUtil.getNullableDouble(ab.get("deaths_4v4"))));
        new Leaderboard("ARENA_BRAWL_WLR", ARENA_BRAWL,
                ab -> LEADERBOARDS.get("ARENA_BRAWL_WINS").derive(ab).doubleValue() / Math.max(1,
                        (JsonUtil.getNullableInt(ab.get("losses_1v1"))
                                + JsonUtil.getNullableInt(ab.get("losses_2v2"))
                                + JsonUtil.getNullableInt(ab.get("losses_4v4")))));
        new Leaderboard("ARENA_BRAWL_MAGICAL_CHESTS", ARENA_BRAWL, ab -> ab.get("magical_chest").getAsInt());
    }

    private static void registerPaintballLeaderboards() {
        new Leaderboard("PAINTBALL_COINS", PAINTBALL, pb -> pb.get("coins").getAsInt());
        new Leaderboard("PAINTBALL_WINS", PAINTBALL, pb -> pb.get("wins").getAsInt());
        new Leaderboard("PAINTBALL_KILLS", PAINTBALL, pb -> pb.get("kills").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("PAINTBALL_KDR", PAINTBALL,
                pb -> JsonUtil.getNullableDouble(pb.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(pb.get("deaths")), 1));
        new Leaderboard("PAINTBALL_KILLSTREAKS", PAINTBALL, pb -> pb.get("killstreaks").getAsInt());
        new Leaderboard("PAINTBALL_SHOTS_FIRED", PAINTBALL, pb -> pb.get("shots_fired").getAsInt());
    }

    private static void registerQuakecraftLeaderboards() {
        new Leaderboard("QUAKECRAFT_COINS", QUAKECRAFT, qc -> qc.get("coins").getAsInt());
        new Leaderboard("QUAKECRAFT_WINS", QUAKECRAFT,
                qc -> JsonUtil.getNullableInt(qc.get("wins")) + JsonUtil.getNullableInt(qc.get("wins_teams")));
        addToLookup(new Leaderboard("QUAKECRAFT_KILLS", QUAKECRAFT,
                qc -> JsonUtil.getNullableInt(qc.get("kills")) + JsonUtil.getNullableInt(qc.get("kills_teams")), -1,
                MASSIVE_CAP));
        new Leaderboard("QUAKECRAFT_KDR", QUAKECRAFT,
                qc -> JsonUtil.getNullableDouble(qc.get("kills"))
                        / Math.max(1, JsonUtil.getNullableDouble(qc.get("deaths"))
                                + JsonUtil.getNullableDouble(qc.get("deaths_teams"))));
        new Leaderboard("QUAKECRAFT_DISTANCE_TRAVELLED", QUAKECRAFT, qc -> qc.get("distance_travelled").getAsInt());
    }

    private static void registerTurboKartRacersLeaderboards() {
        new Leaderboard("TURBO_KART_RACERS_COINS", TURBO_KART_RACERS, tkr -> tkr.get("coins").getAsInt());
        new Leaderboard("TURBO_KART_RACERS_TROPHIES", TURBO_KART_RACERS,
                tkr -> JsonUtil.getNullableInt(tkr.get("gold_trophy"))
                        + JsonUtil.getNullableInt(tkr.get("silver_trophy"))
                        + JsonUtil.getNullableInt(tkr.get("bronze_trophy")));
        new Leaderboard("TURBO_KART_RACERS_GOLD_TROPHIES", TURBO_KART_RACERS, tkr -> tkr.get("gold_trophy").getAsInt(),
                -1, MASSIVE_CAP);
        new Leaderboard("TURBO_KART_RACERS_LAPS", TURBO_KART_RACERS, tkr -> tkr.get("laps_completed").getAsInt());
        new Leaderboard("TURBO_KART_RACERS_ITEM_BOX_PICKUPS", TURBO_KART_RACERS,
                tkr -> tkr.get("box_pickups").getAsInt());
    }

    private static void registerVampireZLeaderboards() {
        new Leaderboard("VAMPIREZ_COINS", VAMPIREZ, vz -> vz.get("coins").getAsInt());
        new Leaderboard("VAMPIREZ_HUMAN_WINS", VAMPIREZ, vz -> vz.get("human_wins").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("VAMPIREZ_HUMAN_KILLS", VAMPIREZ, vz -> vz.get("human_kills").getAsInt(), -1, LARGE_CAP);
        new Leaderboard("VAMPIREZ_VAMPIRE_WINS", VAMPIREZ, vz -> vz.get("vampire_wins").getAsInt());
        new Leaderboard("VAMPIREZ_VAMPIRE_KILLS", VAMPIREZ, vz -> vz.get("vampire_kills").getAsInt());
        new Leaderboard("VAMPIREZ_ZOMBIE_KILLS", VAMPIREZ, vz -> vz.get("zombie_kills").getAsInt());
    }

    private static void registerWallsLeaderboards() {
        new Leaderboard("WALLS_COINS", WALLS, wl -> wl.get("coins").getAsInt());
        new Leaderboard("WALLS_WINS", WALLS, wl -> wl.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("WALLS_KILLS", WALLS, wl -> wl.get("kills").getAsInt());
        new Leaderboard("WALLS_KDR", WALLS,
                wl -> JsonUtil.getNullableDouble(wl.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(wl.get("deaths")), 1));
        new Leaderboard("WALLS_ASSISTS", WALLS, wl -> wl.get("assists").getAsInt());
    }

    private static void registerCopsAndCrimsLeaderboards() {
        new Leaderboard("COPS_AND_CRIMS_SCORE", COPS_AND_CRIMS, cc -> cc.get("score").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("COPS_AND_CRIMS_COINS", COPS_AND_CRIMS, cc -> cc.get("coins").getAsInt());

        addToLookup(
                new Leaderboard("COPS_AND_CRIMS_DEFUSAL_WINS", COPS_AND_CRIMS, cc -> cc.get("game_wins").getAsInt()));
        addToLookup(new Leaderboard("COPS_AND_CRIMS_DEFUSAL_KILLS", COPS_AND_CRIMS, cc -> cc.get("kills").getAsInt()));
        new Leaderboard("COPS_AND_CRIMS_DEFUSAL_BOMBS_PLANTED", COPS_AND_CRIMS,
                cc -> cc.get("bombs_planted").getAsInt());
        new Leaderboard("COPS_AND_CRIMS_DEFUSAL_BOMBS_DEFUSED", COPS_AND_CRIMS,
                cc -> cc.get("bombs_defused").getAsInt());
        new Leaderboard("COPS_AND_CRIMS_DEFUSAL_ROUND_WINS", COPS_AND_CRIMS, cc -> cc.get("round_wins").getAsInt());
        new Leaderboard("COPS_AND_CRIMS_DEFUSAL_KDR", COPS_AND_CRIMS,
                cc -> JsonUtil.getNullableDouble(cc.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(cc.get("deaths")), 1));
        addToLookup(new Leaderboard("COPS_AND_CRIMS_DEFUSAL_ASSISTS", COPS_AND_CRIMS,
                cc -> cc.get("assists").getAsInt()));

        addToLookup(new Leaderboard("COPS_AND_CRIMS_TEAM_DEATHMATCH_WINS", COPS_AND_CRIMS,
                cc -> cc.get("game_wins_deathmatch").getAsInt()));
        addToLookup(new Leaderboard("COPS_AND_CRIMS_TEAM_DEATHMATCH_KILLS", COPS_AND_CRIMS,
                cc -> cc.get("kills_deathmatch").getAsInt()));
        new Leaderboard("COPS_AND_CRIMS_TEAM_DEATHMATCH_KDR", COPS_AND_CRIMS,
                cc -> JsonUtil.getNullableDouble(cc.get("kills_deathmatch"))
                        / Math.max(JsonUtil.getNullableDouble(cc.get("deaths_deathmatch")), 1));
        addToLookup(new Leaderboard("COPS_AND_CRIMS_TEAM_DEATHMATCH_ASSISTS", COPS_AND_CRIMS,
                cc -> cc.get("assists_deathmatch").getAsInt()));

        addToLookup(new Leaderboard("COPS_AND_CRIMS_GUN_GAME_WINS", COPS_AND_CRIMS,
                cc -> cc.get("game_wins_gungame").getAsInt()));
        addToLookup(new Leaderboard("COPS_AND_CRIMS_GUN_GAME_KILLS", COPS_AND_CRIMS,
                cc -> cc.get("kills_gungame").getAsInt()));
        new Leaderboard("COPS_AND_CRIMS_GUN_GAME_KDR", COPS_AND_CRIMS,
                cc -> JsonUtil.getNullableDouble(cc.get("kills_gungame"))
                        / Math.max(JsonUtil.getNullableDouble(cc.get("deaths_gungame")), 1));
        new Leaderboard("COPS_AND_CRIMS_GUN_GAME_FASTEST_WIN", COPS_AND_CRIMS,
                cc -> cc.get("fastest_win_gungame").getAsInt(), 1);
        addToLookup(new Leaderboard("COPS_AND_CRIMS_GUN_GAME_ASSISTS", COPS_AND_CRIMS,
                cc -> cc.get("assists_gungame").getAsInt()));

        new Leaderboard("COPS_AND_CRIMS_WINS", COPS_AND_CRIMS,
                cc -> LEADERBOARDS.get("COPS_AND_CRIMS_DEFUSAL_WINS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_TEAM_DEATHMATCH_WINS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_GUN_GAME_WINS").derive(cc).intValue());
        addToLookup(new Leaderboard("COPS_AND_CRIMS_KILLS", COPS_AND_CRIMS,
                cc -> LEADERBOARDS.get("COPS_AND_CRIMS_DEFUSAL_KILLS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_TEAM_DEATHMATCH_KILLS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_GUN_GAME_KILLS").derive(cc).intValue()));
        new Leaderboard("COPS_AND_CRIMS_GRENADE_KILLS", COPS_AND_CRIMS,
                cc -> cc.get("grenade_kills").getAsInt());
        new Leaderboard("COPS_AND_CRIMS_KDR", COPS_AND_CRIMS,
                cc -> LEADERBOARDS.get("COPS_AND_CRIMS_KILLS").derive(cc).doubleValue() / Math.max(1,
                        JsonUtil.getNullableDouble(cc.get("deaths"))
                                + JsonUtil.getNullableDouble(cc.get("deaths_deathmatch"))
                                + JsonUtil.getNullableDouble(cc.get("deaths_gungame"))));
        new Leaderboard("COPS_AND_CRIMS_ASSISTS", COPS_AND_CRIMS,
                cc -> LEADERBOARDS.get("COPS_AND_CRIMS_DEFUSAL_ASSISTS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_TEAM_DEATHMATCH_ASSISTS").derive(cc).intValue()
                        + LEADERBOARDS.get("COPS_AND_CRIMS_GUN_GAME_ASSISTS").derive(cc).intValue());

        for (String gun : COPS_AND_CRIMS_GUNS) {
            new Leaderboard("COPS_AND_CRIMS_GUN_" + gun.toUpperCase() + "_KILLS", COPS_AND_CRIMS,
                    cc -> cc.get(gun + "Kills").getAsInt(), -1, KIT_CAP);
        }
    }

    private static void registerMegaWallsLeaderboards() {
        new Leaderboard("MEGA_WALLS_COINS", MEGA_WALLS, mw -> mw.get("coins").getAsInt());
        new Leaderboard("MEGA_WALLS_CLASS_POINTS", MEGA_WALLS, mw -> mw.get("class_points").getAsInt());

        new Leaderboard("MEGA_WALLS_WINS", MEGA_WALLS, mw -> mw.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("MEGA_WALLS_WITHER_KILLS", MEGA_WALLS, mw -> mw.get("wither_kills").getAsInt());
        new Leaderboard("MEGA_WALLS_WLR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("wins"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("losses")), 1));
        addToLookup(new Leaderboard("MEGA_WALLS_FINAL_KILLS", MEGA_WALLS,
                mw -> JsonUtil.getNullableInt(mw.get("final_kills")) + JsonUtil.getNullableInt(mw.get("finalKills"))));
        new Leaderboard("MEGA_WALLS_FKDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("final_kills"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("final_deaths"))
                                + JsonUtil.getNullableDouble(mw.get("finalDeaths")), 1.0));
        new Leaderboard("MEGA_WALLS_KILLS", MEGA_WALLS, mw -> mw.get("kills").getAsInt());
        new Leaderboard("MEGA_WALLS_KDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("deaths")), 1));
        new Leaderboard("MEGA_WALLS_MYTHIC_FAVOR", MEGA_WALLS, mw -> mw.get("mythic_favor").getAsInt());

        new Leaderboard("MEGA_WALLS_STANDARD_WINS", MEGA_WALLS, mw -> mw.get("wins_standard").getAsInt());
        new Leaderboard("MEGA_WALLS_STANDARD_WITHER_KILLS", MEGA_WALLS,
                mw -> mw.get("wither_kills_standard").getAsInt());
        new Leaderboard("MEGA_WALLS_STANDARD_WLR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("wins_standard"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("losses_standard")), 1));
        new Leaderboard("MEGA_WALLS_STANDARD_FINAL_KILLS", MEGA_WALLS, mw -> mw.get("final_kills_standard").getAsInt());
        new Leaderboard("MEGA_WALLS_STANDARD_FKDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("final_kills_standard"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("final_deaths_standard")), 1));
        new Leaderboard("MEGA_WALLS_STANDARD_KILLS", MEGA_WALLS, mw -> mw.get("kills_standard").getAsInt());
        new Leaderboard("MEGA_WALLS_STANDARD_KDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("kills_standard"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("deaths_standard")), 1));

        new Leaderboard("MEGA_WALLS_FACEOFF_WINS", MEGA_WALLS, mw -> mw.get("wins_face_off").getAsInt());
        new Leaderboard("MEGA_WALLS_FACEOFF_WITHER_KILLS", MEGA_WALLS,
                mw -> mw.get("wither_kills_face_off").getAsInt());
        new Leaderboard("MEGA_WALLS_FACEOFF_WLR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("wins_face_off"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("losses_face_off")), 1));
        new Leaderboard("MEGA_WALLS_FACEOFF_FINAL_KILLS", MEGA_WALLS, mw -> mw.get("final_kills_face_off").getAsInt());
        new Leaderboard("MEGA_WALLS_FACEOFF_FKDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("final_kills_face_off"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("final_deaths_face_off")), 1));
        new Leaderboard("MEGA_WALLS_FACEOFF_KILLS", MEGA_WALLS, mw -> mw.get("kills_face_off").getAsInt());
        new Leaderboard("MEGA_WALLS_FACEOFF_KDR", MEGA_WALLS,
                mw -> JsonUtil.getNullableDouble(mw.get("kills_face_off"))
                        / Math.max(JsonUtil.getNullableDouble(mw.get("deaths_face_off")), 1));

        new Leaderboard("MEGA_WALLS_PRESTIGE_FOURS", MEGA_WALLS, mw -> {
            int prestigeFours = 0;
            if (mw.has("classes") && !mw.get("classes").isJsonNull()) {
                JsonObject classes = mw.getAsJsonObject("classes");
                for (String cls : MEGA_WALLS_CLASSES) {
                    if (classes.has(cls) && !classes.get(cls).isJsonNull()) {
                        JsonObject classObj = classes.getAsJsonObject(cls);
                        if (classObj.has("prestige") && !classObj.get("prestige").isJsonNull()
                                && classObj.get("prestige").getAsInt() >= 4) {
                            prestigeFours++;
                        }
                    }
                }
            }
            return prestigeFours;
        });

        new Leaderboard("MEGA_WALLS_PRESTIGE_FIVES", MEGA_WALLS, mw -> {
            int prestigeFives = 0;
            if (mw.has("classes") && !mw.get("classes").isJsonNull()) {
                JsonObject classes = mw.getAsJsonObject("classes");
                for (String cls : MEGA_WALLS_CLASSES) {
                    if (classes.has(cls) && !classes.get(cls).isJsonNull()) {
                        JsonObject classObj = classes.getAsJsonObject(cls);
                        if (classObj.has("prestige") && !classObj.get("prestige").isJsonNull()
                                && classObj.get("prestige").getAsInt() >= 5) {
                            prestigeFives++;
                        }
                    }
                }
            }
            return prestigeFives;
        });

        for (String cls : MEGA_WALLS_CLASSES) {
            String clsUpper = cls.toUpperCase();

            new Leaderboard("MEGA_WALLS_" + clsUpper + "_CLASS_POINTS", MEGA_WALLS,
                    mw -> mw.get(cls + "_class_points").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_WINS", MEGA_WALLS, mw -> mw.get(cls + "_wins").getAsInt(), -1,
                    KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_FINAL_KILLS", MEGA_WALLS,
                    mw -> mw.get(cls + "_final_kills").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_FINAL_ASSISTS", MEGA_WALLS,
                    mw -> mw.get(cls + "_final_assists").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_DEFENDER_KILLS", MEGA_WALLS,
                    mw -> mw.get(cls + "_defender_kills").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_PLAYTIME", MEGA_WALLS,
                    mw -> mw.get(cls + "time_played").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_WITHER_DAMAGE", MEGA_WALLS,
                    mw -> mw.get(cls + "_wither_damage").getAsInt(), -1, KIT_CAP);
            new Leaderboard("MEGA_WALLS_" + clsUpper + "_DAMAGE_DEALT", MEGA_WALLS,
                    mw -> mw.get(cls + "_damage_dealt").getAsInt(), -1, KIT_CAP);
        }
    }

    private static void registerSmashHeroesLeaderboards() {
        new Leaderboard("SMASH_HEROES_COINS", SMASH_HEROES, sh -> sh.get("coins").getAsInt());
        new Leaderboard("SMASH_HEROES_WINS", SMASH_HEROES, sh -> sh.get("wins").getAsInt());
        new Leaderboard("SMASH_HEROES_WLR", SMASH_HEROES,
                sh -> JsonUtil.getNullableDouble(sh.get("wins"))
                        / Math.max(JsonUtil.getNullableDouble(sh.get("losses")), 1));
        new Leaderboard("SMASH_HEROES_KILLS", SMASH_HEROES, sh -> sh.get("kills").getAsInt());
        new Leaderboard("SMASH_HEROES_KDR", SMASH_HEROES,
                sh -> JsonUtil.getNullableDouble(sh.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(sh.get("deaths")), 1));
        new Leaderboard("SMASH_HEROES_SMASH_LEVEL", SMASH_HEROES, sh -> sh.get("smashLevel").getAsInt(), -1,
                MASSIVE_CAP);
    }

    private static void registerUHCLeaderboards() {
        new Leaderboard("UHC_COINS", UHC, uhc -> uhc.get("coins").getAsInt());
        new Leaderboard("UHC_SCORE", UHC, uhc -> uhc.get("score").getAsInt(), -1, MASSIVE_CAP);
        // * wins + wins_solo + wins_no_diamonds + wins_brawl + wins_solo_brawl +
        // wins_duo_brawl + wins_vanilla_doubles
        new Leaderboard("UHC_WINS", UHC, uhc -> JsonUtil.getNullableInt(uhc.get("wins"))
                + JsonUtil.getNullableInt(uhc.get("wins_solo")) + JsonUtil.getNullableInt(uhc.get("wins_no_diamonds"))
                + JsonUtil.getNullableInt(uhc.get("wins_brawl")) + JsonUtil.getNullableInt(uhc.get("wins_solo_brawl"))
                + JsonUtil.getNullableInt(uhc.get("wins_duo_brawl"))
                + JsonUtil.getNullableInt(uhc.get("wins_vanilla_doubles")));
        addToLookup(new Leaderboard("UHC_KILLS", UHC, uhc -> JsonUtil.getNullableInt(uhc.get("kills"))
                + JsonUtil.getNullableInt(uhc.get("kills_solo")) + JsonUtil.getNullableInt(uhc.get("kills_no_diamonds"))
                + JsonUtil.getNullableInt(uhc.get("kills_brawl")) + JsonUtil.getNullableInt(uhc.get("kills_solo_brawl"))
                + JsonUtil.getNullableInt(uhc.get("kills_duo_brawl"))
                + JsonUtil.getNullableInt(uhc.get("kills_vanilla_doubles"))));
        new Leaderboard("UHC_KDR", UHC,
                uhc -> LEADERBOARDS.get("UHC_KILLS").derive(uhc).doubleValue() / Math.max(1,
                        JsonUtil.getNullableDouble(uhc.get("deaths"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_solo"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_no_diamonds"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_brawl"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_solo_brawl"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_duo_brawl"))
                                + JsonUtil.getNullableDouble(uhc.get("deaths_vanilla_doubles"))));
        new Leaderboard("UHC_TEAMS_WINS", UHC, uhc -> uhc.get("wins").getAsInt());
        new Leaderboard("UHC_TEAMS_KILLS", UHC, uhc -> uhc.get("kills").getAsInt());
        new Leaderboard("UHC_TEAMS_KDR", UHC,
                uhc -> JsonUtil.getNullableDouble(uhc.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(uhc.get("deaths")), 1));
        new Leaderboard("UHC_SOLO_WINS", UHC, uhc -> uhc.get("wins_solo").getAsInt());
        new Leaderboard("UHC_SOLO_KILLS", UHC, uhc -> uhc.get("kills_solo").getAsInt());
        new Leaderboard("UHC_SOLO_KDR", UHC,
                uhc -> JsonUtil.getNullableDouble(uhc.get("kills_solo"))
                        / Math.max(JsonUtil.getNullableDouble(uhc.get("deaths_solo")), 1));

    }

    private static void registerSpeedUHCLeaderboards() {
        new Leaderboard("SPEED_UHC_SCORE", SPEED_UHC, suhc -> suhc.get("score").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("SPEED_UHC_KILLS", SPEED_UHC, suhc -> suhc.get("kills").getAsInt());
        new Leaderboard("SPEED_UHC_WINS", SPEED_UHC, suhc -> suhc.get("wins").getAsInt());
        new Leaderboard("SPEED_UHC_WLR", SPEED_UHC,
                suhc -> JsonUtil.getNullableDouble(suhc.get("wins"))
                        / Math.max(JsonUtil.getNullableDouble(suhc.get("losses")), 1));
        new Leaderboard("SPEED_UHC_KDR", SPEED_UHC,
                suhc -> JsonUtil.getNullableDouble(suhc.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(suhc.get("deaths")), 1));

    }

    private static void registerWarlordsLeaderboards() {
        new Leaderboard("WARLORDS_COINS", WARLORDS, wl -> wl.get("coins").getAsInt());
        new Leaderboard("WARLORDS_KILLS", WARLORDS, wl -> wl.get("kills").getAsInt());
        new Leaderboard("WARLORDS_WINS", WARLORDS, wl -> wl.get("wins").getAsInt(), -1, MASSIVE_CAP);
        new Leaderboard("WARLORDS_WLR", WARLORDS,
                wl -> JsonUtil.getNullableDouble(wl.get("wins")) / Math.max(1,
                        JsonUtil.getNullableDouble(wl.get("mage_plays"))
                                + JsonUtil.getNullableDouble(wl.get("warrior_plays"))
                                + JsonUtil.getNullableDouble(wl.get("paladin_plays"))
                                + JsonUtil.getNullableDouble(wl.get("shaman_plays"))
                                - JsonUtil.getNullableDouble(wl.get("losses"))));
        new Leaderboard("WARLORDS_KDR", WARLORDS,
                wl -> JsonUtil.getNullableDouble(wl.get("kills"))
                        / Math.max(JsonUtil.getNullableDouble(wl.get("deaths")), 1));
        new Leaderboard("WARLORDS_CAPTURE_THE_FLAG_WINS", WARLORDS, wl -> wl.get("wins_capturetheflag").getAsInt());
        new Leaderboard("WARLORDS_CAPTURE_THE_FLAG_KILLS", WARLORDS, wl -> wl.get("kills_capturetheflag").getAsInt());
        new Leaderboard("WARLORDS_CAPTURE_THE_FLAG_CAPTURES", WARLORDS, wl -> wl.get("flag_conquer_self").getAsInt());
        new Leaderboard("WARLORDS_CAPTURE_THE_FLAG_RETURNS", WARLORDS, wl -> wl.get("flag_returns").getAsInt());
        new Leaderboard("WARLORDS_DOMINATION_WINS", WARLORDS, wl -> wl.get("wins_domination").getAsInt());
        new Leaderboard("WARLORDS_DOMINATION_KILLS", WARLORDS, wl -> wl.get("kills_domination").getAsInt());
        new Leaderboard("WARLORDS_DOMINATION_CAPTURES", WARLORDS, wl -> wl.get("dom_point_captures").getAsInt());
        new Leaderboard("WARLORDS_TEAM_DEATHMATCH_WINS", WARLORDS, wl -> wl.get("wins_teamdeathmatch").getAsInt());
        new Leaderboard("WARLORDS_TEAM_DEATHMATCH_KILLS", WARLORDS, wl -> wl.get("kills_teamdeathmatch").getAsInt());
    }

    private static void registerWoolGamesLeaderboards() {
        new Leaderboard("WOOL_GAMES_WOOL", WOOL_GAMES, wg -> wg.get("coins").getAsInt());
        new Leaderboard("WOOL_GAMES_LEVEL", WOOL_GAMES,
                wg -> wg.getAsJsonObject("progression").get("experience").getAsInt(), -1, MASSIVE_CAP);

        addToLookup(new Leaderboard("WOOL_GAMES_SHEEP_WARS_WINS", WOOL_GAMES,
                wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("wins").getAsInt()));
        new Leaderboard("WOOL_GAMES_SHEEP_WARS_KDR", WOOL_GAMES,
                wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("kills").getAsDouble() / Math
                        .max(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("deaths").getAsDouble(), 1));
        addToLookup(new Leaderboard("WOOL_GAMES_SHEEP_WARS_KILLS", WOOL_GAMES,
                wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("kills").getAsInt()));
        new Leaderboard("WOOL_GAMES_SHEEP_WARS_WLR", WOOL_GAMES,
                wg -> wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("wins").getAsDouble() / Math
                        .max(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("losses").getAsDouble(), 1));

        addToLookup(new Leaderboard("WOOL_GAMES_WOOL_WARS_WINS", WOOL_GAMES,
                wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins").getAsInt()));
        new Leaderboard("WOOL_GAMES_WOOL_WARS_KDR", WOOL_GAMES,
                wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("kills").getAsDouble() / Math
                        .max(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("deaths").getAsDouble(), 1));
        addToLookup(new Leaderboard("WOOL_GAMES_WOOL_WARS_KILLS", WOOL_GAMES,
                wg -> wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("kills").getAsInt()));
        new Leaderboard("WOOL_GAMES_WOOL_WARS_WLR", WOOL_GAMES, wg -> wg.getAsJsonObject("wool_wars")
                .getAsJsonObject("stats").get("wins").getAsDouble()
                / Math.max(JsonUtil
                        .getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("games_played"))
                        - JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins")),
                        1));

        addToLookup(new Leaderboard("WOOL_GAMES_CAPTURE_THE_WOOL_WINS", WOOL_GAMES, wg -> wg
                .getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_wins").getAsInt()));
        new Leaderboard("WOOL_GAMES_CAPTURE_THE_WOOL_KDR", WOOL_GAMES, wg -> wg.getAsJsonObject("capture_the_wool")
                .getAsJsonObject("stats").get("kills").getAsDouble()
                / Math.max(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("deaths").getAsDouble(),
                        1));
        addToLookup(new Leaderboard("WOOL_GAMES_CAPTURE_THE_WOOL_KILLS", WOOL_GAMES,
                wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("kills").getAsInt()));
        new Leaderboard("WOOL_GAMES_CAPTURE_THE_WOOL_WLR", WOOL_GAMES,
                wg -> wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("participated_wins")
                        .getAsDouble()
                        / Math.max(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats")
                                .get("participated_losses").getAsDouble(), 1));

        addToLookup(new Leaderboard("WOOL_GAMES_WINS", WOOL_GAMES,
                wg -> LEADERBOARDS.get("WOOL_GAMES_SHEEP_WARS_WINS").derive(wg).intValue()
                        + LEADERBOARDS.get("WOOL_GAMES_WOOL_WARS_WINS").derive(wg).intValue()
                        + LEADERBOARDS.get("WOOL_GAMES_CAPTURE_THE_WOOL_WINS").derive(wg).intValue()));
        addToLookup(new Leaderboard("WOOL_GAMES_KILLS", WOOL_GAMES,
                wg -> LEADERBOARDS.get("WOOL_GAMES_SHEEP_WARS_KILLS").derive(wg).intValue()
                        + LEADERBOARDS.get("WOOL_GAMES_WOOL_WARS_KILLS").derive(wg).intValue()
                        + LEADERBOARDS.get("WOOL_GAMES_CAPTURE_THE_WOOL_KILLS").derive(wg).intValue()));

        new Leaderboard("WOOL_GAMES_WLR", WOOL_GAMES,
                wg -> LEADERBOARDS.get("WOOL_GAMES_WINS").derive(wg).doubleValue() / Math.max(1, JsonUtil
                        .getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("games_played"))
                        - JsonUtil.getNullableInt(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("wins"))
                        + JsonUtil
                                .getNullableInt(wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("losses"))
                        + JsonUtil.getNullableInt(wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats")
                                .get("participated_losses"))));

        new Leaderboard("WOOL_GAMES_KDR", WOOL_GAMES,
                wg -> LEADERBOARDS.get("WOOL_GAMES_KILLS").derive(wg).doubleValue() / Math.max(1, JsonUtil
                        .getNullableDouble(wg.getAsJsonObject("wool_wars").getAsJsonObject("stats").get("deaths"))
                        + JsonUtil.getNullableDouble(
                                wg.getAsJsonObject("sheep_wars").getAsJsonObject("stats").get("deaths"))
                        + JsonUtil.getNullableDouble(
                                wg.getAsJsonObject("capture_the_wool").getAsJsonObject("stats").get("deaths"))));
    }

    private static void registerFishingLeaderboards() {
        addToLookup(new Leaderboard("FISHING_WATER_FISH_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("fish"))));
        addToLookup(new Leaderboard("FISHING_WATER_TREASURE_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("treasure"))));
        addToLookup(new Leaderboard("FISHING_WATER_JUNK_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("water").get("junk"))));
        new Leaderboard("FISHING_WATER_TOTAL_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_WATER_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_WATER_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_WATER_JUNK_CAUGHT").derive(fish).intValue());

        addToLookup(new Leaderboard("FISHING_LAVA_FISH_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("fish"))));
        addToLookup(new Leaderboard("FISHING_LAVA_TREASURE_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("treasure"))));
        addToLookup(new Leaderboard("FISHING_LAVA_JUNK_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("lava").get("junk"))));
        new Leaderboard("FISHING_LAVA_TOTAL_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_LAVA_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_LAVA_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_LAVA_JUNK_CAUGHT").derive(fish).intValue());

        addToLookup(new Leaderboard("FISHING_ICE_FISH_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("fish"))));
        addToLookup(new Leaderboard("FISHING_ICE_TREASURE_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("treasure"))));
        addToLookup(new Leaderboard("FISHING_ICE_JUNK_CAUGHT", FISHING, fish -> JsonUtil.getNullableInt(
                fish.getAsJsonObject("stats").getAsJsonObject("permanent").getAsJsonObject("ice").get("junk"))));
        new Leaderboard("FISHING_ICE_TOTAL_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_ICE_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_ICE_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_ICE_JUNK_CAUGHT").derive(fish).intValue());

        addToLookup(new Leaderboard("FISHING_FISH_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_WATER_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_LAVA_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_ICE_FISH_CAUGHT").derive(fish).intValue()));
        addToLookup(new Leaderboard("FISHING_TREASURE_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_WATER_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_LAVA_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_ICE_TREASURE_CAUGHT").derive(fish).intValue()));
        addToLookup(new Leaderboard("FISHING_JUNK_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_WATER_JUNK_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_LAVA_JUNK_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_ICE_JUNK_CAUGHT").derive(fish).intValue()));
        new Leaderboard("FISHING_MYTHICAL_FISH_CAUGHT", FISHING,
                fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("selene"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("helios"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("nyx"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("zeus"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("aphrodite"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("archimedes"))
                        + JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").get("hades")));

        addToLookup(new Leaderboard("FISHING_TOTAL_CAUGHT", FISHING,
                fish -> LEADERBOARDS.get("FISHING_FISH_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_TREASURE_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_JUNK_CAUGHT").derive(fish).intValue()
                        + LEADERBOARDS.get("FISHING_MYTHICAL_FISH_CAUGHT").derive(fish).intValue(), -1, MASSIVE_CAP));

        new Leaderboard("FISHING_BIGGEST_DAEDALUS", FISHING, fish -> JsonUtil
                .getNullableInt(fish.getAsJsonObject("orbs").getAsJsonObject("weight").get("archimedes")));
        new Leaderboard("FISHING_BIGGEST_HADES", FISHING,
                fish -> JsonUtil.getNullableInt(fish.getAsJsonObject("orbs").getAsJsonObject("weight").get("hades")));
    }

    private static void registerGuildLeaderboards() {
        // Guild Level
        addToLookup(new Leaderboard("GUILD_LEVEL", GUILDS, guild -> guild.get("level").getAsDouble(), -1, MASSIVE_CAP));
    }
}
