package io.nadeshiko.nadeshiko.hypixel;

import com.google.gson.JsonObject;
import io.nadeshiko.nadeshiko.Nadeshiko;
import io.nadeshiko.nadeshiko.util.MinecraftColors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Utility class for the parsing and formatting of ranks on the network.
 * @author chloe
 */
@Getter
public class RankHelper {

	private String prefix = null;
	private Rank rank = null;
	private boolean hasSuperstar = false;
	private PackageRank packageRank = PackageRank.NONE;

	private String superstarRankColor = "§6"; // default; fallback
	private String superstarPlusColor = "§c"; // default; fallback

	/**
	 * Create a new RankHelper instance from a players API player object
	 * @param playerObject The player object as provided by the Hypixel API
	 */
	public RankHelper(@NonNull JsonObject playerObject) {

		// 1. Check if the player has a custom prefix
		if (playerObject.has("prefix")) {
			String candidatePrefix = playerObject.get("prefix").getAsString();

			// Apparently this will happen if the player once had a prefix and lost it. Can't verify
			if (!candidatePrefix.equals("NONE") && !candidatePrefix.isEmpty()) {
				this.prefix = candidatePrefix;
				return; // Nothing else matters
			}
		}

		// 2. Check if the player has a rank
		if (playerObject.has("rank")) {
			String candidateRank = playerObject.get("rank").getAsString();

			// Happens if the player once had a rank and lost it
			if (!candidateRank.equals("NONE") && !candidateRank.isEmpty()) {

				// In case I'm forgetting about a rank or one gets added
				try {
					this.rank = Rank.valueOf(candidateRank);
					return; // Nothing else matters
				} catch (Exception e) {
					Nadeshiko.logger.warn("Found unrecognized rank {} on player {}!",
						candidateRank, playerObject.get("displayname"));
				}
			}
		}

		// 3. Check if the player is currently a superstar (MVP++)
		if (playerObject.has("monthlyPackageRank")) {
			if (playerObject.get("monthlyPackageRank").getAsString().equals("SUPERSTAR")) {
				this.hasSuperstar = true;

				this.superstarRankColor = MinecraftColors.getCodeFromName(
					playerObject.get("monthlyRankColor").getAsString());
				this.superstarPlusColor = MinecraftColors.getCodeFromName(
					playerObject.get("rankPlusColor").getAsString());

				return; // Nothing else matters
			}
		}

		// 4. Check if the player has a new package rank
		if (playerObject.has("newPackageRank")) {
			String candidatePackageRank = playerObject.get("newPackageRank").getAsString();

			// Happens if the player once had a rank and lost it - i.e. chargeback
			if (!candidatePackageRank.equals("NONE") && !candidatePackageRank.isEmpty()) {

				// MVP+ plus color
				if (playerObject.has("rankPlusColor")) {
					this.superstarPlusColor = MinecraftColors.getCodeFromName(
						playerObject.get("rankPlusColor").getAsString());
				}

				// In case something weird is going on
				try {
					this.packageRank = PackageRank.valueOf(candidatePackageRank);
					return; // Nothing else matters
				} catch (Exception e) {
					Nadeshiko.logger.warn("Found unrecognized newPackageRank {} on player {}!",
						candidatePackageRank, playerObject.get("displayname"));
				}
			}
		}

		// 5. Check if the player has a legacy package rank
		if (playerObject.has("packageRank")) {
			String candidatePackageRank = playerObject.get("packageRank").getAsString();

			// Happens if the player once had a rank and lost it - i.e. chargeback
			if (!candidatePackageRank.equals("NONE") && !candidatePackageRank.isEmpty()) {

				// MVP+ plus color (is MVP+ even possible as a legacy rank?)
				if (playerObject.has("rankPlusColor")) {
					this.superstarPlusColor = MinecraftColors.getCodeFromName(
						playerObject.get("rankPlusColor").getAsString());
				}

				// In case something weird is going on
				try {
					this.packageRank = PackageRank.valueOf(candidatePackageRank);
				} catch (Exception e) {
					Nadeshiko.logger.warn("Found unrecognized packageRank {} on player {}!",
						candidatePackageRank, playerObject.get("displayname"));
				}
			}
		}
	}

	public String getTag() {

		// Highest priority. If the player has a prefix, use it.
		if (this.prefix != null) {
			return this.prefix;
		}

		// Second-highest priority. If the player has a rank, use it.
		if (this.rank != null) {
			return this.rank.getFormattedTag();
		}

		// MVP++
		if (this.hasSuperstar) {
			return "y[MVPx++y]".
				replace("y", this.superstarRankColor).
				replace("x", this.superstarPlusColor);
		}

		// Lowest priority. Use the player's package rank.
		if (this.packageRank != null) {

			// If the player is MVP+, we must modify the prefix to use the custom + color
			if (this.packageRank.equals(PackageRank.MVP_PLUS)) {
				return this.packageRank.getFormattedTag().replace("x", this.superstarPlusColor);
			}

			return this.packageRank.getFormattedTag();
		}

		return ""; // Final fallback, no rank
	}

	@Getter
	@AllArgsConstructor
	public enum PackageRank {
		NONE("§7", "§7"),
		VIP("§a[VIP]", "§a"),
		VIP_PLUS("§a[VIP§6+§a]", "§a"),
		MVP("§b[MVP]", "§b"),
		MVP_PLUS("§b[MVPx+§b]", "§b"); // X is a placeholder for the + color.

		final String formattedTag;
		final String nameColor;
	}

	@Getter
	@AllArgsConstructor
	public enum Rank {
		ADMIN("§c[ADMIN]", "§c"),
		GAME_MASTER("§2[GM]", "§2"),
		YOUTUBER("§c[§fYOUTUBE§c]", "§c");

		final String formattedTag;
		final String nameColor;
	}
}
