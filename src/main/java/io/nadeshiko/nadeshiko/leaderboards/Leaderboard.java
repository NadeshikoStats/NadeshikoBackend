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

//import static io.nadeshiko.nadeshiko.leaderboards.LeaderboardCategory.*;

import com.google.gson.JsonObject;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonArray;

//import io.nadeshiko.nadeshiko.util.JsonUtil;
import lombok.Getter;
//import lombok.RequiredArgsConstructor;

//import java.util.ArrayList;
//import java.util.List;
import java.util.function.Function;
import java.util.Map;
//import java.util.HashMap;
import java.util.Collection;
//import java.util.Arrays;

/**
 * Enum of leaderboards and functions to capture leaderboard data
 *
 * @author chloe, Brooke
 * @since 0.9.0
 */
public class Leaderboard {

    @Getter
    private final String name;

    @Getter
    private final LeaderboardCategory category;

    @Getter
    private final Function<JsonObject, Number> derive;

    @Getter
    private final int sortDirection;

    private static final Map<String, Leaderboard> LEADERBOARDS = LeaderboardRegistry.LEADERBOARDS;

    public Leaderboard(String name, LeaderboardCategory category, Function<JsonObject, Number> derive) {
        this(name, category, derive, -1); // Default to descending sort
    }

    public Leaderboard(String name, LeaderboardCategory category, Function<JsonObject, Number> derive, int sortDirection) {
        this.name = name;
        this.category = category;
        this.derive = derive;
        this.sortDirection = sortDirection;
        LEADERBOARDS.put(name, this);
    }

    public Number derive(JsonObject object) {
        try {
            return this.derive.apply(object);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Leaderboard get(String name) {
        return LEADERBOARDS.get(name);
    }

    public static Collection<Leaderboard> values() {
        return LEADERBOARDS.values();
    }
}