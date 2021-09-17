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

package io.github.amrjlg.stream.buffer;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public abstract class ArrayBuffer {
    protected int index;

    public void reset() {
        index = 0;
    }

    public static final class OfRef<T> extends ArrayBuffer implements Consumer<T> {
        private final T[] array;

        @SuppressWarnings("unchecked")
        public OfRef(int size) {
            this.array = (T[]) new Object[size];
        }

        @Override
        public void accept(T t) {
            array[index++] = t;
        }

        public void forEach(Consumer<? super T> action, long end) {
            for (int i = 0; i < end; i++) {
                action.accept(array[i]);
            }
        }
    }

    public static abstract class OfPrimitive<Consumer> extends ArrayBuffer {

        public abstract void forEach(Consumer consumer, long end);
    }

    public static final class OfByte extends OfPrimitive<ByteConsumer> implements ByteConsumer {
        private final byte[] array;

        public OfByte(int size) {
            this.array = new byte[size];
        }

        @Override
        public void accept(byte value) {
            array[index++] = value;
        }

        @Override
        public void forEach(ByteConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfShort extends OfPrimitive<ShortConsumer> implements ShortConsumer {
        private final short[] array;

        public OfShort(int size) {
            this.array = new short[size];
        }

        @Override
        public void accept(short value) {
            array[index++] = value;
        }

        @Override
        public void forEach(ShortConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfChar extends OfPrimitive<CharConsumer> implements CharConsumer {
        private final char[] array;

        public OfChar(int size) {
            this.array = new char[size];
        }

        @Override
        public void accept(char value) {
            array[index++] = value;
        }

        @Override
        public void forEach(CharConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfInt extends OfPrimitive<IntConsumer> implements IntConsumer {
        private final int[] array;

        public OfInt(int size) {
            this.array = new int[size];
        }

        @Override
        public void accept(int value) {
            array[index++] = value;
        }

        @Override
        public void forEach(IntConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfLong extends OfPrimitive<LongConsumer> implements LongConsumer {
        private final long[] array;

        public OfLong(int size) {
            this.array = new long[size];
        }

        @Override
        public void accept(long value) {
            array[index++] = value;
        }

        @Override
        public void forEach(LongConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfFloat extends OfPrimitive<FloatConsumer> implements FloatConsumer {
        private final float[] array;

        public OfFloat(int size) {
            this.array = new float[size];
        }

        @Override
        public void accept(float value) {
            array[index++] = value;
        }

        @Override
        public void forEach(FloatConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }

    public static final class OfDouble extends OfPrimitive<DoubleConsumer> implements DoubleConsumer {
        private final double[] array;

        public OfDouble(int size) {
            this.array = new double[size];
        }

        @Override
        public void accept(double value) {
            array[index++] = value;
        }

        @Override
        public void forEach(DoubleConsumer consumer, long end) {
            for (int i = 0; i < end; i++) {
                consumer.accept(array[i]);
            }
        }
    }
}