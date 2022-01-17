/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.utils;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

/**
 * @author amrjlg
 **/
class ArrayUtilTest {


    @Test
    void array() {
        boolean[] booleans = ArrayUtil.array(true, false);
        byte[] bytes = ArrayUtil.array((byte) 0x1, (byte) 0x2);
        char[] chars = ArrayUtil.array('1', '2');
        short[] shorts = ArrayUtil.array((short) 1, (short) 2);
        int[] ints = ArrayUtil.array(1, 2);
        float[] floats = ArrayUtil.array(1.0F, 2.0F);
        double[] doubles = ArrayUtil.array(1.0, 2.0, 3);
        long[] longs = ArrayUtil.array(1L, 2L, 3, 4);
    }

    @Test
    void arrays() {
        Integer[] integers = ArrayUtil.arrays(1, 2, 3, 4);
        String[] strings = ArrayUtil.arrays("1");
        Character[] arrays = ArrayUtil.arrays('1');
        Boolean[] booleans = ArrayUtil.arrays(true);
    }

    @Test
    void consumer() {
        ArrayUtil.consumer(ArrayUtil.arrays("arrays"), System.out::println);
    }

    @Test
    void empty() {
        assert !ArrayUtil.empty(ArrayUtil.array(1));
        assert ArrayUtil.empty(ArrayUtil.arrays());
    }


    @Test
    void map() {
        String[] src = new String[]{"1", "2", "3", "4", "5"};
        int[] expect = new int[]{1, 2, 3, 4, 5};
        Function<String, Integer> mapFunction = Integer::valueOf;
        Integer[] integers = new Integer[5];
        ArrayUtil.map(src, mapFunction, integers);
        int mapLength = Math.min(integers.length, src.length);
        for (int i = 0; i < mapLength; i++) {
            assert expect[i] == integers[i];
            System.out.println(integers[i]);
        }

        integers = new Integer[4];
        ArrayUtil.map(src, mapFunction, integers);

        mapLength = Math.min(integers.length, src.length);
        for (int i = 0; i < mapLength; i++) {
            assert expect[i] == integers[i];
            System.out.println(integers[i]);
        }

        integers = new Integer[3];
        ArrayUtil.map(src, mapFunction, integers);
        mapLength = Math.min(integers.length, src.length);
        for (int i = 0; i < mapLength; i++) {
            assert expect[i] == integers[i];
            System.out.println(integers[i]);
        }
        integers = new Integer[6];
        ArrayUtil.map(src, mapFunction, integers);
        mapLength = Math.min(integers.length, src.length);
        for (int i = 0; i < mapLength; i++) {
            assert expect[i] == integers[i];
            System.out.println(integers[i]);
        }
    }

    @Test
    void rangeCheck(){
        ArrayUtil.rangeCheck(new int[]{1}.length,0,1);
    }
}