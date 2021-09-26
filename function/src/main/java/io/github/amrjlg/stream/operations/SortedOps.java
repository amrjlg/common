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

import io.github.amrjlg.stream.ByteStream;
import io.github.amrjlg.stream.CharStream;
import io.github.amrjlg.stream.DoubleStream;
import io.github.amrjlg.stream.FloatStream;
import io.github.amrjlg.stream.IntStream;
import io.github.amrjlg.stream.LongStream;
import io.github.amrjlg.stream.ShortStream;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.AbstractPipeline;
import io.github.amrjlg.stream.pipeline.BytePipeline;
import io.github.amrjlg.stream.pipeline.CharPipeline;
import io.github.amrjlg.stream.pipeline.DoublePipeline;
import io.github.amrjlg.stream.pipeline.FloatPipeline;
import io.github.amrjlg.stream.pipeline.IntPipeline;
import io.github.amrjlg.stream.pipeline.LongPipeline;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.pipeline.ReferencePipeline;
import io.github.amrjlg.stream.pipeline.ShortPipeline;
import io.github.amrjlg.stream.sink.ByteSortingSink;
import io.github.amrjlg.stream.sink.CharSortingSink;
import io.github.amrjlg.stream.sink.DoubleSortingSink;
import io.github.amrjlg.stream.sink.FloatSortingSink;
import io.github.amrjlg.stream.sink.IntSortingSink;
import io.github.amrjlg.stream.sink.LongSortingSink;
import io.github.amrjlg.stream.sink.RefSortingSink;
import io.github.amrjlg.stream.sink.ShortSortingSink;
import io.github.amrjlg.stream.sink.SizedByteSortingSink;
import io.github.amrjlg.stream.sink.SizedCharSortingSink;
import io.github.amrjlg.stream.sink.SizedDoubleSortingSink;
import io.github.amrjlg.stream.sink.SizedFloatSortingSink;
import io.github.amrjlg.stream.sink.SizedIntSortingSink;
import io.github.amrjlg.stream.sink.SizedLongSortingSink;
import io.github.amrjlg.stream.sink.SizedRefSortingSink;
import io.github.amrjlg.stream.sink.SizedShortSortingSink;
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.IntFunction;


/**
 * @author amrjlg
 * @see java.util.stream.SortedOps
 **/
public class SortedOps {

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef<>(upstream);
    }

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
        return new OfRef<>(upstream, comparator);
    }

    public static ByteStream makeByte(AbstractPipeline<?, Byte, ?> upstream) {
        return new OfByte(upstream);
    }

    public static <Input> CharStream makeChar(AbstractPipeline<Input, Character, CharStream> upstream) {
        return new OfChar(upstream);
    }

    public static <Input> ShortStream makeShort(AbstractPipeline<Input, Short, ShortStream> upstream) {
        return new OfShort(upstream);
    }

    public static <In> IntStream makeInt(AbstractPipeline<In, Integer, IntStream> upstream) {
        return new OfInt(upstream);
    }

    public static <Input> LongStream makeLong(AbstractPipeline<Input, Long, LongStream> upstream) {
        return new OfLong(upstream);
    }

    public static <Input> FloatStream makeFLoat(AbstractPipeline<Input, Float, FloatStream> upstream) {
        return new OfFloat(upstream);
    }

    public static <T> DoubleStream makeDouble(AbstractPipeline<T, Double, DoubleStream> upstream) {
        return new OfDouble(upstream);
    }


    public static final class OfRef<T> extends ReferencePipeline.StatefulOp<T, T> {

        private static final int opFlags = StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED;
        private final boolean naturalSort;
        private final Comparator<? super T> comparator;

        @SuppressWarnings("unchecked")
        public OfRef(
                AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE, opFlags);
            this.naturalSort = true;
            this.comparator = (Comparator<? super T>) Comparator.naturalOrder();
        }

        public OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE, opFlags);
            this.comparator = comparator;
            this.naturalSort = false;
        }


        @Override
        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);

            if (StreamOpFlag.SORTED.isKnown(flags) && naturalSort) {
                return sink;
            } else if (StreamOpFlag.SORTED.isKnown(flags)) {
                return new SizedRefSortingSink<>(sink, comparator);
            } else {
                return new RefSortingSink<>(sink, comparator);
            }

        }

        @Override
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && naturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }

            T[] array = helper.evaluate(spliterator, true, generator).asArray(generator);
            Arrays.parallelSort(array, comparator);
            return Nodes.node(array);
        }
    }

    public static final class OfByte extends BytePipeline.StatefulOp<Byte> {

        public OfByte(AbstractPipeline<?, Byte, ?> upstream) {
            super(upstream, StreamShape.BYTE_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedByteSortingSink(sink);
            } else {
                return new ByteSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Byte> opEvaluateParallel(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator, IntFunction<Byte[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfByte node = (Node.OfByte) helper.evaluate(spliterator, true, generator);
            byte[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }

    public static final class OfChar extends CharPipeline.StatefulOp<Character> {
        public OfChar(AbstractPipeline<?, Character, ?> upstream) {
            super(upstream, StreamShape.CHAR_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedCharSortingSink(sink);
            } else {
                return new CharSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Character> opEvaluateParallel(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator, IntFunction<Character[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }

            Node.OfChar node = (Node.OfChar) helper.evaluate(spliterator, true, generator);
            char[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }


    public static class OfShort extends ShortPipeline.StatefulOp<Short> {
        public <Input> OfShort(AbstractPipeline<Input, Short, ShortStream> upstream) {
            super(upstream, StreamShape.SHORT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedShortSortingSink(sink);
            } else {
                return new ShortSortingSink(sink);
            }
        }


        @Override
        protected <P_IN> Node<Short> opEvaluateParallel(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator, IntFunction<Short[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfShort node = (Node.OfShort) helper.evaluate(spliterator, true, generator);

            short[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }

    private static class OfInt extends IntPipeline.StatefulOp<Integer> {
        public <In> OfInt(AbstractPipeline<In, Integer, IntStream> upstream) {
            super(upstream, StreamShape.INT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedIntSortingSink(sink);
            } else {
                return new IntSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfInt node = (Node.OfInt) helper.evaluate(spliterator, true, generator);

            int[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }

    private static class OfLong extends LongPipeline.StatefulOp<Long> {
        public <Input> OfLong(AbstractPipeline<Input, Long, LongStream> upstream) {
            super(upstream, StreamShape.LONG_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedLongSortingSink(sink);
            } else {
                return new LongSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfLong node = (Node.OfLong) helper.evaluate(spliterator, true, generator);
            long[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }

    private static class OfFloat extends FloatPipeline.StatefulOp<Float> {
        public <Input> OfFloat(AbstractPipeline<Input, Float, FloatStream> upstream) {
            super(upstream, StreamShape.FLOAT_VALUE, StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedFloatSortingSink(sink);
            } else {
                return new FloatSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Float> opEvaluateParallel(PipelineHelper<Float> helper, Spliterator<P_IN> spliterator, IntFunction<Float[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfFloat node = (Node.OfFloat) helper.evaluate(spliterator, true, generator);
            float[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }

    private static class OfDouble extends DoublePipeline.StatefulOp<Double> {
        public <T> OfDouble(AbstractPipeline<T, Double, DoubleStream> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE, StreamOpFlag.IS_ORDERED);
        }

        @Override
        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            } else if (StreamOpFlag.SORTED.isKnown(flags)) {
                return new SizedDoubleSortingSink(sink);
            } else {
                return new DoubleSortingSink(sink);
            }
        }

        @Override
        protected <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfDouble node = (Node.OfDouble) helper.evaluate(spliterator, true, generator);
            double[] array = node.asPrimitiveArray();
            Arrays.parallelSort(array);
            return Nodes.node(array);
        }
    }
}
