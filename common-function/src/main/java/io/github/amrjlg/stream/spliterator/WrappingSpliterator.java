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
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.buffer.AbstractSpinedBuffer;
import io.github.amrjlg.stream.buffer.SpinedBuffer;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 * @see java.util.stream.StreamSpliterators.AbstractWrappingSpliterator
 **/
public abstract class WrappingSpliterator<Input, Output, Buffer extends AbstractSpinedBuffer>
        implements Spliterator<Output> {

    final boolean parallel;
    final PipelineHelper<Output> helper;
    private Supplier<Spliterator<Input>> spliteratorSupplier;
    Spliterator<Input> spliterator;
    Sink<Input> bufferSink;
    BooleanSupplier pusher;

    long nextToConsume;

    Buffer buffer;
    boolean finished;

    WrappingSpliterator(PipelineHelper<Output> helper, Supplier<Spliterator<Input>> spliteratorSupplier, boolean parallel) {
        this.helper = helper;
        this.spliteratorSupplier = spliteratorSupplier;
        this.spliterator = null;
        this.parallel = parallel;
    }

    WrappingSpliterator(PipelineHelper<Output> helper, Spliterator<Input> spliterator, boolean parallel) {
        this.helper = helper;
        this.parallel = parallel;
        this.spliterator = spliterator;
        this.spliteratorSupplier = null;
    }

    final void init() {
        if (spliterator == null) {
            spliterator = spliteratorSupplier.get();
            spliteratorSupplier = null;
        }
    }

    final boolean doAdvance() {
        if (buffer == null) {
            if (finished) {
                return false;
            }
            init();
            initPartialTraversalState();
            nextToConsume = 0;
            bufferSink.begin(spliterator.getExactSizeIfKnown());
            return fillBuffer();
        } else {
            ++nextToConsume;
            boolean hasNext = nextToConsume < buffer.count();
            if (!hasNext) {
                nextToConsume = 0;
                buffer.clear();
                hasNext = fillBuffer();
            }
            return hasNext;
        }
    }

    private boolean fillBuffer() {
        while (buffer.count() == 0) {
            if (bufferSink.cancellationRequested() || !pusher.getAsBoolean()) {
                if (finished) {
                    return false;
                } else {
                    bufferSink.end();
                    finished = true;
                }
            }
        }
        return true;
    }

    abstract WrappingSpliterator<Input, Output, ?> wrap(Spliterator<Input> spliterator);

    abstract void initPartialTraversalState();

    @Override
    public Spliterator<Output> trySplit() {
        if (parallel && !finished) {
            init();
            Spliterator<Input> split = spliterator.trySplit();
            if (Objects.isNull(split)) {
                return null;
            }
            return wrap(split);
        }
        return null;
    }

    @Override
    public long estimateSize() {
        init();
        return spliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        init();
        if (StreamOpFlag.SIZED.isKnown(helper.getStreamAndOpFlags())) {
            return spliterator.getExactSizeIfKnown();
        }
        return -1;
    }

    @Override
    public int characteristics() {
        init();
        int characteristics = StreamOpFlag.toCharacteristics(StreamOpFlag.toStreamFlags(helper.getStreamAndOpFlags()));

        if ((characteristics & Spliterator.SIZED) != 0) {
            characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            characteristics = characteristics | (spliterator.characteristics() & (Spliterator.SIZED | Spliterator.SUBSIZED));
        }

        return characteristics;
    }

    @Override
    public Comparator<? super Output> getComparator() {
        if (!hasCharacteristics(ORDERED)) {
            throw new IllegalArgumentException();
        }
        return null;
    }


    public static final class OfRef<In, Out>
            extends WrappingSpliterator<In, Out, SpinedBuffer<Out>> {

        public OfRef(PipelineHelper<Out> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfRef(PipelineHelper<Out> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Out> consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Out, ?> wrap(Spliterator<In> spliterator) {
            return new OfRef<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer<>();
            bufferSink = helper.wrapSink(buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public void forEachRemaining(Consumer<? super Out> consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto(consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfByte<In> extends WrappingSpliterator<In, Byte, SpinedBuffer.OfByte>
            implements Spliterator.OfByte {

        public OfByte(PipelineHelper<Byte> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfByte(PipelineHelper<Byte> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(ByteConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Byte, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfByte<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfByte();
            bufferSink = helper.wrapSink((Sink.OfByte) buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfByte trySplit() {
            return (Spliterator.OfByte) super.trySplit();
        }

        @Override
        public void forEachRemaining(ByteConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfByte)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfShort<In> extends WrappingSpliterator<In, Short, SpinedBuffer.OfShort>
            implements Spliterator.OfShort {

        public OfShort(PipelineHelper<Short> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfShort(PipelineHelper<Short> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(ShortConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Short, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfShort<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfShort();
            bufferSink = helper.wrapSink((Sink.OfShort)buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfShort trySplit() {
            return (Spliterator.OfShort) super.trySplit();
        }

        @Override
        public void forEachRemaining(ShortConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfShort)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfChar<In> extends WrappingSpliterator<In, Character, SpinedBuffer.OfChar>
            implements Spliterator.OfChar {

        public OfChar(PipelineHelper<Character> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfChar(PipelineHelper<Character> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(CharConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Character, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfChar<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfChar();
            bufferSink = helper.wrapSink((Sink.OfChar)buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfChar trySplit() {
            return (Spliterator.OfChar) super.trySplit();
        }

        @Override
        public void forEachRemaining(CharConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfChar)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfInt<In> extends WrappingSpliterator<In, Integer, SpinedBuffer.OfInt>
            implements Spliterator.OfInt {

        public OfInt(PipelineHelper<Integer> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfInt(PipelineHelper<Integer> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Integer, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfInt<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfInt();
            bufferSink = helper.wrapSink((Sink.OfInt) buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfInt trySplit() {
            return (Spliterator.OfInt) super.trySplit();
        }

        @Override
        public void forEachRemaining(IntConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfInt)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfLong<In> extends WrappingSpliterator<In, Long, SpinedBuffer.OfLong>
            implements Spliterator.OfLong {

        public OfLong(PipelineHelper<Long> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfLong(PipelineHelper<Long> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Long, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfLong<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfLong();
            bufferSink = helper.wrapSink((Sink.OfLong)buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfLong trySplit() {
            return (Spliterator.OfLong) super.trySplit();
        }

        @Override
        public void forEachRemaining(LongConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfLong)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfFloat<In> extends WrappingSpliterator<In, Float, SpinedBuffer.OfFloat>
            implements Spliterator.OfFloat {

        public OfFloat(PipelineHelper<Float> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfFloat(PipelineHelper<Float> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(FloatConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Float, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfFloat<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfFloat();
            bufferSink = helper.wrapSink((Sink.OfFloat)buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfFloat trySplit() {
            return (Spliterator.OfFloat) super.trySplit();
        }

        @Override
        public void forEachRemaining(FloatConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfFloat)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

    public static final class OfDouble<In> extends WrappingSpliterator<In, Double, SpinedBuffer.OfDouble>
            implements Spliterator.OfDouble {

        public OfDouble(PipelineHelper<Double> helper, Supplier<Spliterator<In>> spliteratorSupplier, boolean parallel) {
            super(helper, spliteratorSupplier, parallel);
        }

        public OfDouble(PipelineHelper<Double> helper, Spliterator<In> spliterator, boolean parallel) {
            super(helper, spliterator, parallel);
        }

        @Override
        public boolean tryAdvance(DoubleConsumer consumer) {
            Objects.requireNonNull(consumer);
            boolean hasNext = doAdvance();
            if (hasNext) {
                consumer.accept(buffer.get(nextToConsume));
            }
            return hasNext;
        }

        @Override
        WrappingSpliterator<In, Double, ?> wrap(Spliterator<In> spliterator) {
            return new WrappingSpliterator.OfDouble<>(helper, spliterator, parallel);
        }

        @Override
        void initPartialTraversalState() {
            buffer = new SpinedBuffer.OfDouble();
            bufferSink = helper.wrapSink((Sink.OfDouble)buffer::accept);
            pusher = () -> spliterator.tryAdvance(bufferSink);
        }

        @Override
        public Spliterator.OfDouble trySplit() {
            return (Spliterator.OfDouble) super.trySplit();
        }

        @Override
        public void forEachRemaining(DoubleConsumer consumer) {
            if (buffer == null && !finished) {
                Objects.requireNonNull(consumer);
                init();
                helper.wrapAndCopyInto((Sink.OfDouble)consumer::accept, spliterator);
                finished = true;
            } else {
                while (tryAdvance(consumer)) {

                }
            }
        }
    }

}
