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

package io.github.amrjlg.stream.iterator;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class DelegatingSpliterator<T, Spl extends Spliterator<T>>
        implements Spliterator<T> {

    private final Supplier<? extends Spl> supplier;

    private Spl spl;

    public DelegatingSpliterator(Supplier<? extends Spl> supplier) {
        this.supplier = supplier;
    }

    Spl get() {
        if (spl == null) {
            spl = supplier.get();
        }
        return spl;
    }

    @Override
    public Spl trySplit() {
        return (Spl) get().trySplit();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> consumer) {
        return get().tryAdvance(consumer);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> consumer) {
        get().forEachRemaining(consumer);
    }

    @Override
    public long estimateSize() {
        return get().estimateSize();
    }

    @Override
    public int characteristics() {
        return get().characteristics();
    }

    @Override
    public long getExactSizeIfKnown() {
        return get().getExactSizeIfKnown();
    }

    @Override
    public Comparator<? super T> getComparator() {
        return get().getComparator();
    }

    public static abstract class OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator extends Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator>>
            extends DelegatingSpliterator<Primitive, PrimitiveSpliterator>
            implements Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator> {

        public OfPrimitive(Supplier<? extends PrimitiveSpliterator> supplier) {
            super(supplier);
        }

        @Override
        public boolean tryAdvance(PrimitiveConsumer action) {
            return get().tryAdvance(action);
        }

        @Override
        public void forEachRemaining(PrimitiveConsumer action) {
            get().forEachRemaining(action);
        }
    }


    public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, Spliterator.OfByte>
            implements Spliterator.OfByte {

        public OfByte(Supplier<? extends Spliterator.OfByte> supplier) {
            super(supplier);
        }
    }

    public static final class OfShort extends OfPrimitive<Short, ShortConsumer, Spliterator.OfShort>
            implements Spliterator.OfShort {

        public OfShort(Supplier<? extends Spliterator.OfShort> supplier) {
            super(supplier);
        }
    }

    public static final class OfChar extends OfPrimitive<Character, CharConsumer, Spliterator.OfChar>
            implements Spliterator.OfChar {

        public OfChar(Supplier<? extends Spliterator.OfChar> supplier) {
            super(supplier);
        }
    }

    public static final class OfInt extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
            implements Spliterator.OfInt {

        public OfInt(Supplier<? extends Spliterator.OfInt> supplier) {
            super(supplier);
        }
    }

    public static final class OfLong extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
            implements Spliterator.OfLong {

        public OfLong(Supplier<? extends Spliterator.OfLong> supplier) {
            super(supplier);
        }
    }

    public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, Spliterator.OfFloat>
            implements Spliterator.OfFloat {

        public OfFloat(Supplier<? extends Spliterator.OfFloat> supplier) {
            super(supplier);
        }
    }

    public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
            implements Spliterator.OfDouble {

        public OfDouble(Supplier<? extends Spliterator.OfDouble> supplier) {
            super(supplier);
        }
    }
}
