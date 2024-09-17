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

import static io.nadeshiko.nadeshiko.leaderboards.Leaderboard.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage leaderboards.
 * <p>
 * The leaderboard system contains two databases: one containing a large list of players and their stats, and one which
 * contains the same players and their leaderboard positions for each stat.
 * <p>
 * The second database, referred to as the placement database, is derived from the first database, referred to as the
 * stat database. The stat database is updated in real time as players are searched - either inserting a new entry (if
 * a player has never been searched on nadeshiko before), or updating an existing one. The placement database is heavily
 * cached due to the scale of the operation that is generating it. The placement database is regenerated at a regular
 * interval by the {@link LeaderboardService#update()} method.
 * <p>
 * While referred to as "databases", the two databases exist as collections within the "nadeshiko" Mongo database.
 *
 * @author chloe
 * @since 0.9.0
 */
public class LeaderboardService {

    /**
     * This service's logger
     */
    private final Logger logger = LoggerFactory.getLogger("Leaderboard Service");

    /**
     * The {@link MongoClient} used to connect to the nadeshiko database
     */
    private MongoClient mongoClient;

    /**
     * A {@link MongoDatabase} reference to the nadeshiko database
     */
    private MongoDatabase nadeshikoDatabase;

    /**
     * Called on server startup - connect the service to Mongo, creating the database and collections if required
     * @param uri The URI of the Mongo instance to connect to
     */
    public void connect(String uri) {
        this.logger.info("Connecting to {}...", uri);

        this.mongoClient = MongoClients.create(new ConnectionString(uri));
        this.nadeshikoDatabase = this.mongoClient.getDatabase("nadeshiko");

        // Dump leaderboards
        this.dumpLeaderboards();
    }

    /**
     * Called on server shutdown - cleanly disconnect from Mongo
     */
    public void disconnect() {
        this.mongoClient.close();
    }

    /**
     * Called when a player is searched on nadeshiko. Insert them into the stat database.
     * @param player The JsonObject containing the player's stats.
     */
    public void insertPlayer(JsonObject player) {

        Document playerDocument = new Document();

        // Base
        JsonObject profile = player.getAsJsonObject("profile");
        playerDocument
            .append("uuid", player.get("uuid").getAsString())
            .append("tagged_name", profile.get("tagged_name").getAsString())
            .append("time", System.currentTimeMillis());

        // Populate leaderboards
        for (Leaderboard leaderboard : values()) {

            // Handle SkyBlock separately
            if (leaderboard.getCategory().equals(LeaderboardCategory.SKYBLOCK)) {
                insertSkyBlock(player.get("uuid").getAsString(), playerDocument);
                continue;
            }

            JsonObject leaderboardInput = leaderboard.getCategory().getDeriveInput(player);
            playerDocument.append(leaderboard.name(), leaderboard.derive(leaderboardInput));
        }

        // Delete old player stats, if present
        this.nadeshikoDatabase.getCollection("stats")
            .deleteMany(new Document("uuid", player.get("uuid").getAsString()));

        // Insert new stats
        this.nadeshikoDatabase.getCollection("stats").insertOne(playerDocument);
    }

    private void insertSkyBlock(String uuid, Document playerDocument) {
        // TODO: request SkyBlock API
    }

    // TODO: THIS NEEDS CACHING!
    public JsonObject get(Leaderboard leaderboard, int page) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        long entries = this.nadeshikoDatabase.getCollection("stats").countDocuments(
            new Document(leaderboard.name(), new Document("$exists", true).append("$ne", 0))
        );

        List<Document> documents = this.getDocuments(leaderboard, page);
        for (int i = 0; i < documents.size(); i++) {

            Document document = documents.get(i);
            int start = (page - 1) * 100 + 1;

            if (document.get(leaderboard.name()) == null) {
                continue;
            }

            JsonObject entry = new JsonObject();
            entry.addProperty("uuid", document.getString("uuid"));
            entry.addProperty("tagged_name", document.getString("tagged_name"));
            entry.addProperty("ranking", start + i);
            entry.addProperty("percentile", 100 - ((start + i) / (double) entries) * 100);
            entry.addProperty("value", document.get(leaderboard.name()).toString());
            array.add(entry);
        }

        object.addProperty("count", entries);
        object.add("data", array);
        return object;
    }

    private List<Document> getDocuments(Leaderboard leaderboard, int page) {
        Document filter = new Document(leaderboard.name(), new Document("$exists", true).append("$ne", 0));
        Document sort = new Document(leaderboard.name(), leaderboard.getSortDirection()).append("uuid", -1);

        // Query the stats collection, apply the filter, sort and limit the results
        try (MongoCursor<Document> cursor = this.nadeshikoDatabase.getCollection("stats")
                .find(filter)
                .sort(sort)
                .skip((page - 1) * 100)
                .limit(100)
                .iterator()) {

            List<Document> results = new ArrayList<>();
            while (cursor.hasNext()) {
                results.add(cursor.next());
            }
            return results;
        }
    }

    /**
     * Called at a regular interval. Regenerate the placement database from the stat database.
     */
    private void update() {

    }

    private void dumpLeaderboards() {
        JsonObject lbsObject = new JsonObject();

        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            JsonArray array = new JsonArray();

            for (Leaderboard leaderboard : values()) {
                if (leaderboard.getCategory().equals(category)) {
                    array.add(leaderboard.name());
                }
            }

            lbsObject.add(category.name(), array);
        }

        try {
            File leaderboards = new File("leaderboards.json");
            Files.write(leaderboards.toPath(), lbsObject.toString().getBytes());
            logger.info("Dumped leaderboards to leaderboards.json");
        } catch (Exception e) {
            logger.error("Failed to dump leaderboards!", e);
        }
    }
}
