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

import static io.nadeshiko.nadeshiko.leaderboards.Leaderboard.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.resps.Tuple;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service to manage leaderboards using Redis.Uses ZSET to maintain player rankings.
 *
 * @since 0.9.0
 */
public class LeaderboardService {

    /**
     * This service's logger
     */
    private final Logger logger = LoggerFactory.getLogger("Leaderboard Service");

    /**
     * The Redis connection pool
     */
    private JedisPool jedisPool;

    /**
     * Maximum number of retries for Redis operations
     */
    private static final int MAX_RETRIES = 3;

    /**
     * Called on server startup - connect the service to Redis
     * @param uri The URI of the Redis instance to connect to
     */
    public void connect(String uri) {
        this.logger.info("Connecting to {}...", uri);
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(16);
        poolConfig.setMinIdle(8);
        poolConfig.setMinEvictableIdleDuration(Duration.ofMinutes(5));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMinutes(1));
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(30));
        poolConfig.setTestOnBorrow(true);
        
        this.jedisPool = new JedisPool(poolConfig, uri);
        
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            
            // Configure Redis persistence
            configureRedisPersistence(jedis);
            
            this.logger.info("Successfully connected to Redis!");
            
            // Initialize leaderboards
            initializeLeaderboards();
        } catch (Exception e) {
            this.logger.error("Failed to connect to Redis", e);
            throw e;
        }
    }

    //Configure Redis persistence settings for data safety
    private void configureRedisPersistence(Jedis jedis) {
        // Configure RDB (snapshot) persistence
        //jedis.configSet("save", "3600 1"); // every hour if one key changed
        //jedis.configSet("save", "900 100"); // every 15 minutes if 100 keys changed
        
        // aof persistence
        //jedis.configSet("appendonly", "yes");
        //jedis.configSet("appendfsync", "no"); // lets the OS handle this
        
        logger.info("Configured Redis persistence settings");
    }

    /**
     * Execute a Redis operation with automatic retry on connection failure
     */
    private <T> T executeWithRetry(RedisOperation<T> operation) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try (Jedis jedis = jedisPool.getResource()) {
                return operation.execute(jedis);
            } catch (JedisConnectionException e) {
                attempts++;
                if (attempts == MAX_RETRIES) {
                    logger.error("Failed to execute Redis operation after {} attempts! Is the Redis server running?", MAX_RETRIES, e);
                    throw e;
                }
                logger.warn("Redis connection failed, attempt {}/{}", attempts, MAX_RETRIES);
                try {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry! This should not happen.", ie);
                }
            }
        }
        throw new RuntimeException("nadeshiko backend has completed the challenge [How Did We Get Here?]");
    }

    /**
     * Called on server shutdown - cleanly disconnect from Redis
     */
    public void disconnect() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            try (Jedis jedis = jedisPool.getResource()) {
                // Try background save first
                try {
                    jedis.bgsave();
                    logger.info("Initiated background Redis save before shutdown");
                    
                    // Wait for bgsave
                    Thread.sleep(2000);
                } catch (Exception e) {
                    // Try normal save
                    logger.warn("Background save failed, attempting normal save", e);
                    try {
                        jedis.save();
                        logger.info("Completed Redis save before shutdown");
                    } catch (Exception saveEx) {
                        logger.error("Failed to complete Redis save", saveEx);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during Redis shutdown", e);
            } finally {
                try {
                    jedisPool.close();
                } catch (Exception e) {
                    logger.error("Error closing Redis pool", e);
                }
            }
        }
    }

    // Initialize all leaderboards in Redis if they don't exist
    private void initializeLeaderboards() {
        executeWithRetry(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            
            // Create metadata about available leaderboards
            for (LeaderboardCategory category : LeaderboardCategory.values()) {
                String categoryKey = "category:" + category.name();
                pipeline.del(categoryKey); // Clear existing category data
                
                for (Leaderboard leaderboard : values()) {
                    if (leaderboard.getCategory().equals(category)) {
                        // Add leaderboard to category set
                        pipeline.sadd(categoryKey, leaderboard.getName());
                        
                        // Ensure the leaderboard sorted set exists, enforce cap
                        String lbKey = "lb:" + leaderboard.getName();
                        pipeline.exists(lbKey);
                        
                        int cap = leaderboard.getCap();
                        if (cap > 0) {
                            if (leaderboard.getSortDirection() == -1) {
                                // Descending order
                                pipeline.zremrangeByRank(lbKey, 0, -(cap + 1));
                            } else {
                                // Ascending order
                                pipeline.zremrangeByRank(lbKey, cap, -1);
                            }
                        }
                    }
                }
            }
            
            pipeline.sync();
            logger.info("Initialized leaderboard structure in Redis");
            return null;
        });
    }

    /**
     * Called when a player is searched on nadeshiko. Update their Redis data
     * @param player The JsonObject containing the player's stats.
     */
    public synchronized void insertPlayer(JsonObject player) {
        String uuid = player.get("uuid").getAsString();
        JsonObject profile = player.getAsJsonObject("profile");
        
        executeWithRetry(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            
            // The hash for player data
            pipeline.hset("player:" + uuid, "badge", player.get("badge").getAsString());
            pipeline.hset("player:" + uuid, "tagged_name", profile.get("tagged_name").getAsString());
            pipeline.hset("player:" + uuid, "time", String.valueOf(System.currentTimeMillis()));
            
            // Update leaderboard scores/vals
            for (Leaderboard leaderboard : values()) {
                JsonObject leaderboardInput = leaderboard.getCategory().getDeriveInput(player);
                Number scoreNum = leaderboard.derive(leaderboardInput);
                double score = scoreNum.doubleValue();
                String lbKey = "lb:" + leaderboard.getName();
                
                if (score != 0) {  // Store nonzero scores
                    pipeline.zadd(lbKey, score, uuid);
                    
                    // Apply cap
                    int cap = leaderboard.getCap();
                    if (cap > 0) {
                        if (leaderboard.getSortDirection() == -1) {
                            // Descending order
                            pipeline.zremrangeByRank(lbKey, 0, -(cap + 1));
                        } else {
                            // Ascending order
                            pipeline.zremrangeByRank(lbKey, cap, -1);
                        }
                    }
                }
            }
            
            pipeline.sync();
            return null;
        });
    }

    /**
     * Called when a guild is searched on nadeshiko. Update Redis data for the guild.
     * @param guild The JsonObject containing the guild's data.
     */
    public synchronized void insertGuild(JsonObject guild) {
        String guildId = guild.get("id").getAsString();
        String guildName = guild.get("name").getAsString();
        
        executeWithRetry(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            String guildNameWithTag = guildName;

            // guild metadata
            if (guild.has("tag")) {
                guildNameWithTag = guildName + " " + guild.get("tag").getAsString();
            }
            pipeline.hset("guild:" + guildId, "time", String.valueOf(System.currentTimeMillis()));
            pipeline.hset("guild:" + guildId, "tagged_name", guildNameWithTag);
            pipeline.hset("guild:" + guildId, "name", guildName);
            
            // leaderboard scores
            for (Leaderboard leaderboard : values()) {
                if (leaderboard.getCategory() == LeaderboardCategory.GUILDS) {
                    JsonObject leaderboardInput = leaderboard.getCategory().getDeriveInput(guild);
                    Number scoreNum = leaderboard.derive(leaderboardInput);
                    double score = scoreNum.doubleValue();
                    String lbKey = "lb:" + leaderboard.getName();
                    
                    if (score != 0) {
                        pipeline.zadd(lbKey, score, guildId);
                        
                        // apply cap
                        int cap = leaderboard.getCap();
                        if (cap > 0) {
                            if (leaderboard.getSortDirection() == -1) {
                                // Descending
                                pipeline.zremrangeByRank(lbKey, 0, -(cap + 1));
                            } else {
                                // Ascending
                                pipeline.zremrangeByRank(lbKey, cap, -1);
                            }
                        }
                    }
                }
            }
            
            pipeline.sync();
            return null;
        });
    }

    /**
     * Get a list of all leaderboards in a category. This is only used for dumping leaderboards
     * @param category The category to get leaderboards for
     * @return A JsonArray of leaderboard names
     */
    public JsonArray getLeaderboardsInCategory(LeaderboardCategory category) {
        return executeWithRetry(jedis -> {
            JsonArray array = new JsonArray();
            String categoryKey = "category:" + category.name();
            for (String leaderboardName : jedis.smembers(categoryKey)) {
                array.add(leaderboardName);
            }
            return array;
        });
    }

    public synchronized JsonObject get(Leaderboard leaderboard, int page) {
        return executeWithRetry(jedis -> {
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray();
            String lbKey = "lb:" + leaderboard.getName();
            
            // Get total number of entries
            long totalEntries = jedis.zcard(lbKey);
            
            // Calculate range for pagination (zero-based)
            int start = (page - 1) * 100;
            int end = start + 99;
            
            // +1 entry for tie detection
            int actualStart = Math.max(0, start - 1);
            boolean fetchedExtra = (actualStart < start);
            
            // Get sorted set entries with scores
            List<Tuple> results;
            if (leaderboard.getSortDirection() == 1) {
                // lowest first
                results = jedis.zrangeWithScores(lbKey, actualStart, end);
            } else {
                // highest first
                results = jedis.zrevrangeWithScores(lbKey, actualStart, end);
            }
            
            double previousScore = Double.NaN;
            int trueRank = actualStart + 1;
            int displayRank = trueRank;
            
            int startIndex = fetchedExtra ? 1 : 0;
            
            if (fetchedExtra && results.size() > 1) {
                double extraScore = results.get(0).getScore();
                double firstPageScore = results.get(1).getScore();
                
                // find the real rank by scanning upward
                if (Math.abs(extraScore - firstPageScore) < Double.MIN_VALUE) {
                    // need to find where it begins
                    int tieStartRank = findTieStartRank(jedis, lbKey, extraScore, actualStart, leaderboard.getSortDirection());
                    displayRank = tieStartRank;
                    trueRank = start + 1;
                    previousScore = extraScore;
                } else {
                    // no tie
                    previousScore = extraScore;
                    displayRank = trueRank = start + 1;
                }
            }
            
            // process results for the current page
            for (int i = startIndex; i < results.size(); i++) {
                Tuple result = results.get(i);
                String uuid = result.getElement();
                double score = result.getScore();
                
                // use the same display rank (tie)
                boolean isTie = false;
                if (i > startIndex && Math.abs(score - previousScore) < Double.MIN_VALUE) {
                    // same score
                    isTie = true;
                } else if (i > startIndex) {
                    // new score, update displayed rank
                    displayRank = trueRank;
                }
                
                String badge;
                String taggedName;
                String name;

                JsonObject entry = new JsonObject();

                if (lbKey.startsWith("lb:GUILD_")) {
                    badge = jedis.hget("guild:" + uuid, "badge");
                    taggedName = jedis.hget("guild:" + uuid, "tagged_name");
                    name = jedis.hget("guild:" + uuid, "name");
                    entry.addProperty("name", name);
                } else {
                    badge = jedis.hget("player:" + uuid, "badge");
                    taggedName = jedis.hget("player:" + uuid, "tagged_name");
                }
                
                entry.addProperty("uuid", uuid);
                entry.addProperty("badge", badge);
                entry.addProperty("tagged_name", taggedName);
                entry.addProperty("ranking", displayRank);
                entry.addProperty("percentile", 100 - (displayRank / (double) totalEntries) * 100);
                entry.addProperty("value", String.valueOf(score));
                entry.addProperty("tie", isTie);
                array.add(entry);
                
                previousScore = score;
                trueRank++;
            }
            
            object.addProperty("count", totalEntries);
            object.add("data", array);
            return object;
        });
    }
    
    /**
     * Find the starting rank for a tie by searching upward in the leaderboard
     * @param jedis Redis connection
     * @param lbKey Leaderboard key
     * @param targetScore The score to find ties for
     * @param position Current position in the leaderboard
     * @param sortDirection Direction of sorting (1 for ascending, -1 for descending)
     * @return The true starting rank for the tied entries
     */
    private int findTieStartRank(Jedis jedis, String lbKey, double targetScore, int position, int sortDirection) {
        // binary search approach to find the first entry with a different score
        int low = 0;
        int high = position;
        int tieStart = position;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            List<Tuple> entries;
            
            if (sortDirection == 1) {
                // ascending order
                entries = jedis.zrangeWithScores(lbKey, mid, mid);
            } else {
                // descending order
                entries = jedis.zrevrangeWithScores(lbKey, mid, mid);
            }
            
            if (entries.isEmpty()) {
                break;
            }
            
            double midScore = entries.get(0).getScore();
            
            if (Math.abs(midScore - targetScore) < Double.MIN_VALUE) {
                // found tie; search lower
                tieStart = mid;
                high = mid - 1;
            } else {
                // different score; search higher
                low = mid + 1;
            }
        }
        
        // rank starts at 1, not 0
        return tieStart + 1;
    }

    private void dumpLeaderboards() {
        JsonObject lbsObject = new JsonObject();

        for (LeaderboardCategory category : LeaderboardCategory.values()) {
            lbsObject.add(category.name(), getLeaderboardsInCategory(category));
        }

        try {
            File leaderboards = new File("leaderboards.json");
            Files.write(leaderboards.toPath(), lbsObject.toString().getBytes());
            logger.info("Dumped leaderboards to leaderboards.json");
        } catch (Exception e) {
            logger.error("Failed to dump leaderboards!", e);
        }
    }

    /**
     * Get the rankings of a player for all leaderboards
     * @param uuid The UUID of the player to get rankings for
     * @return A JsonArray containing the rankings of the player for each leaderboard where the player has a score
     */
    public JsonArray getPlayerRankings(String uuid) {
        return executeWithRetry(jedis -> {
            List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
            
            for (Leaderboard leaderboard : values()) {
                String lbKey = "lb:" + leaderboard.getName();
                
                // Get player's score
                Double score = jedis.zscore(lbKey, uuid);
                if (score != null) {
                    Long position = (leaderboard.getSortDirection() == 1) 
                        ? jedis.zrank(lbKey, uuid) 
                        : jedis.zrevrank(lbKey, uuid);
                    
                    if (position != null) {
                        int tieAdjustedRank = findTrueRankForScore(jedis, lbKey, score, position, leaderboard.getSortDirection());
                        leaderboardEntries.add(new LeaderboardEntry(leaderboard.getName(), tieAdjustedRank, score));
                    }
                }
            }
            
            // Sort by rank
            leaderboardEntries.sort(Comparator.comparing(LeaderboardEntry::rank));
            
            // Convert to JSON
            JsonArray rankings = new JsonArray();
            for (LeaderboardEntry entry : leaderboardEntries) {
                JsonObject leaderboardData = new JsonObject();
                leaderboardData.addProperty("id", entry.name());
                leaderboardData.addProperty("rank", entry.rank());
                leaderboardData.addProperty("score", entry.score());
                rankings.add(leaderboardData);
            }
            
            return rankings;
        });
    }
    
    /**
     * Find the true rank for a player by checking for ties above them
     * @param jedis Redis connection
     * @param lbKey Leaderboard key
     * @param targetScore The player's score
     * @param position The player's position in the sorted set
     * @param sortDirection Direction of sorting (1 for ascending, -1 for descending)
     * @return The true rank accounting for ties
     */
    private int findTrueRankForScore(Jedis jedis, String lbKey, double targetScore, long position, int sortDirection) {
        // check if there are any ties. in case of ties, return the highest rank (lowest number)
        return findTieStartRank(jedis, lbKey, targetScore, (int)position, sortDirection);
    }

    private record LeaderboardEntry(String name, long rank, double score) {}

    @FunctionalInterface
    private interface RedisOperation<T> {
        T execute(Jedis jedis);
    }
}
