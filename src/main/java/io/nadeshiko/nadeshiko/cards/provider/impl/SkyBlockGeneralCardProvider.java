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
import io.nadeshiko.nadeshiko.util.NumberUtil;
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
		String prefixColor = this.getPrefixColor(level);
		String levelText = String.format("Level §8[%s" + level + "§8]", prefixColor);
		g.setColor(level == maxLevel ? maxColor : Color.WHITE);
		g.setFont(mediumBold);
		MinecraftRenderer.drawCustomString(g,levelText, 755 - MinecraftRenderer.customWidth(g, levelText), 80);

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

		// Draw dungeons
		this.drawDungeons(g, profileData.getAsJsonObject("dungeons"));

		// Draw slayers
		JsonObject slayersObject = profileData.getAsJsonObject("slayer").getAsJsonObject("slayers");
		this.drawSlayer(g, 1131, 357, "Rev", slayersObject.getAsJsonObject("zombie"));
		this.drawSlayer(g, 1131, 389, "Sven", slayersObject.getAsJsonObject("wolf"));
		this.drawSlayer(g, 1131, 421, "Blaze", slayersObject.getAsJsonObject("blaze"));
		this.drawSlayer(g, 1332, 357, "Tara", slayersObject.getAsJsonObject("spider"));
		this.drawSlayer(g, 1332, 389, "Eman", slayersObject.getAsJsonObject("enderman"));
		this.drawSlayer(g, 1332, 421, "Vamp", slayersObject.getAsJsonObject("vampire"));

		// Draw bottom stuff
		int mp = profileData.getAsJsonObject("accessories").getAsJsonObject("magical_power").get("total").getAsInt();
		double networth = profileData.getAsJsonObject("networth").get("networth").getAsDouble();
		double purse = profileData.getAsJsonObject("networth").get("purse").getAsDouble();
		double bank = profileData.getAsJsonObject("networth").get("bank").getAsDouble();
		String text = "MP  " + String.format("%,d", mp) + "           Networth " + NumberUtil.formatNumber(networth) +
			"           Purse  " + NumberUtil.formatNumber(purse) + "           Bank  " + NumberUtil.formatNumber(bank);
		g.setColor(new Color(181, 181, 181));
		g.setFont(tinyLight);
		g.drawString(text, (int) (1000 - g.getFontMetrics().stringWidth(text) / 2d), 255);
	}

	private void drawSkill(Graphics2D g, int x, int y, String name, JsonObject data) {

		// Draw the skill level
		int level = data.get("level").getAsInt();
		int maxLevel = data.get("maxLevel").getAsInt();
		String levelText = " " + level;
		g.setColor(level >= maxLevel ? maxColor : Color.WHITE);
		g.setFont(smallBold);
		g.drawString(levelText, x - 15 - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill name
		g.setColor(level >= maxLevel ? maxColor : new Color(181, 181, 181));
		g.setFont(smallLight);
		g.drawString(name, x - 20 - g.getFontMetrics().stringWidth(name) - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill progress
		float progress = level == maxLevel ? 1 : data.get("progress").getAsFloat();
		this.drawProgress(g, x, y, 236, 8, progress, level == maxLevel ? maxColor : this.getColor());
	}

	private void drawDungeons(Graphics2D g, JsonObject data) {

		// Draw Catacombs level
		int cataLevel = data.getAsJsonObject("catacombs").getAsJsonObject("level").get("level").getAsInt();
		int cataLevelMax = data.getAsJsonObject("catacombs").getAsJsonObject("level").get("maxLevel").getAsInt();
		String cataLevelText = " " + cataLevel;
		g.setColor(cataLevel >= cataLevelMax ? maxColor : Color.WHITE);
		g.setFont(smallBold);
		g.drawString(cataLevelText, 730, 367);

		// Draw "Catacombs"
		g.setColor(cataLevel >= cataLevelMax ? maxColor : new Color(181, 181, 181));
		g.setFont(smallLight);
		g.drawString("Catacombs", 627, 367);

		// Draw Catacombs level progress
		float cataProgress = data.getAsJsonObject("catacombs").getAsJsonObject("level").get("progress").getAsFloat();
		this.drawProgress(g, 773, 357, 217, 8, cataProgress, cataLevel == cataLevelMax ? maxColor : this.getColor());

		// Draw classes
		JsonObject classesObject = data.getAsJsonObject("classes").getAsJsonObject("classes");
		this.drawClass(g, 655, 397, "Archer", classesObject.getAsJsonObject("archer"));
		this.drawClass(g, 655, 420, "Healer", classesObject.getAsJsonObject("healer"));
		this.drawClass(g, 655, 444, "Tank", classesObject.getAsJsonObject("tank"));
		this.drawClass(g, 830, 397, "Berserk", classesObject.getAsJsonObject("berserk"));
		this.drawClass(g, 830, 420, "Mage", classesObject.getAsJsonObject("mage"));

		// Draw class average
		float classAvg = data.getAsJsonObject("classes").get("average_level").getAsFloat();
		boolean max = data.getAsJsonObject("classes").get("maxed").getAsBoolean();
		g.setColor(max ? maxColor : new Color(181, 181, 181));
		g.setFont(tinyLight);
		g.drawString("Class Average", 802, 444);
		g.setColor(max ? maxColor : Color.WHITE);
		g.setFont(tinyBold);
		g.drawString(String.valueOf(classAvg), 802 + g.getFontMetrics().stringWidth("Class Average "), 444);
	}

	private void drawClass(Graphics2D g, int x, int y, String name, JsonObject data) {

		int level = data.getAsJsonObject("level").get("level").getAsInt();
		int levelMax = data.getAsJsonObject("level").get("maxLevel").getAsInt();

		// Draw name
		g.setColor(level >= levelMax ? maxColor : new Color(181, 181, 181));
		g.setFont(tinyLight);
		g.drawString(name, x, y);

		// Draw level
		g.setColor(level >= levelMax ? maxColor : Color.WHITE);
		g.setFont(tinyBold);
		g.drawString(String.valueOf(level), x + g.getFontMetrics().stringWidth(name + " "), y);
	}

	private void drawSlayer(Graphics2D g, int x, int y, String name, JsonObject data) {

		// Draw the slayer level
		int level = data.getAsJsonObject("level").get("currentLevel").getAsInt();
		int maxLevel = data.getAsJsonObject("level").get("maxLevel").getAsInt();
		String levelText = " " + level;
		g.setColor(level >= maxLevel ? maxColor : Color.WHITE);
		g.setFont(tinyBold);
		g.drawString(levelText, x - 12 - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the slayer name
		g.setColor(level >= maxLevel ? maxColor : new Color(181, 181, 181));
		g.setFont(tinyLight);
		g.drawString(name, x - 15 - g.getFontMetrics().stringWidth(name) - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the slayer progress
		float progress = level == maxLevel ? 1 : data.getAsJsonObject("level").get("progress").getAsFloat();
		this.drawProgress(g, x, y, 102, 8, progress, level == maxLevel ? maxColor : this.getColor());
	}

	private String getPrefixColor(int level) {
		if (level < 40) {
			return "§7";
		} else if (level < 80) {
			return "§f";
		} else if (level < 120) {
			return "§e";
		} else if (level < 160) {
			return "§a";
		} else if (level < 200) {
			return "§2";
		} else if (level < 240) {
			return "§b";
		} else if (level < 280) {
			return "§3";
		} else if (level < 320) {
			return "§9";
		} else if (level < 360) {
			return "§d";
		} else if (level < 400) {
			return "§5";
		} else if (level < 440) {
			return "§6";
		} else if (level < 480) {
			return "§c";
		} else {
			return "§4";
		}
	}
}
