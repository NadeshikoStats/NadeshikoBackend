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

package io.nadeshiko.nadeshiko.cards.provider.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.cards.CardGame;
import io.nadeshiko.nadeshiko.cards.provider.CardProvider;
import io.nadeshiko.nadeshiko.util.HTTPUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class SkyblockDungeonsCardProvider extends CardProvider {

    private final Color maxColor = new Color(206, 143, 18);

    public SkyblockDungeonsCardProvider() {
        super(CardGame.SKYBLOCK_DUNGEONS);
    }

    @Override
    public void generate(BufferedImage image, JsonObject stats) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        JsonObject skyblockProfiles, profileData = null;

        // Fetch the player's SkyBlock stats
        try {
            skyblockProfiles = JsonParser.parseString(HTTPUtil.get("https://sky.shiiyu.moe/api/v2/profile/" +
                stats.get("name").getAsString()).response()).getAsJsonObject().getAsJsonObject("profiles");

            // Iterate over profiles to find the active one
            for (Map.Entry<String, JsonElement> entry : skyblockProfiles.entrySet()) {
                JsonObject entryObject = entry.getValue().getAsJsonObject();

                if (entryObject.has("current") && entryObject.get("current").getAsBoolean()) {
                    profileData = entryObject.getAsJsonObject("data");
                    break;
                }
            }

            // Ensure that the active profile was found
            if (profileData == null) {
                Nadeshiko.logger.error("Somehow {} has no active SkyBlock profile?", stats);
                return;
            }
        } catch (Exception e) {
            Nadeshiko.logger.error("Encountered error when fetching SkyBlock stats for {}", stats, e);
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }
}
