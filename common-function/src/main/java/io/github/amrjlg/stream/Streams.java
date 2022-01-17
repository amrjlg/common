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

package io.github.amrjlg.stream;

import io.github.amrjlg.stream.pipeline.BytePipeline;
import io.github.amrjlg.stream.pipeline.Pipelines;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;

/**
 * @author amrjlg
 **/
public class Streams {

    public static final int CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.ORDERED;

    public static Runnable composeWithExceptions(Runnable first, Runnable second) {
        return () -> {
            try {
                first.run();
            } catch (Throwable parent) {
                try {
                    second.run();
                } catch (Throwable child) {
                    try {
                        parent.addSuppressed(child);
                    } catch (Throwable ignore) {
                    }
                }
                throw parent;
            }
            second.run();
        };
    }

    public static <T> Stream<T> stream(T[] array) {
        return stream(array, 0, array.length);
    }

    public static <T> Stream<T> stream(T[] array, int start, int end) {
        return Pipelines.stream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }


    public static ByteStream stream(byte[] array) {
        return stream(array, 0, array.length);
    }

    public static ByteStream stream(byte[] array, int start, int end) {
        return Pipelines.byteStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static ShortStream stream(short[] array) {
        return stream(array, 0, array.length);
    }

    public static ShortStream stream(short[] array, int start, int end) {
        return Pipelines.shortStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static CharStream stream(char[] array) {
        return stream(array, 0, array.length);
    }

    public static CharStream stream(char[] array, int start, int end) {
        return Pipelines.charStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static IntStream stream(int[] array) {
        return stream(array, 0, array.length);
    }

    public static IntStream stream(int[] array, int start, int end) {
        return Pipelines.intStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static LongStream stream(long[] array) {
        return stream(array, 0, array.length);
    }

    public static LongStream stream(long[] array, int start, int end) {
        return Pipelines.longStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static FloatStream stream(float[] array) {
        return stream(array, 0, array.length);
    }

    public static FloatStream stream(float[] array, int start, int end) {
        return Pipelines.floatStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }

    public static DoubleStream stream(double[] array) {
        return stream(array, 0, array.length);
    }

    public static DoubleStream stream(double[] array, int start, int end) {
        return Pipelines.doubleStream(Spliterators.spliterator(array, start, end, CHARACTERISTICS), false);
    }


}
