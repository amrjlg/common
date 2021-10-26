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

package io.github.amrjlg.stream.spliterator;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;


import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public interface Spliterator<T>  {
    /**
     * <pre>
     * define some flag for characteristics
     * 00000000 00000001 -&gt; 0x00000001 DISTINCT
     * 00000000 00000100 -&gt; 0x00000004 SORTED
     * 00000000 00010000 -&gt; 0x00000010 ORDERED
     * 00000000 01000000 -&gt; 0x00000040 SIZED
     * 00000001 00000000 -&gt; 0x00000100 NONNULL
     * 00000100 00000000 -&gt; 0x00000400 IMMUTABLE
     * 00010000 00000000 -&gt; 0x00001000 CONCURRENT
     * 01000000 00000000 -&gt; 0x00004000 SUBSIZED
     * </pre>
     */
    int DISTINCT = 0x00000001;

    int SORTED = 0x00000004;

    int ORDERED = 0x00000010;

    int SIZED = 0x00000040;

    int NONNULL = 0x00000100;

    int IMMUTABLE = 0x00000400;

    int CONCURRENT = 0x00001000;

    int SUBSIZED = 0x00004000;


    boolean tryAdvance(Consumer<? super T> consumer);

    default void forEachRemaining(Consumer<? super T> consumer) {
        do {
        } while (tryAdvance(consumer));
    }

    Spliterator<T> trySplit();

    long estimateSize();

    default long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
    }


    int characteristics();

    default boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }

    default Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }


    interface OfPrimitive<T, PrimitiveConsumer, PrimitiveSpliterator extends OfPrimitive<T, PrimitiveConsumer, PrimitiveSpliterator>>
            extends Spliterator<T> {

        @Override
        PrimitiveSpliterator trySplit();

        boolean tryAdvance(PrimitiveConsumer action);

        default void forEachRemaining(PrimitiveConsumer action) {
            do {
            } while (tryAdvance(action));
        }
    }

    interface OfByte extends OfPrimitive<Byte, ByteConsumer, OfByte> {
        @Override
        OfByte trySplit();

        @Override
        boolean tryAdvance(ByteConsumer action);

        @Override
        default boolean tryAdvance(Consumer<? super Byte> action) {
            ByteConsumer consumer = action instanceof ByteConsumer ? (ByteConsumer) action : action::accept;
            return tryAdvance(consumer);
        }


        @Override
        default void forEachRemaining(Consumer<? super Byte> action) {
            ByteConsumer consumer = action instanceof ByteConsumer ? (ByteConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfChar extends OfPrimitive<Character, CharConsumer, OfChar> {
        @Override
        OfChar trySplit();

        @Override
        boolean tryAdvance(CharConsumer action);


        @Override
        default boolean tryAdvance(Consumer<? super Character> action) {
            CharConsumer consumer = action instanceof CharConsumer ? (CharConsumer) action : action::accept;
            return tryAdvance(consumer);
        }

        @Override
        default void forEachRemaining(Consumer<? super Character> action) {
            CharConsumer consumer = action instanceof CharConsumer ? (CharConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfShort extends OfPrimitive<Short, ShortConsumer, OfShort> {
        @Override
        OfShort trySplit();

        @Override
        boolean tryAdvance(ShortConsumer action);


        @Override
        default boolean tryAdvance(Consumer<? super Short> action) {
            ShortConsumer consumer = action instanceof ShortConsumer ? (ShortConsumer) action : action::accept;
            return tryAdvance(consumer);
        }

        @Override
        default void forEachRemaining(Consumer<? super Short> action) {
            ShortConsumer consumer = action instanceof ShortConsumer ? (ShortConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfInt extends OfPrimitive<Integer, IntConsumer, OfInt> {
        @Override
        Spliterator.OfInt trySplit();

        @Override
        boolean tryAdvance(IntConsumer action);

        @Override
        default boolean tryAdvance(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                return tryAdvance((IntConsumer) consumer);
            } else {
                return tryAdvance((IntConsumer) consumer::accept);
            }
        }

        @Override
        default void forEachRemaining(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEachRemaining((IntConsumer) consumer);
            } else {
                forEachRemaining((IntConsumer) consumer::accept);
            }
        }
    }

    interface OfLong extends OfPrimitive<Long, LongConsumer, OfLong> {
        @Override
        Spliterator.OfLong trySplit();

        @Override
        boolean tryAdvance(LongConsumer action);

        @Override
        default boolean tryAdvance(Consumer<? super Long> action) {
            LongConsumer consumer = action instanceof LongConsumer ? (LongConsumer) action : action::accept;
            return tryAdvance(consumer);
        }

        @Override
        default void forEachRemaining(Consumer<? super Long> action) {
            LongConsumer consumer = action instanceof LongConsumer ? (LongConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfFloat extends OfPrimitive<Float, FloatConsumer, OfFloat> {
        @Override
        OfFloat trySplit();

        @Override
        boolean tryAdvance(FloatConsumer action);

        @Override
        default boolean tryAdvance(Consumer<? super Float> action) {
            FloatConsumer consumer = action instanceof FloatConsumer ? (FloatConsumer) action : action::accept;
            return tryAdvance(consumer);
        }

        @Override
        default void forEachRemaining(Consumer<? super Float> action) {
            FloatConsumer consumer = action instanceof FloatConsumer ? (FloatConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {
        @Override
        Spliterator.OfDouble trySplit();

        @Override
        boolean tryAdvance(DoubleConsumer action);

        @Override
        default boolean tryAdvance(Consumer<? super Double> action) {
            DoubleConsumer consumer = action instanceof DoubleConsumer ? (DoubleConsumer) action : action::accept;
            return tryAdvance(consumer);
        }

        @Override
        default void forEachRemaining(Consumer<? super Double> action) {
            DoubleConsumer consumer = action instanceof DoubleConsumer ? (DoubleConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }


}
