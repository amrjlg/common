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
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.pipeline.BytePipeline;
import io.github.amrjlg.stream.pipeline.CharPipeline;
import io.github.amrjlg.stream.sink.ByteSortingSink;
import io.github.amrjlg.stream.sink.CharSortingSink;
import io.github.amrjlg.stream.sink.SizedByteSortingSink;
import io.github.amrjlg.stream.sink.SizedCharSortingSink;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.AbstractPipeline;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.pipeline.ReferencePipeline;
import io.github.amrjlg.stream.sink.RefSortingSink;
import io.github.amrjlg.stream.sink.SizedRefSortingSink;

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

}
