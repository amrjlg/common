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

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.exception.NotImplementedException;
import io.github.amrjlg.function.CharBinaryOperator;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.CharFunction;
import io.github.amrjlg.function.CharPredicate;
import io.github.amrjlg.function.CharToByteFunction;
import io.github.amrjlg.function.CharToDoubleFunction;
import io.github.amrjlg.function.CharToFloatFunction;
import io.github.amrjlg.function.CharToIntFunction;
import io.github.amrjlg.function.CharToLongFunction;
import io.github.amrjlg.function.CharUnaryOperator;
import io.github.amrjlg.function.ObjCharConsumer;
import io.github.amrjlg.stream.ByteStream;
import io.github.amrjlg.stream.CharStream;
import io.github.amrjlg.stream.DoubleStream;
import io.github.amrjlg.stream.FloatStream;
import io.github.amrjlg.stream.IntStream;
import io.github.amrjlg.stream.LongStream;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.NodeBuilder;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.operations.FindOps;
import io.github.amrjlg.stream.operations.ForeachOps;
import io.github.amrjlg.stream.operations.MatchKind;
import io.github.amrjlg.stream.operations.MatchOps;
import io.github.amrjlg.stream.operations.ReduceOps;
import io.github.amrjlg.stream.operations.SliceOps;
import io.github.amrjlg.stream.operations.SortedOps;
import io.github.amrjlg.stream.spliterator.DelegatingSpliterator;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;
import io.github.amrjlg.stream.spliterator.WrappingSpliterator;
import io.github.amrjlg.util.CharSummaryStatistics;
import io.github.amrjlg.util.OptionalChar;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author amrjlg
 * @date 2021-09-22 11:35
 **/
public abstract class CharPipeline<Input> extends AbstractPipeline<Input, Character, CharStream>
        implements CharStream {
    public CharPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public CharPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    CharPipeline(AbstractPipeline<?, Input, ?> previousStage, int opFlags) {
        super(previousStage, opFlags);
    }

    @Override
    public Spliterator.OfChar spliterator() {
        return toChar(super.spliterator());
    }

    @Override
    <P_IN> Node<Character> evaluateToNode(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Character[]> generator) {
        return Nodes.collectChar(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Character> wrap(PipelineHelper<Character> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfChar<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    Spliterator<Character> lazySpliterator(Supplier<? extends Spliterator<Character>> supplier) {
        return new DelegatingSpliterator.OfChar((Supplier<? extends Spliterator.OfChar>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Character> spliterator, Sink<Character> sink) {
        Spliterator.OfChar spl = toChar(spliterator);
        CharConsumer s = toChar(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(s)) {

        }
    }


    @Override
    public NodeBuilder<Character> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Character[]> generator) {
        return Nodes.charBuilder(exactSizeIfKnown);
    }


    @Override
    StreamShape getOutputShape() {
        return StreamShape.CHAR_VALUE;
    }


    @Override
    public CharStream map(CharUnaryOperator mapper) {
        return new StateLessOp<Character>(this, StreamShape.CHAR_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<Character>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(CharFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Character, U>(this, StreamShape.CHAR_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedChar<U>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(CharToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Character>(this, StreamShape.CHAR_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedChar<Byte>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(CharToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Character>(this,StreamShape.CHAR_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedChar<Integer>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(CharToLongFunction mapper) {
        return new LongPipeline.StatelessOp<Character>(this,StreamShape.CHAR_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedChar<Long>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream mapToFloat(CharToFloatFunction mapper) {
        return new FloatPipeline.StatelessOp<Character>(this,StreamShape.CHAR_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedChar<Float>(sink) {
                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsFloat(value));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream mapToDouble(CharToDoubleFunction mapper) {
        return new DoublePipeline.StatelessOp<Character>(this,StreamShape.CHAR_VALUE,MAP_OP_FLAGS){

            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedChar<Double>(sink){

                    @Override
                    public void accept(char value) {
                        downstream.accept(mapper.applyAsDouble(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream flatMap(CharFunction<? extends CharStream> mapper) {
        return new StateLessOp<Character>(this, StreamShape.CHAR_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<Character>(sink) {

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(char value) {
                        try (CharStream stream = mapper.apply(value)) {
                            if (Objects.nonNull(sink)) {
                                stream.sequential().forEach(downstream::accept);
                            }
                        }
                    }
                };
            }
        };
    }

    @Override
    public CharStream filter(CharPredicate predicate) {
        return new StateLessOp<Character>(this, StreamShape.CHAR_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<Character>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }
                    @Override
                    public void accept(char value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public CharStream distinct() {
        return boxed().distinct().mapToChar(v -> v);
    }

    @Override
    public CharStream sorted() {
        return SortedOps.makeChar(this);
    }

    @Override
    public CharStream peek(CharConsumer action) {
        return new StateLessOp<Character>(this, StreamShape.CHAR_VALUE, 0) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<Character>(sink) {
                    @Override
                    public void accept(char value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public CharStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeChar(this, 0, maxSize);
    }

    @Override
    public CharStream skip(long n) {
        positive(n);
        if (n == 0) {
            return this;
        }
        return SliceOps.makeChar(this, n, -1);
    }

    @Override
    public void forEach(CharConsumer action) {
        evaluate(ForeachOps.makeChar(action, false));
    }

    @Override
    public void forEachOrdered(CharConsumer action) {
        evaluate(ForeachOps.makeChar(action, true));
    }

    @Override
    public char[] toArray() {
        Node.OfChar node = (Node.OfChar) evaluateToArrayNode(Character[]::new);
        return Nodes.flattenChar(node).asPrimitiveArray();
    }

    @Override
    public char reduce(char identity, CharBinaryOperator op) {
        return evaluate(ReduceOps.makeChar(identity, op));
    }

    @Override
    public OptionalChar reduce(CharBinaryOperator op) {
        return evaluate(ReduceOps.makeChar(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjCharConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeChar(supplier, accumulator, (left, right) -> {
            combiner.accept(left, right);
            return left;
        }));
    }

    @Override
    public char sum() {
        return reduce('0', (l, r) -> (char) (l + r));
    }

    @Override
    public OptionalChar min() {
        return reduce((l, r) -> l > r ? r : l);
    }

    @Override
    public OptionalChar max() {
        return reduce((l, r) -> l < r ? r : l);
    }

    @Override
    public long count() {
        return mapToLong(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {
        ObjCharConsumer<long[]> consumer = (values, value) -> {
            values[0]++;
            values[1]+=value;
        };
        long[] avg = collect(averageSupplier(), consumer, averageCombiner());
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[1] / avg[0])
                : OptionalDouble.empty();
    }

    @Override
    public CharSummaryStatistics summaryStatistics() {
        return collect(CharSummaryStatistics::new, CharSummaryStatistics::accept, CharSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(CharPredicate predicate) {
        return evaluate(MatchOps.makeChar(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalChar findFirst() {
        return evaluate(FindOps.makeChar(true));
    }

    @Override
    public OptionalChar findAny() {
        return evaluate(FindOps.makeChar(false));
    }

    @Override
    public Stream<Character> boxed() {
        return mapToObj(Character::valueOf);
    }

    @Override
    public PrimitiveIterator.OfChar iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public CharStream unordered() {
        if (!isOrdered()) {
            return this;
        }
        return new StateLessOp<Character>(this, StreamShape.CHAR_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return sink;
            }
        };
    }

    static class Head<In> extends CharPipeline<In> {

        public Head(Supplier<? extends Spliterator<Character>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Character> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sink<In> opWrapSink(int flags, Sink<Character> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(CharConsumer action) {
            if (!isParallel()) {
                CharPipeline.toChar(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(CharConsumer action) {
            if (!isParallel()) {
                CharPipeline.toChar(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEachOrdered(action);
            }
        }

    }

    private static Spliterator.OfChar toChar(Spliterator<Character> spliterator) {
        if (spliterator instanceof Spliterator.OfChar) {
            return (Spliterator.OfChar) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    private CharConsumer toChar(Sink<Character> sink) {
        if (sink instanceof CharConsumer) {
            return (CharConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    public static abstract class StateLessOp<In> extends CharPipeline<In> {

        public StateLessOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<In> extends CharPipeline<In> {

        public StatefulOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Character> opEvaluateParallel(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator, IntFunction<Character[]> generator);
    }
}
