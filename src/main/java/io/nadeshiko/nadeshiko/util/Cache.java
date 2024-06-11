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

import java.util.HashMap;

/**
 * An abstract cache
 * @param <K> The type of keys used to cache objects
 * @param <V> The type of objects being cached
 * @author chloe
 */
public abstract class Cache<K, V> {

	/**
	 * The cache itself, using type {@code K} as keys and type {@code V} as values
	 */
	protected final HashMap<K, V> cache = new HashMap<>();
}
