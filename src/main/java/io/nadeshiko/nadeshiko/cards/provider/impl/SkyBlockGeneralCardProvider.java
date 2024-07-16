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
import io.nadeshiko.nadeshiko.util.MinecraftRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Function;

public class SkyBlockGeneralCardProvider extends CardProvider {

	private final Color maxColor = new Color(206, 143, 18);

	public SkyBlockGeneralCardProvider() {
		super(CardGame.SKYBLOCK_GENERAL);
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

		// Draw the SkyBlock level
		int level = profileData.getAsJsonObject("skyblock_level").get("level").getAsInt();
		int maxLevel = profileData.getAsJsonObject("skyblock_level").get("maxLevel").getAsInt();
		String levelText = "Level [" + level + "]";
		g.setColor(level == maxLevel ? maxColor : Color.WHITE);
		g.setFont(mediumBold);
		g.drawString(levelText, 755 - g.getFontMetrics().stringWidth(levelText), 80);

		// Draw the SkyBlock level bar
		float progress = profileData.getAsJsonObject("skyblock_level").get("progress").getAsFloat();
		this.drawProgress(g, 772, 71, 654, 8, progress, level == maxLevel ? maxColor : this.getColor());

		// Draw the skills
		JsonObject skillsObject = profileData.getAsJsonObject("skills").getAsJsonObject("skills");
		this.drawSkill(g, 772, 108, "Taming", skillsObject.getAsJsonObject("taming"));
		this.drawSkill(g, 772, 133, "Mining", skillsObject.getAsJsonObject("mining"));
		this.drawSkill(g, 772, 158, "Foraging", skillsObject.getAsJsonObject("foraging"));
		this.drawSkill(g, 772, 183, "Enchanting", skillsObject.getAsJsonObject("enchanting"));
		this.drawSkill(g, 772, 208, "Carpentry", skillsObject.getAsJsonObject("carpentry"));
		this.drawSkill(g, 1192, 108, "Farming", skillsObject.getAsJsonObject("farming"));
		this.drawSkill(g, 1192, 133, "Combat", skillsObject.getAsJsonObject("combat"));
		this.drawSkill(g, 1192, 158, "Fishing", skillsObject.getAsJsonObject("fishing"));
		this.drawSkill(g, 1192, 183, "Alchemy", skillsObject.getAsJsonObject("alchemy"));
		this.drawSkill(g, 1192, 208, "Social", skillsObject.getAsJsonObject("social"));
	}

	private void drawSkill(Graphics2D g, int x, int y, String name, JsonObject data) {

		// Draw the skill level
		int level = data.get("level").getAsInt();
		int maxLevel = data.get("maxLevel").getAsInt();
		String levelText = " " + level;
		g.setColor(level == maxLevel ? maxColor : Color.WHITE);
		g.setFont(smallBold);
		g.drawString(levelText, x - 15 - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill name
		g.setColor(level == maxLevel ? maxColor : new Color(181, 181, 181));
		g.setFont(smallLight);
		g.drawString(name, x - 20 - g.getFontMetrics().stringWidth(name) - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill progress
		float progress = level == maxLevel ? 1 : data.get("progress").getAsFloat();
		this.drawProgress(g, x, y, 236, 8, progress, level == maxLevel ? maxColor : this.getColor());
	}
}
