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

package io.github.amrjlg.stream.operations;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.TerminalSink;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.task.ForEachOrderedTask;
import io.github.amrjlg.stream.task.ForEachTask;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public abstract class ForeachOp<T> implements TerminalOp<T, Void>, TerminalSink<T, Void> {
    private final boolean ordered;

    public ForeachOp(boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public int getOpFlags() {
        return ordered ? 0 : StreamOpFlag.NOT_SORTED;
    }

    @Override
    public <Out> Void evaluateSequential(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
        return helper.wrapAndCopyInto(this, spliterator).get();
    }

    @Override
    public <Out> Void evaluateParallel(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
        if (ordered) {
            new ForEachOrderedTask<>(helper, spliterator, this).invoke();
        } else {
            new ForEachTask<>(helper, spliterator, helper.wrapSink(this)).invoke();
        }
        return null;
    }

    @Override
    public Void get() {
        return null;
    }

    @Override
    public abstract StreamShape inputShape();

    public static final class OfRef<T> extends ForeachOp<T> {
        final Consumer<? super T> consumer;

        public OfRef(Consumer<? super T> consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
        }

        @Override
        public void accept(T t) {
            consumer.accept(t);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.REFERENCE;
        }
    }

    public static final class OfByte extends ForeachOp<Byte> implements Sink.OfByte {
        private final ByteConsumer consumer;

        public OfByte(ByteConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(byte value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.BYTE_VALUE;
        }
    }

    public static final class OfShort extends ForeachOp<Short> implements Sink.OfShort {
        private final ShortConsumer consumer;

        public OfShort(ShortConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(short value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.SHORT_VALUE;
        }
    }

    public static final class OfChar extends ForeachOp<Character> implements Sink.OfChar {
        private final CharConsumer consumer;

        public OfChar(CharConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(char value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.CHAR_VALUE;
        }
    }

    public static final class OfInt extends ForeachOp<Integer> implements Sink.OfInt {
        private final IntConsumer consumer;

        public OfInt(IntConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(int value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.INT_VALUE;
        }
    }

    public static final class OfLong extends ForeachOp<Long> implements Sink.OfLong {
        private final LongConsumer consumer;

        public OfLong(LongConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(long value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    public static final class OfFloat extends ForeachOp<Float> implements Sink.OfFloat {
        private final FloatConsumer consumer;

        public OfFloat(FloatConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(float value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.FLOAT_VALUE;
        }
    }

    public static final class OfDouble extends ForeachOp<Double> implements Sink.OfDouble {
        private final DoubleConsumer consumer;

        public OfDouble(DoubleConsumer consumer, boolean ordered) {
            super(ordered);
            this.consumer = consumer;
            Objects.requireNonNull(consumer);
        }

        @Override
        public void accept(double value) {
            consumer.accept(value);
        }

        @Override
        public StreamShape inputShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }
}