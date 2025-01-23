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

package io.nadeshiko.nadeshiko.util.hypixel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Utility class for SkyBlock-related calculations. The resource file on which this class depends is sourced from the
 * NotEnoughUpdates mod.
 * @author chloe, Moulberry
 */
@UtilityClass
public class SkyBlockUtil {

    /**
     * List of the EXP required per level of <i>most</i> skills
     */
    private static final List<Long> SKILL_EXP = new ArrayList<>();

    /**
     * List of the EXP required per level of the Runecrafting skill
     */
    private static final List<Long> RUNECRAFTING_EXP = new ArrayList<>();

    /**
     * List of the EXP required per level of the Social skill
     */
    private static final List<Long> SOCIAL_EXP = new ArrayList<>();

    /**
     * List of the EXP required per Catacombs and Dungeon Class level
     */
    private static final List<Long> CATACOMBS_EXP = new ArrayList<>();

    /**
     * List of the EXP required per level of <i>most</i> slayers
     */
    private static final List<Long> SLAYER_EXP = new ArrayList<>();
    private static final int MAX_SLAYER = 9;

    /**
     * List of the EXP required per level of the Vampire slayer
     */
    private static final List<Long> VAMPIRE_EXP = new ArrayList<>();
    private static final int MAX_VAMPIRE = 5;

    /**
     * Map of skills names to their maximum level
     */
    private static final Map<String, Integer> MAX_LEVELS = new HashMap<>();

    /**
     * Map of rarity names -> MP provided
     */
    private static final Map<String, Integer> RARITY_MP_WORTH = new HashMap<>() {{
        put("MYTHIC", 22);
        put("LEGENDARY", 16);
        put("EPIC", 12);
        put("RARE", 8);
        put("UNCOMMON", 5);
        put("COMMON", 3);
        put("SPECIAL", 3);
        put("VERY_SPECIAL", 5);
    }};

    // Load the leveling.json file
    static {
        readLevelingFile();
    }

    public static int calculateAccessoryMp(@NonNull JsonObject accessory) {

        // In case we get passed an empty item
        if (accessory.isEmpty()) {
            return 0;
        }

        // Base worth based on rarity
        int worth = RARITY_MP_WORTH.get(accessory.get("rarity").getAsString());

        // Special case
        String id = accessory.getAsJsonObject("attributes").getAsJsonObject("id").get("value").getAsString();
        if (id.equals("HEGEMONY_ARTIFACT")) {
            worth *= 2;
        }

        return worth;
    }

    public static double calculateCatacombs(double exp) {
        return calculateLevel(CATACOMBS_EXP, exp, 1000); // I highly doubt anyone will be Cata 1000 ever
    }

    public static JsonObject expandSlayer(@NonNull String name, double exp) {
        JsonObject slayerData = new JsonObject();
        slayerData.addProperty("experience", exp);

        boolean vampire = name.equalsIgnoreCase("VAMPIRE");

        double level = calculateSlayer(vampire ? VAMPIRE_EXP : SLAYER_EXP, exp, vampire ? MAX_VAMPIRE : MAX_SLAYER);

        slayerData.addProperty("max_level", vampire ? MAX_VAMPIRE : MAX_SLAYER);
        slayerData.addProperty("exact_level", level);
        slayerData.addProperty("level", (int) level);
        slayerData.addProperty("progress", level % 1);
        return slayerData;
    }

    /**
     * Expands a skill name and EXP into a JsonObject containing more in-depth information
     * @param key The key of the skill in the Hypixel API, i.e. SKILL_MINING
     * @param exp The EXP of the skill
     * @param profile The player's profile
     * @param hasRank Whether the player has a Hypixel rank
     * @return A JsonObject containing the calculated skill level, the skill max level, and progress.
     */
    public static JsonObject expandSkill(@NonNull String key, double exp, @NonNull JsonObject profile, boolean hasRank) {
        JsonObject skillData = new JsonObject();
        skillData.addProperty("experience", exp);

        // Remove the SKILL_ prefix that Hypixel adds
        key = key.substring("SKILL_".length());

        if (!MAX_LEVELS.containsKey(key)) {
            Nadeshiko.logger.error("Encountered unknown skill key \"{}\"!", key);
        }

        // Get the max level of the skill, falling back to 0.
        int maxLevel = Objects.requireNonNullElse(MAX_LEVELS.get(key), 0);

        // Some skills are special. Because of course.
        try {

            // Taming cap by giving pets to George
            if (key.equals("TAMING")) {
                maxLevel += profile.getAsJsonObject("pets_data").getAsJsonObject("pet_care")
                    .getAsJsonArray("pet_types_sacrificed").size();
            }

            // Farming cap by buying from Anita
            else if (key.equals("FARMING")) {
                maxLevel += profile.getAsJsonObject("jacobs_contest").getAsJsonObject("perks").get("farming_level_cap").getAsInt();
            }

            // Runecrafting cap without rank
            else if (key.equals("RUNECRAFTING") && !hasRank) {
                maxLevel = 3;
            }
        } catch (Exception ignored) {} // I have a bad feeling about this code, so I'm doing this to be safe

        // Add the max level
        skillData.addProperty("max_level", maxLevel);

        // Determine which EXP curve to use
        List<Long> expCurve;
        switch (key) {
            case "RUNECRAFTING" -> expCurve = RUNECRAFTING_EXP;
            case "SOCIAL" -> expCurve = SOCIAL_EXP;
            default -> expCurve = SKILL_EXP;
        }

        double level = calculateLevel(expCurve, exp, maxLevel);

        skillData.addProperty("exact_level", level);
        skillData.addProperty("level", (int) level);
        skillData.addProperty("progress", level % 1);
        return skillData;
    }

    private static double calculateSlayer(@NonNull List<Long> curve, double exp, int maxLevel) {
        int level = 1;
        long totalExp = 0;

        for (int i = 0; i < maxLevel; i++) {
            int lastLevelExp = (int) (i > 0 ? curve.get(i - 1) : 0);
            int levelExp = (int) (curve.get(i) - lastLevelExp);
            totalExp += levelExp;

            if (totalExp > exp) {
                totalExp -= levelExp;
                break;
            }

            level = (i + 1);
        }

        long remainingExp = (long) Math.floor(exp - totalExp);
        long expForNext = 0;

        if (level < maxLevel) {
            expForNext = (long) Math.ceil(curve.get(level) - curve.get(level - 1));
        }

        double progress = expForNext > 0 ? Math.max(0, Math.min(((double) remainingExp) / expForNext, 1)) : 0;
        return level + progress;
    }

    /**
     * Calculate the exact skill level provided an EXP curve and an EXP number
     * @param curve The EXP curve to use
     * @param exp The EXP to calculate with
     * @param maxLevel The maximum level to go up to
     * @return The exact level corresponding to the given EXP number
     * @author CrypticPlasma on the Hypixel Forums
     */
    private static double calculateLevel(@NonNull List<Long> curve, double exp, int maxLevel) {
        int level = 1;
        long totalExp = 0;

        for (int i = 0; i < maxLevel; i++) {
            totalExp += curve.get(i);

            if (totalExp > exp) {
                totalExp -= curve.get(i);
                break;
            }

            level = (i + 1);
        }

        long remainingExp = (long) Math.floor(exp - totalExp);
        long expForNext = 0;

        if (level < maxLevel) {
            expForNext = (long) Math.ceil(curve.get(level));
        }

        double progress = expForNext > 0 ? Math.max(0, Math.min(((double) remainingExp) / expForNext, 1)) : 0;
        return level + progress;
    }

    /**
     * Reads the {@code leveling.json} resource file, getting the data used by this class
     */
    private static void readLevelingFile() {
        try (InputStream stream = SkyBlockUtil.class.getResourceAsStream("/skyblock/leveling.json")) {

            // Ensure the resource exists
            if (stream == null) {
                throw new Exception("Stream is null!");
            }

            // Parse the resource
            JsonObject leveling = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            // Populate lists
            leveling.getAsJsonArray("leveling_xp").forEach(e -> SKILL_EXP.add(e.getAsLong()));
            leveling.getAsJsonArray("runecrafting_xp").forEach(e -> RUNECRAFTING_EXP.add(e.getAsLong()));
            leveling.getAsJsonArray("social").forEach(e -> SOCIAL_EXP.add(e.getAsLong()));
            leveling.getAsJsonArray("catacombs").forEach(e -> CATACOMBS_EXP.add(e.getAsLong()));

            // Populate slayer stuff
            leveling.getAsJsonObject("slayer_xp").getAsJsonArray("zombie").forEach(e -> SLAYER_EXP.add(e.getAsLong()));
            leveling.getAsJsonObject("slayer_xp").getAsJsonArray("vampire").forEach(e -> VAMPIRE_EXP.add(e.getAsLong()));

            // Populate max level map
            leveling.getAsJsonObject("leveling_caps").entrySet().forEach(e ->
                MAX_LEVELS.put(e.getKey().toUpperCase(), e.getValue().getAsInt()));
        } catch (Exception e) {
            Nadeshiko.logger.error("Failed to read leveling.json resource! This shouldn't happen! Halting.", e);
            Runtime.getRuntime().exit(0);
        }
    }
}
