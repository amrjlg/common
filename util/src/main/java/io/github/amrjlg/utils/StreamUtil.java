/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.amrjlg.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author amrjlg
 **/
public class StreamUtil {
    /**
     * stream to map ,the value like {@link HashMap#put(K, U)}
     *
     * @param key   function to get key
     * @param value function to get value
     * @param <T>   type of stream object
     * @param <K>   key type
     * @param <U>   value type
     * @return map collector
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMapKeepFirst(Function<? super T, ? extends K> key, Function<? super T, ? extends U> value) {
        BiFunction<U, U, U> mergeFunction = last();
        BiConsumer<Map<K, U>, T> consumer = (map, element) -> map.merge(key.apply(element), value.apply(element), mergeFunction);
        Supplier<Map<K, U>> supplier = HashMap::new;
        BinaryOperator<Map<K, U>> operator = (m, n) -> {
            n.forEach((k, v) -> m.merge(k, v, mergeFunction));
            return m;
        };

        return Collector.of(supplier, consumer, operator, Collector.Characteristics.IDENTITY_FINISH);
    }

    /**
     * stream to map ,the value like {@link HashMap#put(K, U)}
     *
     * @param key   function to get key
     * @param value function to get value
     * @param <T>   type of stream object
     * @param <K>   key type
     * @param <U>   value type
     * @return map collector
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMapKeepLast(Function<? super T, ? extends K> key, Function<? super T, ? extends U> value) {
        BiFunction<U, U, U> mergeFunction = first();
        BiConsumer<Map<K, U>, T> consumer = (map, element) -> map.merge(key.apply(element), value.apply(element), mergeFunction);
        Supplier<Map<K, U>> supplier = HashMap::new;
        BinaryOperator<Map<K, U>> operator = (m, n) -> {
            n.forEach((k, v) -> m.merge(k, v, mergeFunction));
            return m;
        };

        return Collector.of(supplier, consumer, operator, Collector.Characteristics.IDENTITY_FINISH);
    }

    /**
     * stream to map ,the value like {@link HashMap#put(K, U)}
     *
     * @param key   function to get key
     * @param value function to get value
     * @param first merge type, if have same key, <code>true<code/> will keep old value,else will use new value
     * @see #toMapKeepFirst(Function, Function)
     * @see #toMapKeepLast(Function, Function)
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> key, Function<? super T, ? extends U> value, boolean first) {
        return first ? toMapKeepFirst(key, value) : toMapKeepLast(key, value);
    }

    /**
     * stream to map ,the value like {@link HashMap#put(K, U)}
     *
     * @param key   function to get key
     * @param value function to get value
     * @param <T>   type of stream object
     * @param <K>   key type
     * @param <U>   value type
     * @return map collector
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> key, Function<? super T, ? extends U> value) {
        return toMapKeepLast(key, value);
    }


    public static <T> BinaryOperator<T> last() {
        return (f, l) -> l;
    }

    public static <T> BinaryOperator<T> first() {
        return (f, l) -> f;
    }
}
