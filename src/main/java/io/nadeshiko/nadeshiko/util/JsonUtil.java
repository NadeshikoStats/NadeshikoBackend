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

package io.nadeshiko.nadeshiko.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {

    public int getNullableInt(JsonElement object) {
        return object != null ? object.getAsInt() : 0;
    }

    public double getNullableDouble(JsonElement object) {
        return object != null ? object.getAsDouble() : 0;
    }

    public String getNullableString(JsonElement object) {
        return object != null ? object.getAsString() : "null";
    }

    public boolean getNullableBoolean(JsonElement object) {
        return object != null && object.getAsBoolean();
    }
}
