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

package io.github.amrjlg.stream;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public abstract class Spliterators {

    private Spliterators() {

    }

    public static class ByteSpliterator implements Spliterator.OfByte {
        private final byte[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public ByteSpliterator(byte[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ByteSpliterator(byte[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfByte trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.ByteSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(ByteConsumer action) {
            if (action == null)
                throw new NullPointerException();
            byte[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(ByteConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Byte> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class CharSpliterator implements Spliterator.OfChar {
        private final char[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public CharSpliterator(char[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public CharSpliterator(char[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfChar trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.CharSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(CharConsumer action) {
            if (action == null)
                throw new NullPointerException();
            char[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(CharConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Character> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class ShortSpliterator implements Spliterator.OfShort {
        private final short[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public ShortSpliterator(short[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public ShortSpliterator(short[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfShort trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.ShortSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(ShortConsumer action) {
            if (action == null)
                throw new NullPointerException();
            short[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(ShortConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Short> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class IntSpliterator implements Spliterator.OfInt {
        private final int[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public IntSpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public IntSpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.IntSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            if (action == null)
                throw new NullPointerException();
            int[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class LongSpliterator implements Spliterator.OfLong {
        private final long[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public LongSpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public LongSpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.LongSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            if (action == null)
                throw new NullPointerException();
            long[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class FloatSpliterator implements Spliterator.OfFloat {
        private final float[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public FloatSpliterator(float[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public FloatSpliterator(float[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfFloat trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.FloatSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(FloatConsumer action) {
            if (action == null)
                throw new NullPointerException();
            float[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(FloatConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Float> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    public static class DoubleSpliterator implements Spliterator.OfDouble {
        private final double[] array;
        private int index;        // current index, modified on advance/split
        private final int fence;  // one past last index
        private final int characteristics;

        public DoubleSpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        public DoubleSpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public Spliterator.OfDouble trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            return (lo >= mid)
                    ? null
                    : new Spliterators.DoubleSpliterator(array, lo, index = mid, characteristics);
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            if (action == null)
                throw new NullPointerException();
            double[] a = array;
            // hoist accesses and checks from loop
            int hi = fence, i = index;
            if (a.length >= hi && i >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return fence - index;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }
}
