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
	public void generate(BufferedImage image, JsonObject data, JsonObject stats) {
		generate(image, data, stats, null);
	}


	public void generate(BufferedImage image, JsonObject data, JsonObject stats, JsonObject profileData) {
		// requested card size
		CardGame.CardSize size = CardGame.CardSize.FULL;
		if (data.has("size")) {
			try {
				String sizeStr = data.get("size").getAsString().toUpperCase();
				size = CardGame.CardSize.valueOf(sizeStr);
			} catch (IllegalArgumentException ignored) {
				// Bad size, keep default
			}
		}

		if (profileData == null) {
			profileData = fetchProfileData(stats);
			if (profileData == null) {
				return; // Already logged
			}
		}

		// Render based on size
		switch (size) {
			case FORUMS -> generateForums(image, stats, profileData);
			case TINY -> generateTiny(image, stats, profileData);
			case FULL -> generateFull(image, stats, profileData);
			default -> generateFull(image, stats, profileData);
		}
	}

	private void drawSkillsAndMisc(Graphics2D g, JsonObject profileData, CardGame.CardSize size, int offsetX, int offsetY) {
		// Common data extraction
		int level = profileData.getAsJsonObject("skyblock_level").get("level").getAsInt();
		int maxLevel = 100_000_000; // profileData.getAsJsonObject("skyblock_level").get("max_level").getAsInt();
		String prefixColor = this.getPrefixColor(level);
		String levelText = String.format("Level §8[%s" + level + "§8]", prefixColor);
		float progress = profileData.getAsJsonObject("skyblock_level").get("progress").getAsFloat();

		JsonObject skillsObject = profileData.getAsJsonObject("skills");
		JsonObject skillsData = skillsObject != null && skillsObject.has("skills") ?
				skillsObject.getAsJsonObject("skills") : new JsonObject();

		// Misc stats (MP, Skill Avg, Purse, Bank)
		int mp = 0;
		if (profileData.has("accessories")) {
			JsonObject accessoriesJson = profileData.getAsJsonObject("accessories");
			if (accessoriesJson.has("magical_power")) {
				JsonObject magicalPowerJson = accessoriesJson.getAsJsonObject("magical_power");
				if (magicalPowerJson.has("total")) {
					mp = magicalPowerJson.get("total").getAsInt();
				}
			}
		}

		double skillAverage = 0;
		if (profileData.has("skills") && profileData.getAsJsonObject("skills").has("average")) {
			skillAverage = profileData.getAsJsonObject("skills").get("average").getAsDouble();
		}

		double purse = profileData.get("purse").getAsDouble();
		double bank = profileData.get("bank").getAsDouble();

		g.setColor(level == maxLevel ? maxColor : Color.WHITE);
		g.setFont(bold20);
		MinecraftRenderer.drawCustomString(g, levelText, 592 - MinecraftRenderer.customWidth(g, levelText), 85 + offsetY);
		this.drawProgress(g, 601 + offsetX, 74 + offsetY, 605, 6, progress, level == maxLevel ? maxColor : this.getColor());

		this.drawSkill(g, 600 + offsetX, 100 + offsetY, "Combat", skillsData.getAsJsonObject("combat"), 100, 6, bold15, plain15);
		this.drawSkill(g, 600 + offsetX, 120 + offsetY, "Farming", skillsData.getAsJsonObject("farming"), 100, 6, bold15, plain15);
		this.drawSkill(g, 600 + offsetX, 140 + offsetY, "Fishing", skillsData.getAsJsonObject("fishing"), 100, 6, bold15, plain15);
		this.drawSkill(g, 600 + offsetX, 160 + offsetY, "Mining", skillsData.getAsJsonObject("mining"), 100, 6, bold15, plain15);

		this.drawSkill(g, 853 + offsetX, 100 + offsetY, "Foraging", skillsData.getAsJsonObject("foraging"), 100, 6, bold15, plain15);
		this.drawSkill(g, 853 + offsetX, 120 + offsetY, "Enchanting", skillsData.getAsJsonObject("enchanting"), 100, 6, bold15, plain15);
		this.drawSkill(g, 853 + offsetX, 140 + offsetY, "Alchemy", skillsData.getAsJsonObject("alchemy"), 100, 6, bold15, plain15);
		this.drawSkill(g, 853 + offsetX, 160 + offsetY, "Hunting", skillsData.getAsJsonObject("hunting"), 100, 6, bold15, plain15);

		this.drawSkill(g, 1106 + offsetX, 100 + offsetY, "Taming", skillsData.getAsJsonObject("taming"), 100, 6, bold15, plain15);
		this.drawSkill(g, 1106 + offsetX, 120 + offsetY, "Carpentry", skillsData.getAsJsonObject("carpentry"), 100, 6, bold15, plain15);
		this.drawSkill(g, 1106 + offsetX, 140 + offsetY, "Social", skillsData.getAsJsonObject("social"), 100, 6, bold15, plain15);
		this.drawSkill(g, 1106 + offsetX, 160 + offsetY, "Runecrafting", skillsData.getAsJsonObject("runecrafting"), 100, 6, bold15, plain15);

		// Bottom misc stats
		drawStat(g, DrawStatOptions.builder().label("MP").value(String.format("%,d", mp)).x(1267 + offsetX).y(90 + offsetY).build());
		drawStat(g, DrawStatOptions.builder().label("Skill Average").value(String.format("%.2f", skillAverage)).x(1267 + offsetX).y(114 + offsetY).build());
		drawStat(g, DrawStatOptions.builder().label("Purse").value(NumberUtil.formatNumber(purse)).x(1267 + offsetX).y(138 + offsetY).build());
		drawStat(g, DrawStatOptions.builder().label("Bank").value(NumberUtil.formatNumber(bank)).x(1267 + offsetX).y(162 + offsetY).build());
	}

	private void generateForums(BufferedImage image, JsonObject stats, JsonObject profileData) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		if (profileData == null) {
			profileData = fetchProfileData(stats);
			if (profileData == null) {
				return; // Already logged inside fetchProfileData
			}
		}

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Shared drawing (level, skills, misc)
		this.drawSkillsAndMisc(g, profileData, CardGame.CardSize.FORUMS, 0, 0);

		// Draw dungeons
		JsonObject dungeonsData = profileData.getAsJsonObject("dungeons");
		this.drawDungeonsForums(g, dungeonsData);

		// Draw slayers
		JsonObject slayerObject = profileData.getAsJsonObject("slayer");
		if (slayerObject != null && slayerObject.has("slayers")) {
			JsonObject slayersObject = slayerObject.getAsJsonObject("slayers");
			this.drawSlayer(g, 1064, 245, "Rev", slayersObject.getAsJsonObject("zombie"), 117, 6);
			this.drawSlayer(g, 1064, 270, "Sven", slayersObject.getAsJsonObject("wolf"), 117, 6);
			this.drawSlayer(g, 1064, 295, "Blaze", slayersObject.getAsJsonObject("blaze"), 117, 6);
			this.drawSlayer(g, 1295, 245, "Tara", slayersObject.getAsJsonObject("spider"), 117, 6);
			this.drawSlayer(g, 1295, 270, "Eman", slayersObject.getAsJsonObject("enderman"), 117, 6);
			this.drawSlayer(g, 1295, 295, "Vamp", slayersObject.getAsJsonObject("vampire"), 117, 6);
		}
	}

	private void generateTiny(BufferedImage image, JsonObject stats, JsonObject profileData) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		if (profileData == null) {
			profileData = fetchProfileData(stats);
			if (profileData == null) {
				return; // Already logged inside fetchProfileData
			}
		}

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Re-use the shared drawing method with TINY sizing.
		this.drawSkillsAndMisc(g, profileData, CardGame.CardSize.TINY, 0, 7);
	}

	public void generateFull(BufferedImage image, JsonObject stats, JsonObject profileData) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		if (profileData == null) {
			profileData = fetchProfileData(stats);
			if (profileData == null) {
				return; // Already logged inside fetchProfileData
			}
		}

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw the SkyBlock level
		int level = profileData.getAsJsonObject("skyblock_level").get("level").getAsInt();
		int maxLevel = 100_000_000; //profileData.getAsJsonObject("skyblock_level").get("max_level").getAsInt();
		String prefixColor = this.getPrefixColor(level);
		String levelText = String.format("Level §8[%s" + level + "§8]", prefixColor);
		g.setColor(level == maxLevel ? maxColor : Color.WHITE);
		g.setFont(bold20);
		MinecraftRenderer.drawCustomString(g,levelText, 755 - MinecraftRenderer.customWidth(g, levelText), 80);

		// Draw the SkyBlock level bar
		float progress = profileData.getAsJsonObject("skyblock_level").get("progress").getAsFloat();
		this.drawProgress(g, 772, 71, 654, 8, progress, level == maxLevel ? maxColor : this.getColor());

		// Draw the skills
		JsonObject skillsObject = profileData.getAsJsonObject("skills");
		JsonObject skillsData = skillsObject != null && skillsObject.has("skills") ? 
			skillsObject.getAsJsonObject("skills") : new JsonObject();

		this.drawSkill(g, 772, 108, "Taming", skillsData.getAsJsonObject("taming"), 236, 8, bold18, plain18);
		this.drawSkill(g, 772, 133, "Mining", skillsData.getAsJsonObject("mining"), 236, 8, bold18, plain18);
		this.drawSkill(g, 772, 158, "Foraging", skillsData.getAsJsonObject("foraging"), 236, 8, bold18, plain18);
		this.drawSkill(g, 772, 183, "Enchanting", skillsData.getAsJsonObject("enchanting"), 236, 8, bold18, plain18);
		this.drawSkill(g, 772, 208, "Carpentry", skillsData.getAsJsonObject("carpentry"), 236, 8, bold18, plain18);
		this.drawSkill(g, 1192, 108, "Farming", skillsData.getAsJsonObject("farming"), 236, 8, bold18, plain18);
		this.drawSkill(g, 1192, 133, "Combat", skillsData.getAsJsonObject("combat"), 236, 8, bold18, plain18);
		this.drawSkill(g, 1192, 158, "Fishing", skillsData.getAsJsonObject("fishing"), 236, 8, bold18, plain18);
		this.drawSkill(g, 1192, 183, "Alchemy", skillsData.getAsJsonObject("alchemy"), 236, 8, bold18, plain18);
		this.drawSkill(g, 1192, 208, "Social", skillsData.getAsJsonObject("social"), 236, 8, bold18, plain18);

		// Draw dungeons
		JsonObject dungeonsData = profileData.getAsJsonObject("dungeons");
		this.drawDungeons(g, dungeonsData);

		// Draw slayers
		JsonObject slayerObject = profileData.getAsJsonObject("slayer");
		if (slayerObject != null && slayerObject.has("slayers")) {
			JsonObject slayersObject = slayerObject.getAsJsonObject("slayers");
			this.drawSlayer(g, 1131, 357, "Rev", slayersObject.getAsJsonObject("zombie"), 102, 8);
			this.drawSlayer(g, 1131, 389, "Sven", slayersObject.getAsJsonObject("wolf"), 102, 8);
			this.drawSlayer(g, 1131, 421, "Blaze", slayersObject.getAsJsonObject("blaze"), 102, 8);
			this.drawSlayer(g, 1332, 357, "Tara", slayersObject.getAsJsonObject("spider"), 102, 8);
			this.drawSlayer(g, 1332, 389, "Eman", slayersObject.getAsJsonObject("enderman"), 102, 8);
			this.drawSlayer(g, 1332, 421, "Vamp", slayersObject.getAsJsonObject("vampire"), 102, 8);
		}

		// Draw bottom stuff
		int mp = 0;
		if (profileData.has("accessories")) {
			JsonObject accessoriesJson = profileData.getAsJsonObject("accessories");
			if (accessoriesJson.has("magical_power")) {
				JsonObject magicalPowerJson = accessoriesJson.getAsJsonObject("magical_power");
				if (magicalPowerJson.has("total")) {
					mp = magicalPowerJson.get("total").getAsInt();
				}
			}
		}

		// Get skill average
		double skillAverage = 0;
		if (profileData.has("skills") && profileData.getAsJsonObject("skills").has("average")) {
			skillAverage = profileData.getAsJsonObject("skills").get("average").getAsDouble();
		}

		double purse = profileData.get("purse").getAsDouble();
		double bank = profileData.get("bank").getAsDouble();
		String text = "MP  " + String.format("%,d", mp) + "           Skill Average  " + String.format("%.2f", skillAverage) +
			"           Purse  " + NumberUtil.formatNumber(purse) + "           Bank  " + NumberUtil.formatNumber(bank);
		g.setColor(new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString(text, (int) (1000 - g.getFontMetrics().stringWidth(text) / 2d), 255);
	}

	private void drawSkill(Graphics2D g, int x, int y, String name, JsonObject data, int progressWidth, int progressHeight, Font levelFont, Font nameFont) {
		// Default to level 0 if data is missing
		int level = 0;
		float progress = 0;
		boolean isMaxed = false;

		if (data != null) {
			level = data.has("level") ? data.get("level").getAsInt() : 0;
			if (data.has("max_level")) {
				isMaxed = level >= data.get("max_level").getAsInt();
			}
			progress = isMaxed ? 1 : (data.has("progress") ? data.get("progress").getAsFloat() : 0);
		}

		// Draw the skill level
		String levelText = " " + level;
		g.setColor(isMaxed ? maxColor : Color.WHITE);
		g.setFont(levelFont);
		g.drawString(levelText, x - 15 - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill name
		g.setColor(isMaxed ? maxColor : new Color(181, 181, 181));
		g.setFont(nameFont);
		g.drawString(name, x - 20 - g.getFontMetrics().stringWidth(name) - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the skill progress
		this.drawProgress(g, x, y, progressWidth, progressHeight, progress, isMaxed ? maxColor : this.getColor());
	}

	private void drawDungeonsForums(Graphics2D g, JsonObject data) {
		// Get catacombs data, defaulting to 0 if missing
		int cataLevel = 0;
		float cataProgress = 0;
		boolean isMaxed = false;
		
		if (data != null && data.has("catacombs")) {
			JsonObject catacombsData = data.getAsJsonObject("catacombs");
			if (catacombsData != null && catacombsData.has("level")) {
				JsonObject catacombsLevel = catacombsData.getAsJsonObject("level");
				cataLevel = catacombsLevel.get("level").getAsInt();
				cataProgress = catacombsLevel.get("progress").getAsFloat();
				isMaxed = cataLevel >= catacombsLevel.get("max_level").getAsInt();
			}
		}

		// Draw Catacombs level
		String cataLevelText = " " + cataLevel;
		g.setColor(isMaxed ? maxColor : Color.WHITE);
		g.setFont(bold16);
		g.drawString(cataLevelText, 576, 254);

		// Draw "Catacombs"
		g.setColor(isMaxed ? maxColor : new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString("Catacombs", 486, 254);

		// Draw Catacombs level progress
		this.drawProgress(g, 609, 245, 312, 7, cataProgress, isMaxed ? maxColor : this.getColor());

		// Draw classes
		JsonObject classesObject = null;
		if (data != null) {
			classesObject = data.getAsJsonObject("classes");
		}
		JsonObject classesData = classesObject != null && classesObject.has("classes") ? 
			classesObject.getAsJsonObject("classes") : new JsonObject();

		this.drawClass(g, 506, 282, "Archer", classesData.getAsJsonObject("archer"));
		this.drawClass(g, 506, 305, "Tank", classesData.getAsJsonObject("tank"));
		this.drawClass(g, 652, 282, "Berserk", classesData.getAsJsonObject("berserk"));
		this.drawClass(g, 652, 305, "Mage", classesData.getAsJsonObject("mage"));
		this.drawClass(g, 802, 282, "Healer", classesData.getAsJsonObject("healer"));

		// Draw class average
		float classAvg = classesData != null && classesData.has("average_level") ? 
		classesData.get("average_level").getAsFloat() : 0;
		boolean max = classesData != null && classesData.has("maxed") && 
		classesData.get("maxed").getAsBoolean();
		
		g.setColor(max ? maxColor : new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString("Class Average", 781, 305);
		g.setColor(max ? maxColor : Color.WHITE);
		g.setFont(bold16);
		g.drawString(String.valueOf(classAvg), 781 + g.getFontMetrics().stringWidth("Class Average "), 305);
	}

	private void drawDungeons(Graphics2D g, JsonObject data) {
		// Get catacombs data, defaulting to 0 if missing
		int cataLevel = 0;
		float cataProgress = 0;
		boolean isMaxed = false;
		
		if (data != null && data.has("catacombs")) {
			JsonObject catacombsData = data.getAsJsonObject("catacombs");
			if (catacombsData != null && catacombsData.has("level")) {
				JsonObject catacombsLevel = catacombsData.getAsJsonObject("level");
				cataLevel = catacombsLevel.get("level").getAsInt();
				cataProgress = catacombsLevel.get("progress").getAsFloat();
				isMaxed = cataLevel >= catacombsLevel.get("max_level").getAsInt();
			}
		}

		// Draw Catacombs level
		String cataLevelText = " " + cataLevel;
		g.setColor(isMaxed ? maxColor : Color.WHITE);
		g.setFont(bold18);
		g.drawString(cataLevelText, 730, 367);

		// Draw "Catacombs"
		g.setColor(isMaxed ? maxColor : new Color(181, 181, 181));
		g.setFont(plain18);
		g.drawString("Catacombs", 627, 367);

		// Draw Catacombs level progress
		this.drawProgress(g, 773, 357, 217, 8, cataProgress, isMaxed ? maxColor : this.getColor());

		// Draw classes
		JsonObject classesObject = null;
		if (data != null) {
			classesObject = data.getAsJsonObject("classes");
		}
		JsonObject classesData = classesObject != null && classesObject.has("classes") ? 
			classesObject.getAsJsonObject("classes") : new JsonObject();

		this.drawClass(g, 655, 397, "Archer", classesData.getAsJsonObject("archer"));
		this.drawClass(g, 655, 420, "Healer", classesData.getAsJsonObject("healer"));
		this.drawClass(g, 655, 444, "Tank", classesData.getAsJsonObject("tank"));
		this.drawClass(g, 830, 397, "Berserk", classesData.getAsJsonObject("berserk"));
		this.drawClass(g, 830, 420, "Mage", classesData.getAsJsonObject("mage"));

		// Draw class average
		float classAvg = classesData != null && classesData.has("average_level") ? 
		classesData.get("average_level").getAsFloat() : 0;
		boolean max = classesData != null && classesData.has("maxed") && 
		classesData.get("maxed").getAsBoolean();
		
		g.setColor(max ? maxColor : new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString("Class Average", 802, 444);
		g.setColor(max ? maxColor : Color.WHITE);
		g.setFont(bold16);
		g.drawString(String.valueOf(classAvg), 802 + g.getFontMetrics().stringWidth("Class Average "), 444);
	}

	private void drawClass(Graphics2D g, int x, int y, String name, JsonObject data) {
		int level = 0;
		boolean isMaxed = false;

		if (data != null && data.has("level")) {
			JsonObject levelData = data.getAsJsonObject("level");
			if (levelData != null) {
				level = levelData.get("level").getAsInt();
				isMaxed = level >= levelData.get("max_level").getAsInt();
			}
		}

		// Draw name
		g.setColor(isMaxed ? maxColor : new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString(name, x, y);

		// Draw level
		g.setColor(isMaxed ? maxColor : Color.WHITE);
		g.setFont(bold16);
		g.drawString(String.valueOf(level), x + g.getFontMetrics().stringWidth(name + " "), y);
	}

	private void drawSlayer(Graphics2D g, int x, int y, String name, JsonObject data, int progressWidth, int progressHeight) {
		int level = 0;
		float progress = 0;
		boolean isMaxed = false;

		if (data != null && data.has("level")) {
			JsonObject levelData = data.getAsJsonObject("level");
			if (levelData != null) {
				level = levelData.get("current_level").getAsInt();
				isMaxed = level >= levelData.get("max_level").getAsInt();
				progress = isMaxed ? 1 : levelData.get("progress").getAsFloat();
			}
		}

		// Draw the slayer level
		String levelText = " " + level;
		g.setColor(isMaxed ? maxColor : Color.WHITE);
		g.setFont(bold16);
		g.drawString(levelText, x - 12 - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the slayer name
		g.setColor(isMaxed ? maxColor : new Color(181, 181, 181));
		g.setFont(plain16);
		g.drawString(name, x - 15 - g.getFontMetrics().stringWidth(name) - g.getFontMetrics().stringWidth(levelText), y + 10);

		// Draw the slayer progress
		this.drawProgress(g, x, y, progressWidth, progressHeight, progress, isMaxed ? maxColor : this.getColor());
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

	private JsonObject fetchProfileData(@NonNull JsonObject stats) {
		try {
			// First get UUID from PlayerDB
			HTTPUtil.Response profileResponse = HTTPUtil.get("https://playerdb.co/api/player/minecraft/" + stats.get("name").getAsString());
			JsonObject minecraftProfile = JsonParser.parseString(profileResponse.response()).getAsJsonObject();

			if (minecraftProfile == null || !minecraftProfile.has("data") ||
					minecraftProfile.get("code").getAsString().equals("minecraft.invalid_username")) {
				Nadeshiko.logger.error("Could not find player {}", stats.get("name").getAsString());
				return null;
			}

			String uuid = minecraftProfile.getAsJsonObject("data").getAsJsonObject("player").get("id").getAsString();
			JsonObject skyblockData = Nadeshiko.INSTANCE.getSkyBlockCache().get(uuid, null);

			if (!skyblockData.get("success").getAsBoolean()) {
				Nadeshiko.logger.error("Failed to fetch SkyBlock data for {}: {}",
					stats.get("name").getAsString(),
					skyblockData.get("cause").getAsString());
				return null;
			}

			JsonObject profileData = skyblockData.getAsJsonObject("skyblock_profile");

			// Ensure that the profile was found
			if (profileData == null) {
				Nadeshiko.logger.error("No SkyBlock profile found for {}", stats.get("name").getAsString());
			}

			return profileData;
		} catch (Exception e) {
			Nadeshiko.logger.error("Encountered error when fetching SkyBlock stats for {}", stats.get("name").getAsString(), e);
			return null;
		}
	}
}
