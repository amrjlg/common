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
import io.github.amrjlg.stream.buffer.ArrayBuffer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public abstract class UnorderedSliceSpliterator<Type, TypeSpl extends Spliterator<Type>> {

    static final int CHUNK_SIZE = 1 << 7;
    protected final TypeSpl spl;
    protected final boolean unlimited;
    private final long skipThreshold;
    private final AtomicLong premits;

    public UnorderedSliceSpliterator(TypeSpl spl, long skip, long limit) {
        this.spl = spl;
        this.unlimited = limit < 0;
        this.skipThreshold = Math.max(0, limit);
        this.premits = new AtomicLong(limit >= 0 ? skip + limit : skip);
    }

    public UnorderedSliceSpliterator(TypeSpl spl, UnorderedSliceSpliterator<Type, TypeSpl> parent) {
        this.spl = spl;
        this.unlimited = parent.unlimited;
        this.skipThreshold = parent.skipThreshold;
        this.premits = parent.premits;
    }

    protected final long acquirePermits(long ele) {
        long remain, grabbing;
        assert ele > 0;
        do {
            remain = premits.get();
            if (remain == 0) {
                return unlimited ? ele : 0;
            }
            grabbing = Math.min(remain, ele);
        } while (grabbing > 0 && !premits.compareAndSet(remain, remain - grabbing));

        if (unlimited) {
            return Math.max(ele - grabbing, 0);
        } else if (remain > skipThreshold) {
            return Math.max(grabbing - (remain - skipThreshold), 0);
        } else {
            return grabbing;
        }

    }

    enum PermitStatus {
        NO_MORE,
        MAYBE_MORE,
        UNLIMITED;
    }

    protected final PermitStatus permitStatus() {
        if (premits.get() > 0) {
            return PermitStatus.MAYBE_MORE;
        } else {
            return unlimited ? PermitStatus.UNLIMITED : PermitStatus.NO_MORE;
        }
    }

    protected abstract TypeSpl makeSpliterator(TypeSpl spl);


    public final TypeSpl trySplit() {
        if (premits.get() == 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        TypeSpl spliterator = (TypeSpl) spl.trySplit();
        if (spliterator == null) {
            return null;
        }
        return makeSpliterator(spliterator);
    }

    public final long estimateSize() {
        return spl.estimateSize();
    }

    public final int characteristics() {
        return spl.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED);
    }


    public static final class OfRef<T> extends UnorderedSliceSpliterator<T, Spliterator<T>> implements Spliterator<T>, Consumer<T> {

        private T tmp;

        public OfRef(Spliterator<T> spliterator, long skip, long limit) {
            super(spliterator, skip, limit);
        }

        public OfRef(Spliterator<T> spliterator, OfRef<T> parent) {
            super(spliterator, parent);
        }


        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            while (permitStatus() != PermitStatus.NO_MORE) {
                if (!spl.tryAdvance(this)) {
                    return false;
                }
                if (acquirePermits(1) == 1) {
                    consumer.accept(tmp);
                    tmp = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Spliterator<T> makeSpliterator(Spliterator<T> spliterator) {
            return new UnorderedSliceSpliterator.OfRef<T>(spliterator, this);
        }

        @Override
        public void accept(T t) {
            tmp = t;
        }
    }


    public static abstract class OfPrimitive<
            Primitive,
            PrimitiveConsumer,
            PrimitiveBuffer extends ArrayBuffer.OfPrimitive<PrimitiveConsumer>,
            PrimitiveSpl extends Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpl>>
            extends UnorderedSliceSpliterator<Primitive, PrimitiveSpl>
            implements Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpl> {

        public OfPrimitive(PrimitiveSpl spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfPrimitive(PrimitiveSpl spl, UnorderedSliceSpliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveBuffer, PrimitiveSpl> parent) {
            super(spl, parent);
        }

        protected abstract void acceptConsumed(PrimitiveConsumer consumer);

        protected abstract PrimitiveBuffer bufferCreate(int capacity);

        @Override
        public boolean tryAdvance(PrimitiveConsumer action) {

            Objects.requireNonNull(action);
            PrimitiveConsumer consumer = (PrimitiveConsumer) this;
            while (permitStatus() != PermitStatus.NO_MORE) {
                if (!spl.tryAdvance(consumer)) {
                    return false;
                }
                if (acquirePermits(1) == 1) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEachRemaining(PrimitiveConsumer action) {
            Objects.requireNonNull(action);
            PrimitiveBuffer buffer = null;
            PermitStatus permitStatus = permitStatus();
            while (permitStatus != PermitStatus.NO_MORE) {
                if (permitStatus == PermitStatus.MAYBE_MORE) {
                    if (buffer == null) {
                        buffer = bufferCreate(CHUNK_SIZE);
                    }
                    buffer.reset();
                    PrimitiveConsumer consumer = (PrimitiveConsumer) buffer;
                    long premitsRequested = 0;
                    while (premitsRequested++ < CHUNK_SIZE && spl.tryAdvance(consumer)) {
                    }

                    if (premitsRequested == 0) {
                        break;
                    }
                    buffer.forEach(action, acquirePermits(premitsRequested));
                } else {
                    spl.forEachRemaining(action);
                    break;
                }
                permitStatus = permitStatus();
            }
        }
    }


    public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, ArrayBuffer.OfByte, Spliterator.OfByte>
            implements Spliterator.OfByte, ByteConsumer {

        private byte tmp;

        public OfByte(Spliterator.OfByte ofByte, long skip, long limit) {
            super(ofByte, skip, limit);
        }

        public OfByte(Spliterator.OfByte ofByte, UnorderedSliceSpliterator.OfPrimitive<Byte, ByteConsumer, ArrayBuffer.OfByte, Spliterator.OfByte> parent) {
            super(ofByte, parent);
        }

        @Override
        public void accept(byte value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfByte makeSpliterator(Spliterator.OfByte spl) {
            return new UnorderedSliceSpliterator.OfByte(spl, this);
        }

        @Override
        protected void acceptConsumed(ByteConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfByte bufferCreate(int capacity) {
            return new ArrayBuffer.OfByte(capacity);
        }
    }

    public static final class OfShort extends OfPrimitive<Short, ShortConsumer, ArrayBuffer.OfShort, Spliterator.OfShort>
            implements Spliterator.OfShort, ShortConsumer {

        private short tmp;

        public OfShort(Spliterator.OfShort spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfShort(Spliterator.OfShort spl, UnorderedSliceSpliterator.OfPrimitive<Short, ShortConsumer, ArrayBuffer.OfShort, Spliterator.OfShort> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(short value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfShort makeSpliterator(Spliterator.OfShort spl) {
            return new UnorderedSliceSpliterator.OfShort(spl, this);
        }

        @Override
        protected void acceptConsumed(ShortConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfShort bufferCreate(int capacity) {
            return new ArrayBuffer.OfShort(capacity);
        }
    }

    public static final class OfChar extends OfPrimitive<Character, CharConsumer, ArrayBuffer.OfChar, Spliterator.OfChar>
            implements Spliterator.OfChar, CharConsumer {

        private char tmp;

        public OfChar(Spliterator.OfChar spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfChar(Spliterator.OfChar spl, UnorderedSliceSpliterator.OfPrimitive<Character, CharConsumer, ArrayBuffer.OfChar, Spliterator.OfChar> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(char value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfChar makeSpliterator(Spliterator.OfChar spl) {
            return new UnorderedSliceSpliterator.OfChar(spl, this);
        }

        @Override
        protected void acceptConsumed(CharConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfChar bufferCreate(int capacity) {
            return new ArrayBuffer.OfChar(capacity);
        }
    }

    public static final class OfInt extends OfPrimitive<Integer, IntConsumer, ArrayBuffer.OfInt, Spliterator.OfInt>
            implements Spliterator.OfInt, IntConsumer {

        private int tmp;

        public OfInt(Spliterator.OfInt spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfInt(Spliterator.OfInt spl, UnorderedSliceSpliterator.OfPrimitive<Integer, IntConsumer, ArrayBuffer.OfInt, Spliterator.OfInt> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(int value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt spl) {
            return new UnorderedSliceSpliterator.OfInt(spl, this);
        }

        @Override
        protected void acceptConsumed(IntConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfInt bufferCreate(int capacity) {
            return new ArrayBuffer.OfInt(capacity);
        }
    }

    public static final class OfLong extends OfPrimitive<Long, LongConsumer, ArrayBuffer.OfLong, Spliterator.OfLong>
            implements Spliterator.OfLong, LongConsumer {

        private long tmp;

        public OfLong(Spliterator.OfLong spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfLong(Spliterator.OfLong spl, UnorderedSliceSpliterator.OfPrimitive<Long, LongConsumer, ArrayBuffer.OfLong, Spliterator.OfLong> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(long value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong spl) {
            return new UnorderedSliceSpliterator.OfLong(spl, this);
        }

        @Override
        protected void acceptConsumed(LongConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfLong bufferCreate(int capacity) {
            return new ArrayBuffer.OfLong(capacity);
        }
    }

    public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, ArrayBuffer.OfFloat, Spliterator.OfFloat>
            implements Spliterator.OfFloat, FloatConsumer {

        private float tmp;

        public OfFloat(Spliterator.OfFloat spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfFloat(Spliterator.OfFloat spl, UnorderedSliceSpliterator.OfPrimitive<Float, FloatConsumer, ArrayBuffer.OfFloat, Spliterator.OfFloat> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(float value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfFloat makeSpliterator(Spliterator.OfFloat spl) {
            return new UnorderedSliceSpliterator.OfFloat(spl, this);
        }

        @Override
        protected void acceptConsumed(FloatConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfFloat bufferCreate(int capacity) {
            return new ArrayBuffer.OfFloat(capacity);
        }
    }

    public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, ArrayBuffer.OfDouble, Spliterator.OfDouble>
            implements Spliterator.OfDouble, DoubleConsumer {

        private double tmp;

        public OfDouble(Spliterator.OfDouble spl, long skip, long limit) {
            super(spl, skip, limit);
        }

        public OfDouble(Spliterator.OfDouble spl, UnorderedSliceSpliterator.OfPrimitive<Double, DoubleConsumer, ArrayBuffer.OfDouble, Spliterator.OfDouble> parent) {
            super(spl, parent);
        }

        @Override
        public void accept(double value) {
            tmp = value;
        }

        @Override
        protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble spl) {
            return new UnorderedSliceSpliterator.OfDouble(spl, this);
        }

        @Override
        protected void acceptConsumed(DoubleConsumer consumer) {
            consumer.accept(tmp);
        }

        @Override
        protected ArrayBuffer.OfDouble bufferCreate(int capacity) {
            return new ArrayBuffer.OfDouble(capacity);
        }
    }

}
