/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.utils;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ByteToDoubleFunction;
import io.github.amrjlg.function.ByteToIntFunction;
import io.github.amrjlg.function.ByteToLongFunction;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.CharToDoubleFunction;
import io.github.amrjlg.function.CharToIntFunction;
import io.github.amrjlg.function.CharToLongFunction;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.function.ShortToDoubleFunction;
import io.github.amrjlg.function.ShortToIntFunction;
import io.github.amrjlg.function.ShortToLongFunction;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * @author amrjlg
 */
public class ArrayUtil {
    public static boolean[] array(boolean... elements) {
        if (elements == null) {
            return new boolean[0];
        }
        return elements;
    }

    public static byte[] array(byte... elements) {
        if (elements == null) {
            return new byte[0];
        }
        return elements;
    }

    public static char[] array(char... elements) {
        if (elements == null) {
            return new char[0];
        }
        return elements;
    }

    public static short[] array(short... elements) {
        if (elements == null) {
            return new short[0];
        }
        return elements;
    }


    public static int[] array(int... elements) {
        if (elements == null) {
            return new int[0];
        }
        return elements;
    }

    public static long[] array(long... elements) {
        if (elements == null) {
            return new long[0];
        }
        return elements;
    }

    public static float[] array(float... elements) {
        if (elements == null) {
            return new float[0];
        }
        return elements;
    }

    public static double[] array(double... elements) {
        if (elements == null) {
            return new double[0];
        }
        return elements;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] arrays(T... elements) {
        if (elements == null) {
            return (T[]) new Object[0];
        }
        return elements;
    }

    public static void swap(byte[] array, int left, int right) {
        byte temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(char[] array, int left, int right) {
        char temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(short[] array, int left, int right) {
        short temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(int[] array, int left, int right) {
        int temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(long[] array, int left, int right) {
        long temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(float[] array, int left, int right) {
        float temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void swap(double[] array, int left, int right) {
        double temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static <T> void swap(T[] array, int left, int right) {
        T temp = array[left];
        array[left] = array[right];
        array[right] = temp;
    }

    public static void reserved(byte[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(char[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(short[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(int[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(long[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(float[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static void reserved(double[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static <T> void reserved(T[] array) {
        if (array.length == 1 || array.length == 0) {
            return;
        }
        for (int i = 0; i < array.length / 2; i++) {
            swap(array, i, array.length - i);
        }

    }

    public static <T> void consumer(byte[] bytes, ByteConsumer consumer) {
        if (!empty(bytes)) {
            for (byte b : bytes) {
                consumer.accept(b);
            }
        }
    }

    public static <T> void consumer(char[] bytes, CharConsumer consumer) {
        if (!empty(bytes)) {
            for (char b : bytes) {
                consumer.accept(b);
            }
        }
    }

    public static <T> void consumer(short[] bytes, ShortConsumer consumer) {
        if (!empty(bytes)) {
            for (short b : bytes) {
                consumer.accept(b);
            }
        }
    }

    public static <T> void consumer(int[] bytes, IntConsumer consumer) {
        if (!empty(bytes)) {
            for (int b : bytes) {
                consumer.accept(b);
            }
        }
    }

    public static <T> void consumer(long[] bytes, LongConsumer consumer) {
        if (!empty(bytes)) {
            for (long b : bytes) {
                consumer.accept(b);
            }
        }
    }

    public static <T> void consumer(T[] ts, Consumer<T> consumer) {
        if (!empty(ts))
            Arrays.stream(ts).forEach(consumer);
    }

    public static int length(boolean[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(byte[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(short[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(char[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(int[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(long[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(float[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(double[] array) {
        return array == null ? 0 : array.length;
    }

    public static <T> int length(T[] array) {
        return array == null ? 0 : array.length;
    }

    public static boolean empty(boolean[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(short[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(double[] array) {
        return array == null || array.length == 0;
    }

    public static boolean empty(long[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean empty(T[] array) {
        return array == null || array.length == 0;
    }

    public static <T> int[] mapToInt(T[] source, ToIntFunction<T> function) {
        int length = length(source);
        int[] ints = new int[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsInt(source[i]);
        }
        return ints;
    }

    public static int[] mapToInt(byte[] source, ByteToIntFunction function) {
        int length = length(source);
        int[] ints = new int[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsInt(source[i]);
        }
        return ints;
    }

    public static int[] mapToInt(short[] source, ShortToIntFunction function) {
        int length = length(source);
        int[] ints = new int[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsInt(source[i]);
        }
        return ints;
    }

    public static int[] mapToInt(char[] source, CharToIntFunction function) {
        int length = length(source);
        int[] ints = new int[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsInt(source[i]);
        }
        return ints;
    }


    public static <T> long[] mapToLong(T[] source, ToLongFunction<T> function) {
        int length = length(source);
        long[] ints = new long[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsLong(source[i]);
        }
        return ints;
    }

    public static long[] mapToLong(byte[] source, ByteToLongFunction function) {
        int length = length(source);
        long[] ints = new long[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsLong(source[i]);
        }
        return ints;
    }

    public static long[] mapToLong(short[] source, ShortToLongFunction function) {
        int length = length(source);
        long[] ints = new long[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsLong(source[i]);
        }
        return ints;
    }

    public static long[] mapToLong(char[] source, CharToLongFunction function) {
        int length = length(source);
        long[] ints = new long[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsLong(source[i]);
        }
        return ints;
    }


    public static <T> double[] mapToDouble(T[] source, ToDoubleFunction<T> function) {
        int length = length(source);
        double[] ints = new double[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsDouble(source[i]);
        }
        return ints;
    }

    public static double[] mapToDouble(byte[] source, ByteToDoubleFunction function) {
        int length = length(source);
        double[] ints = new double[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsDouble(source[i]);
        }
        return ints;
    }

    public static double[] mapToDouble(short[] source, ShortToDoubleFunction function) {
        int length = length(source);
        double[] ints = new double[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsDouble(source[i]);
        }
        return ints;
    }

    public static double[] mapToDouble(char[] source, CharToDoubleFunction function) {
        int length = length(source);
        double[] ints = new double[length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = function.applyAsDouble(source[i]);
        }
        return ints;
    }


    /**
     * ???????????????
     *
     * @param ts        ?????????
     * @param transform ?????????
     * @param rs        ????????????
     * @param <T>       ?????????
     * @param <R>       ????????????
     * @return ??????????????????
     */
    public static <T, R> R[] map(T[] ts, Function<T, R> transform, R[] rs) {
        if (ts == null || ts.length == 0) {
            return rs;
        }
        int length = Math.min(rs.length, ts.length);

        map(ts, 0, rs, 0, length, transform);

        return rs;
    }

    public static <T, R> R[] map(Supplier<T[]> ts, Function<T, R> transform, Supplier<R[]> rs) {
        R[] r = rs.get();
        if (ts == null) {
            return r;
        }
        return map(ts.get(), transform, rs);
    }

    public static <T, R> R[] map(T[] ts, Function<T, R> transform, Supplier<R[]> supplier) {
        R[] rs = supplier.get();
        if (rs == null) {
            throw new IllegalArgumentException("????????????????????????");
        }
        return map(ts, transform, rs);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param src       ?????????
     * @param start     ???????????? 0??????
     * @param des       ????????????
     * @param destStart ???????????? 0??????
     * @param length    ?????????
     * @param transform ?????????
     * @param <T>       ?????????
     * @param <R>       ????????????
     */
    public static <T, R> void map(T[] src, int start, R[] des, int destStart, int length, Function<T, R> transform) {
        if (src == null || src.length == 0 || des == null || des.length == 0 || length < 1) {
            return;
        }
        rangeCheck(src, start, start + length);
        rangeCheck(des, destStart, destStart + length);
        for (int i = start; i < start + length; i++) {
            des[i] = transform.apply(src[i]);
        }

    }

    public static <T> void rangeCheck(T[] array, int start, int end) {
        if (Objects.isNull(array)) {
            throw new NullPointerException("array is null");
        }
        rangeCheck(array.length, start, end);
    }

    public static void rangeCheck(int length, int start, int end) {
        if (start < 0 || end > length || start >= length || start > end) {
            throw new IndexOutOfBoundsException(String.format("length %d, start %d, end %d", length, start, end));
        }
    }
}
