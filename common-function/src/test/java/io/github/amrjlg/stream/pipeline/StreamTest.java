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

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.stream.Streams;
import io.github.amrjlg.utils.ArrayUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


/**
 * @author amrjlg
 **/
public class StreamTest {


    @Test
    public void stream() {

        String[] array = ArrayUtil.arrays("af", "bf", "cf", "df", "ef", "f");

        Streams.stream(array).parallel().skip(1)
                .filter(v -> v.contains("f")).limit(3).forEach(System.out::println);

    }


    @Test
    public void byteStream() {
        byte[] array = {5, 6, 2, 3, 4};

        Streams.stream(array)
                .parallel()
                .skip(1)
                .filter(v -> v > 0x2)
                .limit(2)
                .forEach(System.out::println);
    }

    @Test
    public void charStream() {
        char[] array = ArrayUtil.array('c', 'a', 'f', 'b', 'd', 'e');

        Streams.stream(array)
                .parallel()
                .skip(1)
                .filter(v -> v > 'c')
                .limit(1)
                .findFirst()
                .ifPresent(System.out::println);
    }

    @Test
    public void shortStream() {
        short[] array = new short[]{3, 5, 4, 1, 2, 6, 8, 7, 9};
        Streams.stream(array).parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(10)
                .findAny()
                .ifPresent(System.out::println);
    }

    @Test
    public void intStream() {
        int[] array = ArrayUtil.array(3, 5, 4, 1, 2, 6, 8, 7, 9);

        Arrays.stream(array).parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(1)
                .forEach(System.out::println);
        System.out.println("===========");
        Streams.stream(array).parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(1)
                .forEach(System.out::println);
    }

    @Test
    public void longStream() {
        long[] array = new long[]{3, 5, 4, 1, 2, 6, 8, 7, 9, 9};
        Streams.stream(array)
                .parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(2)
                .findAny()
                .ifPresent(System.out::println);
    }

    @Test
    public void floatStream() {
        float[] array = new float[]{3, 5, 4, 1, 2, 6, 8, 7, 9, 9};
        Streams.stream(array)
                .parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(0)
                .forEach(System.out::println);
    }

    @Test
    public void doubleStream() {
        double[] array = new double[]{3, 5, 4, 1, 2, 6, 8, 7, 9, 9};
        Streams.stream(array)
                .parallel()
                .skip(1)
                .filter(v -> v > 1)
                .limit(2)
                .findAny()
                .ifPresent(System.out::println);
    }
}


