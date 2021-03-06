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

package io.github.amrjlg.stream.spliterator;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
@SuppressWarnings("unused")
public abstract class Spliterators {

    private Spliterators() {

    }


    @SuppressWarnings("unchecked")
    public static <T> Spliterator<T> emptySpliterator() {
        return (Spliterator<T>) EMPTY_SPLITERATOR;
    }

    public static Spliterator.OfByte emptyByteSpliterator() {
        return new EmptySpliterator.OfByte();
    }

    public static Spliterator.OfShort emptyShortSpliterator() {
        return new EmptySpliterator.OfShort();
    }

    public static Spliterator.OfChar emptyCharSpliterator() {
        return new EmptySpliterator.OfChar();
    }

    public static Spliterator.OfInt emptyIntSpliterator() {
        return new EmptySpliterator.OfInt();
    }

    public static Spliterator.OfLong emptyLongSpliterator() {
        return new EmptySpliterator.OfLong();
    }

    public static Spliterator.OfFloat emptyFloatSpliterator() {
        return new EmptySpliterator.OfFloat();
    }

    public static Spliterator.OfDouble emptyDoubleSpliterator() {
        return new EmptySpliterator.OfDouble();
    }

    public static <T> Spliterator<T> spliterator(T[] array, int additionalCharacteristics) {
        return new Spliterators.ArraySpliterator<>(Objects.requireNonNull(array), additionalCharacteristics);
    }

    public static <T> Spliterator<T> spliterator(T[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new Spliterators.ArraySpliterator<>(array, fromIndex, toIndex, additionalCharacteristics);
    }

    public static Spliterator.OfByte spliterator(byte[] array, int characteristics) {
        return new ByteArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfByte spliterator(byte[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new ByteArraySpliterator(array, fromIndex, toIndex, characteristics);
    }

    public static Spliterator.OfChar spliterator(char[] array, int characteristics) {
        return new CharArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfChar spliterator(char[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new CharArraySpliterator(array, fromIndex, toIndex, characteristics);
    }

    public static Spliterator.OfShort spliterator(short[] array, int characteristics) {
        return new ShortArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfShort spliterator(short[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new ShortArraySpliterator(array, fromIndex, toIndex, characteristics);
    }


    public static Spliterator.OfInt spliterator(int[] array, int characteristics) {
        return new IntArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new IntArraySpliterator(array, fromIndex, toIndex, characteristics);
    }

    public static Spliterator.OfLong spliterator(long[] array, int characteristics) {
        return new LongArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfLong spliterator(long[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new LongArraySpliterator(array, fromIndex, toIndex, characteristics);
    }

    public static Spliterator.OfFloat spliterator(float[] array, int characteristics) {
        return new FloatArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfFloat spliterator(float[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new FloatArraySpliterator(array, fromIndex, toIndex, characteristics);
    }

    public static Spliterator.OfDouble spliterator(double[] array, int characteristics) {
        return new DoubleArraySpliterator(array, characteristics);
    }

    public static Spliterator.OfDouble spliterator(double[] array, int fromIndex, int toIndex, int characteristics) {
        checkFromToBounds(array.length, fromIndex, toIndex);
        return new DoubleArraySpliterator(array, fromIndex, toIndex, characteristics);
    }


    private static void checkFromToBounds(int arrayLength, int begin, int end) {
        if (begin > end) {
            throw new ArrayIndexOutOfBoundsException(
                    "begin(" + begin + ") > end(" + end + ")");
        }
        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException(begin);
        }
        if (end > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(end);
        }
    }

    public static <T> Iterator<T> iterator(Spliterator<T> spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements Iterator<T>, Consumer<T> {
            boolean ready = false;
            T value;

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }

            @Override
            public T next() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public void accept(T t) {
                ready = true;
                value = t;
            }
        }

        return new Adapter();
    }

    public static PrimitiveIterator.OfByte iterator(Spliterator.OfByte spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfByte, ByteConsumer {
            boolean ready = false;
            byte value;
            private final Spliterator.OfByte spliterator;

            public Adapter(Spliterator.OfByte spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(byte value) {
                ready = true;
                this.value = value;
            }

            @Override
            public byte nextByte() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfShort iterator(Spliterator.OfShort spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfShort, ShortConsumer {
            boolean ready = false;
            short value;
            private final Spliterator.OfShort spliterator;

            public Adapter(Spliterator.OfShort spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(short value) {
                ready = true;
                this.value = value;
            }

            @Override
            public short nextShort() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfChar iterator(Spliterator.OfChar spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfChar, CharConsumer {
            boolean ready = false;
            char value;
            private final Spliterator.OfChar spliterator;

            public Adapter(Spliterator.OfChar spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(char value) {
                ready = true;
                this.value = value;
            }

            @Override
            public char nextChar() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfInt iterator(Spliterator.OfInt spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfInt, IntConsumer {
            boolean ready = false;
            int value;
            private final Spliterator.OfInt spliterator;

            public Adapter(Spliterator.OfInt spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(int value) {
                ready = true;
                this.value = value;
            }

            @Override
            public int nextInt() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfLong iterator(Spliterator.OfLong spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfLong, LongConsumer {
            boolean ready = false;
            long value;
            private final Spliterator.OfLong spliterator;

            public Adapter(Spliterator.OfLong spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(long value) {
                ready = true;
                this.value = value;
            }

            @Override
            public long nextLong() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfFloat iterator(Spliterator.OfFloat spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfFloat, FloatConsumer {
            boolean ready = false;
            float value;
            private final Spliterator.OfFloat spliterator;

            public Adapter(Spliterator.OfFloat spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(float value) {
                ready = true;
                this.value = value;
            }

            @Override
            public float nextFloat() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    public static PrimitiveIterator.OfDouble iterator(Spliterator.OfDouble spliterator) {
        Objects.requireNonNull(spliterator);

        class Adapter implements PrimitiveIterator.OfDouble, DoubleConsumer {
            boolean ready = false;
            double value;
            private final Spliterator.OfDouble spliterator;

            public Adapter(Spliterator.OfDouble spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public void accept(double value) {
                ready = true;
                this.value = value;
            }

            @Override
            public double nextDouble() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return value;
            }

            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }
        }
        return new Adapter(spliterator);
    }

    /**
     * base empty spliterator
     *
     * @param <T>
     * @param <S>
     * @param <C>
     */
    public static abstract class EmptySpliterator<T, S extends Spliterator<T>, C> {

        EmptySpliterator() {
        }

        public S trySplit() {
            return null;
        }

        public boolean tryAdvance(C consumer) {
            Objects.requireNonNull(consumer);
            return false;
        }

        public void forEachRemaining(C consumer) {
            Objects.requireNonNull(consumer);
        }

        public long estimateSize() {
            return 0;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        public static final class OfRef<T>
                extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>>
                implements Spliterator<T> {
            OfRef() {
            }
        }

        public static final class OfByte extends EmptySpliterator<Byte, Spliterator.OfByte, ByteConsumer>
                implements Spliterator.OfByte {
            public OfByte() {
            }
        }

        public static final class OfChar extends EmptySpliterator<Character, Spliterator.OfChar, CharConsumer>
                implements Spliterator.OfChar {
            public OfChar() {
            }
        }

        public static final class OfShort extends EmptySpliterator<Short, Spliterator.OfShort, ShortConsumer>
                implements Spliterator.OfShort {
            public OfShort() {
            }
        }


        public static final class OfInt
                extends EmptySpliterator<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            public OfInt() {
            }
        }

        public static final class OfLong
                extends EmptySpliterator<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            public OfLong() {
            }
        }

        public static final class OfFloat extends EmptySpliterator<Float, Spliterator.OfFloat, FloatConsumer>
                implements Spliterator.OfFloat {
            public OfFloat() {
            }
        }

        public static final class OfDouble
                extends EmptySpliterator<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            public OfDouble() {
            }
        }
    }

    private static final Spliterator<Object> EMPTY_SPLITERATOR = new EmptySpliterator.OfRef<>();

    public static class ArraySpliterator<T> implements Spliterator<T> {
        private final T[] array;
        // current index, modified on advance/split
        private int index;
        // one past last index
        private final int end;
        private final int characteristics;

        public ArraySpliterator(T[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ArraySpliterator(T[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator<T> trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new ArraySpliterator<>(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            T[] a;
            int i, hi; // hoist accesses and checks from loop
            if (consumer == null) {
                throw new NullPointerException();
            }
            if ((a = array).length >= (hi = end) &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    consumer.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                T e = array[index++];
                consumer.accept(e);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class ByteArraySpliterator implements Spliterator.OfByte {
        private final byte[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public ByteArraySpliterator(byte[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ByteArraySpliterator(byte[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfByte trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.ByteArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(ByteConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            byte[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(ByteConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Byte> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class CharArraySpliterator implements Spliterator.OfChar {
        private final char[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public CharArraySpliterator(char[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public CharArraySpliterator(char[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfChar trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.CharArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(CharConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            char[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(CharConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Character> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class ShortArraySpliterator implements Spliterator.OfShort {
        private final short[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public ShortArraySpliterator(short[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ShortArraySpliterator(short[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfShort trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.ShortArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(ShortConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            short[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(ShortConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Short> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class IntArraySpliterator implements Spliterator.OfInt {
        private final int[] array;
        /**
         * current index, modified on advance/split
         */
        private int index;
        /**
         * one past last index
         */
        private final int end;
        private final int characteristics;

        public IntArraySpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public IntArraySpliterator(int[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.IntArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class LongArraySpliterator implements Spliterator.OfLong {
        private final long[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public LongArraySpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public LongArraySpliterator(long[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.LongArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            long[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class FloatArraySpliterator implements Spliterator.OfFloat {
        private final float[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public FloatArraySpliterator(float[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public FloatArraySpliterator(float[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfFloat trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.FloatArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(FloatConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            float[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(FloatConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Float> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class DoubleArraySpliterator implements Spliterator.OfDouble {
        private final double[] array;
        private int index;        // current index, modified on advance/split
        private final int end;  // one past last index
        private final int characteristics;

        public DoubleArraySpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public DoubleArraySpliterator(double[] array, int origin, int end, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.end = end;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfDouble trySplit() {
            int lo = index, mid = (lo + end) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.DoubleArraySpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            double[] a = array;
            // hoist accesses and checks from loop
            int hi = end, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (index >= 0 && index < end) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return end - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    /**
     * object iterator spliterator
     *
     * @param <T>
     */
    public static class IteratorSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        private final Collection<? extends T> collection; // null OK
        private Iterator<? extends T> it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
            this.collection = collection;
            this.it = null;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public Spliterator<T> trySplit() {
            Iterator<? extends T> i;
            long s;
            if ((i = it) == null) {
                i = it = collection.iterator();
                s = est = (long) collection.size();
            } else
                s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s) {
                    n = (int) s;
                }
                if (n > MAX_BATCH) {
                    n = MAX_BATCH;
                }
                Object[] a = new Object[n];
                int j = 0;
                do {
                    a[j] = i.next();
                } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                }
                return new ArraySpliterator<>((T[]) a, 0, j, characteristics);
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            Iterator<? extends T> i;
            if ((i = it) == null) {
                i = it = collection.iterator();
                est = (long) collection.size();
            }
            i.forEachRemaining(consumer);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            if (it == null) {
                it = collection.iterator();
                est = (long) collection.size();
            }
            if (it.hasNext()) {
                consumer.accept(it.next());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            if (it == null) {
                it = collection.iterator();
                return est = (long) collection.size();
            }
            return est;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    /**
     * byte iterator spliterator
     */
    public static class ByteIteratorSpliterator implements Spliterator.OfByte {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfByte iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public ByteIteratorSpliterator(PrimitiveIterator.OfByte iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public ByteIteratorSpliterator(PrimitiveIterator.OfByte iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public OfByte trySplit() {
            PrimitiveIterator.OfByte iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > estimateSize) {
                    n = (int) estimateSize;
                }
                if (n > MAX_BATCH) {
                    n = MAX_BATCH;
                }
                byte[] bytes = new byte[n];
                int offset = 0;
                do {
                    bytes[offset++] = iterator.nextByte();
                } while (offset < n && iterator.hasNext());
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new ByteArraySpliterator(bytes, 0, offset, characteristics);
            }

            return null;
        }

        @Override
        public boolean tryAdvance(ByteConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextByte());
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(ByteConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public Comparator<? super Byte> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class CharIteratorSpliterator implements Spliterator.OfChar {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfChar iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public CharIteratorSpliterator(PrimitiveIterator.OfChar iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public CharIteratorSpliterator(PrimitiveIterator.OfChar iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Character> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(CharConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public OfChar trySplit() {
            PrimitiveIterator.OfChar iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + MAX_BATCH;
                split = (int) Math.min(split, estimateSize);
                split = (int) Math.min(split, MAX_BATCH);
                char[] chars = new char[split];
                int offset = 0;
                do {
                    chars[offset++] = iterator.nextChar();
                } while (offset < split && iterator.hasNext());
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new CharArraySpliterator(chars, 0, offset, characteristics);

            }

            return null;
        }

        @Override
        public boolean tryAdvance(CharConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextChar());
                return true;
            }
            return false;
        }
    }

    public static class ShortIteratorSpliterator implements Spliterator.OfShort {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfShort iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public ShortIteratorSpliterator(PrimitiveIterator.OfShort iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public ShortIteratorSpliterator(PrimitiveIterator.OfShort iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Short> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(ShortConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public OfShort trySplit() {
            PrimitiveIterator.OfShort iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + BATCH_UNIT;
                split = (int) Math.min(split, estimateSize);
                split = (int) Math.min(split, MAX_BATCH);
                short[] shorts = new short[split];
                int offset = 0;
                while (offset < split && iterator.hasNext()) {
                    shorts[offset++] = iterator.nextShort();
                }
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new ShortArraySpliterator(shorts, 0, offset, characteristics);

            }
            return null;
        }

        @Override
        public boolean tryAdvance(ShortConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextShort());
                return true;
            }
            return false;
        }
    }

    public static class IntIteratorSpliterator implements Spliterator.OfInt {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfInt iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public Spliterator.OfInt trySplit() {
            PrimitiveIterator.OfInt iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + BATCH_UNIT;
                split = (int) Math.min(estimateSize, split);
                split = Math.min(split, MAX_BATCH);

                int[] ints = new int[split];
                int offset = 0;
                while (offset < split && iterator.hasNext()) {
                    ints[offset++] = iterator.nextInt();
                }
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new IntArraySpliterator(ints, 0, offset, characteristics);
            }
            return null;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextInt());
                return true;
            }
            return false;
        }
    }

    public static class LongIteratorSpliterator implements Spliterator.OfLong {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfLong iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }

            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public Spliterator.OfLong trySplit() {

            PrimitiveIterator.OfLong iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + BATCH_UNIT;
                split = (int) Math.min(split, estimateSize);
                split = Math.min(split, MAX_BATCH);
                long[] longs = new long[split];
                int offset = 0;
                while (offset < split && iterator.hasNext()) {
                    longs[offset++] = iterator.nextLong();
                }
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }

                return new LongArraySpliterator(longs, 0, offset, characteristics);
            }
            return null;
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextLong());
                return true;
            }
            return false;
        }
    }

    public static class FloatIteratorSpliterator implements Spliterator.OfFloat {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfFloat iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public FloatIteratorSpliterator(PrimitiveIterator.OfFloat iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & java.util.Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public FloatIteratorSpliterator(PrimitiveIterator.OfFloat iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public void forEachRemaining(FloatConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public OfFloat trySplit() {
            PrimitiveIterator.OfFloat iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + BATCH_UNIT;
                split = (int) Math.min(split, estimateSize);
                split = Math.min(split, MAX_BATCH);
                float[] floats = new float[split];
                int offset = 0;
                while (offset < split && iterator.hasNext()) {
                    floats[offset++] = iterator.nextFloat();
                }

                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new FloatArraySpliterator(floats, 0, offset, characteristics);
            }
            return null;
        }

        @Override
        public boolean tryAdvance(FloatConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextFloat());
                return true;
            }
            return false;
        }

        @Override
        public Comparator<? super Float> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }
    }

    public static class DoubleIteratorSpliterator implements Spliterator.OfDouble {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private PrimitiveIterator.OfDouble iterator;
        private final int characteristics;
        private long estimateSize;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator        the iterator for the source
         * @param size            the number of elements in the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator        the iterator for the source
         * @param characteristics properties of this spliterator's
         *                        source or elements.
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, int characteristics) {
            this.iterator = iterator;
            this.estimateSize = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED)) {
                return null;
            }
            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            iterator.forEachRemaining(Objects.requireNonNull(action));
        }

        @Override
        public Spliterator.OfDouble trySplit() {
            PrimitiveIterator.OfDouble iterator = this.iterator;
            long estimateSize = this.estimateSize;
            if (estimateSize > 1 && iterator.hasNext()) {
                int split = batch + BATCH_UNIT;
                split = (int) Math.min(split, estimateSize);
                split = Math.min(split, MAX_BATCH);
                double[] doubles = new double[split];
                int offset = 0;
                while (offset < split && iterator.hasNext()) {
                    doubles[offset++] = iterator.nextDouble();
                }
                batch = offset;
                if (this.estimateSize != Long.MAX_VALUE) {
                    this.estimateSize -= offset;
                }
                return new DoubleArraySpliterator(doubles, 0, offset, characteristics);
            }

            return null;
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (iterator.hasNext()) {
                action.accept(iterator.nextDouble());
                return true;
            }
            return false;
        }
    }
}
