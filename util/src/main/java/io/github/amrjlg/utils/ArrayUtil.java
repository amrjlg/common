/*
 *  Copyright (c) 2021-2021 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.amrjlg.utils;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author amrjlg
 */
public class ArrayUtil {
    public static boolean[] array(boolean... booleans) {
        if (booleans == null) {
            return new boolean[0];
        }
        return booleans;
    }

    public static byte[] array(byte... bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        return bytes;
    }

    public static char[] array(char... chars) {
        if (chars == null) {
            return new char[0];
        }
        return chars;
    }

    public static short[] array(short... shorts) {
        if (shorts == null) {
            return new short[0];
        }
        return shorts;
    }


    public static int[] array(int... ints) {
        if (ints == null) {
            return new int[0];
        }
        return ints;
    }

    public static long[] array(long... longs) {
        if (longs == null) {
            return new long[0];
        }
        return longs;
    }

    public static float[] array(float... floats) {
        if (floats == null) {
            return new float[0];
        }
        return floats;
    }

    public static double[] array(double... doubles) {
        if (doubles == null) {
            return new double[0];
        }
        return doubles;
    }

    public static <T> T[] arrays(T... ts) {
        if (ts == null) {
            return (T[]) new Object[0];
        }
        return ts;
    }

    public static <T> void consumer(T[] ts, Consumer<T> consumer) {
        Arrays.stream(ts).forEach(consumer);
    }

    public static boolean empty(boolean[] booleans) {
        return booleans == null || booleans.length == 0;
    }

    public static boolean empty(byte[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(char[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(short[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(int[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(float[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(double[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static boolean empty(long[] numbers) {
        return numbers == null || numbers.length == 0;
    }

    public static <T> boolean emptyArray(T[] ts) {
        return ts == null || ts.length == 0;
    }

    /**
     * 将数组通过
     *
     * @param ts        原数组
     * @param transform 转换器
     * @param rs        目标数组
     * @param <T>       原类型
     * @param <R>       目标类型
     * @return 目标类型数组
     */
    public static <T, R> R[] map(T[] ts, Function<T, R> transform, R[] rs) {
        if (ts == null || ts.length == 0) {
            return rs;
        }
        int length = Math.min(rs.length, ts.length);

        map(ts, 0, rs, 0, length, transform);

        return rs;
    }

    public static <T, R> R[] map(T[] ts, Function<T, R> transform, Supplier<R[]> supplier) {
        R[] rs = supplier.get();
        if (rs == null) {
            throw new IllegalArgumentException("目标数组不能为空");
        }
        return map(ts, transform, rs);
    }

    /**
     * 将源数组指定索引开始指定长度的元素转换过后拷贝到目标数组指定索引
     *
     * @param src       源数组
     * @param start     起始索引 0开始
     * @param desc      目标数组
     * @param destStart 目标索引 0开始
     * @param length    总长度
     * @param transform 转换器
     * @param <T>       源类型
     * @param <R>       目标类型
     */
    public static <T, R> void map(T[] src, int start, R[] desc, int destStart, int length, Function<T, R> transform) {
        if (src == null) {
            return;
        }
        int srcTotal = src.length;
        if (start >= srcTotal) {
            throw new IndexOutOfBoundsException(String.format("index start:{%d} out of range,should be {0,%d}", start, srcTotal - 1));
        }
        if (srcTotal < start + length) {
            throw new IllegalArgumentException(String.format("src array not enough only %d form %d and total is %d", srcTotal - start, start, srcTotal));
        }
        int desTotal = desc.length;
        if (desTotal <= destStart) {
            throw new IndexOutOfBoundsException(String.format("index start:{%d} out of range,should be {0,%d}", destStart, desTotal - 1));
        }
        if (desTotal < start + length) {
            throw new IllegalArgumentException(String.format("src array not enough only %d form %d and total is %d", desTotal - destStart, destStart, desTotal));
        }
        for (int i = start; i < start + length; i++) {
            desc[i] = transform.apply(src[i]);
        }

    }
}
