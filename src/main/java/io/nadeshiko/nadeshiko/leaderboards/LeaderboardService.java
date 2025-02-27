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
        jedis.configSet("save", "3600 1"); // every hour if one key changed
        jedis.configSet("save", "900 100"); // every 15 minutes if 100 keys changed
        
        // aof persistence
        jedis.configSet("appendonly", "yes");
        jedis.configSet("appendfsync", "no"); // lets the OS handle this
        
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
                        
                        // Ensure the leaderboard sorted set exists
                        String lbKey = "lb:" + leaderboard.getName();
                        pipeline.exists(lbKey);
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
                
                if (score != 0) {  // Store nonzero scores
                    pipeline.zadd("lb:" + leaderboard.getName(), score, uuid);
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
            
            // Get sorted set entries with scores
            List<Tuple> results;
            if (leaderboard.getSortDirection() == 1) {
                // lowest first
                results = jedis.zrangeWithScores(lbKey, start, end);
            } else {
                // highest first
                results = jedis.zrevrangeWithScores(lbKey, start, end);
            }
            
            int rank = start + 1;
            for (Tuple result : results) {
                String uuid = result.getElement();
                double score = result.getScore();
                
                // Get player details
                String badge = jedis.hget("player:" + uuid, "badge");
                String taggedName = jedis.hget("player:" + uuid, "tagged_name");
                
                JsonObject entry = new JsonObject();
                entry.addProperty("uuid", uuid);
                entry.addProperty("badge", badge);
                entry.addProperty("tagged_name", taggedName);
                entry.addProperty("ranking", rank);
                entry.addProperty("percentile", 100 - (rank / (double) totalEntries) * 100);
                entry.addProperty("value", String.valueOf(score));
                array.add(entry);
                
                rank++;
            }
            
            object.addProperty("count", totalEntries);
            object.add("data", array);
            return object;
        });
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
     * @return A JsonObject containing the rankings of the player for each leaderboard where the player has a score
     */
    public JsonObject getPlayerRankings(String uuid) {
        return executeWithRetry(jedis -> {
            JsonObject rankings = new JsonObject();
            
            for (Leaderboard leaderboard : values()) {
                String lbKey = "lb:" + leaderboard.getName();
                
                // Get player's score
                Double score = jedis.zscore(lbKey, uuid);
                if (score != null) {
                    JsonObject leaderboardData = new JsonObject();
                    
                    //long totalEntries = jedis.zcard(lbKey);
                    
                    Long rank = (leaderboard.getSortDirection() == 1) 
                        ? jedis.zrank(lbKey, uuid) 
                        : jedis.zrevrank(lbKey, uuid);
                    
                    if (rank != null) {
                        rank++; // ranks are originally starting with 0
                        leaderboardData.addProperty("rank", rank);
                        leaderboardData.addProperty("score", score);
                        //leaderboardData.addProperty("percentile", 100 - (rank / (double) totalEntries) * 100);
                        //leaderboardData.addProperty("total_players", totalEntries);
                        
                        rankings.add(leaderboard.getName(), leaderboardData);
                    }
                }
            }
            
            return rankings;
        });
    }

    @FunctionalInterface
    private interface RedisOperation<T> {
        T execute(Jedis jedis);
    }
}
