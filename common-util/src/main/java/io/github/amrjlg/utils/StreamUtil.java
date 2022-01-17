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
 * limitations under the License.
 *
 */

package io.github.amrjlg.utils;

import io.github.amrjlg.function.ByteToDoubleFunction;
import io.github.amrjlg.function.ByteToIntFunction;
import io.github.amrjlg.function.ByteToLongFunction;
import io.github.amrjlg.function.CharToDoubleFunction;
import io.github.amrjlg.function.CharToIntFunction;
import io.github.amrjlg.function.CharToLongFunction;
import io.github.amrjlg.function.ShortToDoubleFunction;
import io.github.amrjlg.function.ShortToIntFunction;
import io.github.amrjlg.function.ShortToLongFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author amrjlg
 **/
public class StreamUtil {
    /**
     * stream to map ,the value like {@link HashMap#put}
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
     * stream to map ,the value like {@link HashMap#put}
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
     * stream to map ,the value like {@link HashMap#put}
     *
     * @param key   function to get key
     * @param value function to get value
     * @param first merge type, if have same key, {@code true} will keep old value,else will use new value
     * @see #toMapKeepFirst(Function, Function)
     * @see #toMapKeepLast(Function, Function)
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> key, Function<? super T, ? extends U> value, boolean first) {
        return first ? toMapKeepFirst(key, value) : toMapKeepLast(key, value);
    }

    /**
     * stream to map ,the value like {@link HashMap#put}
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

    /**
     * int stream
     *
     * @param ts       type
     * @param function map to int function
     * @param <T>      type of source
     * @return IntStream
     */
    public static <T> IntStream stream(T[] ts, ToIntFunction<T> function) {
        return Arrays.stream(ArrayUtil.mapToInt(ts, function));
    }

    public static IntStream stream(byte[] bytes) {
        return stream(bytes, b -> (int) b);
    }

    public static IntStream stream(byte[] bytes, ByteToIntFunction function) {
        return Arrays.stream(ArrayUtil.mapToInt(bytes, function));
    }

    public static IntStream stream(char[] bytes) {
        return stream(bytes, b -> (int) b);
    }

    public static IntStream stream(char[] bytes, CharToIntFunction function) {
        return Arrays.stream(ArrayUtil.mapToInt(bytes, function));
    }

    public static IntStream stream(short[] bytes) {
        return stream(bytes, b -> (int) b);
    }

    public static IntStream stream(short[] bytes, ShortToIntFunction function) {
        return Arrays.stream(ArrayUtil.mapToInt(bytes, function));
    }

    public static <T> LongStream longStream(T[] ts, ToLongFunction<T> function) {
        return Arrays.stream(ArrayUtil.mapToLong(ts, function));
    }

    public static LongStream longStream(byte[] bytes) {
        return longStream(bytes, b -> (long) b);
    }

    public static LongStream longStream(byte[] bytes, ByteToLongFunction function) {
        return Arrays.stream(ArrayUtil.mapToLong(bytes, function));
    }

    public static LongStream longStream(char[] chars) {
        return longStream(chars, c -> (long) c);
    }

    public static LongStream longStream(char[] chars, CharToLongFunction function) {
        return Arrays.stream(ArrayUtil.mapToLong(chars, function));
    }

    public static LongStream longStream(short[] shorts) {
        return longStream(shorts, s -> (long) s);
    }

    public static LongStream longStream(short[] shorts, ShortToLongFunction function) {
        return Arrays.stream(ArrayUtil.mapToLong(shorts, function));
    }


    public static <T> DoubleStream doubleStream(T[] ts, ToDoubleFunction<T> function) {
        return Arrays.stream(ArrayUtil.mapToDouble(ts, function));
    }

    public static DoubleStream doubleStream(byte[] bytes) {
        return doubleStream(bytes, b -> (double) b);
    }

    public static DoubleStream doubleStream(byte[] bytes, ByteToDoubleFunction function) {
        return Arrays.stream(ArrayUtil.mapToDouble(bytes, function));
    }

    public static DoubleStream doubleStream(char[] chars) {
        return doubleStream(chars, c -> (double) c);
    }

    public static DoubleStream doubleStream(char[] bytes, CharToDoubleFunction function) {
        return Arrays.stream(ArrayUtil.mapToDouble(bytes, function));
    }

    public static DoubleStream doubleStream(short[] shorts) {
        return doubleStream(shorts, c -> (double) c);
    }

    public static DoubleStream doubleStream(short[] bytes, ShortToDoubleFunction function) {
        return Arrays.stream(ArrayUtil.mapToDouble(bytes, function));
    }
}
