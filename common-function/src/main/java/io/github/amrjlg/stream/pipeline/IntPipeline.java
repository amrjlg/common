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
import io.github.amrjlg.function.IntToByteFunction;
import io.github.amrjlg.function.IntToCharFunction;
import io.github.amrjlg.function.IntToFloatFunction;
import io.github.amrjlg.function.IntToShortFunction;
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

import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 * @date 2021-09-23 11:43
 **/
public abstract class IntPipeline<In> extends AbstractPipeline<In, Integer, IntStream>
        implements IntStream {
    public IntPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public IntPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public IntPipeline(AbstractPipeline<?, In, ?> previousStage, int opFlags) {
        super(previousStage, opFlags);
    }

    protected Spliterator.OfInt adapter(Spliterator<Integer> spliterator) {
        if (spliterator instanceof Spliterator.OfInt) {
            return (Spliterator.OfInt) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    protected static IntConsumer adapter(Sink<Integer> sink) {
        if (sink instanceof IntConsumer) {
            return (IntConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    <P_IN> Node<Integer> evaluateToNode(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Integer[]> generator) {
        return Nodes.collectInt(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Integer> wrap(PipelineHelper<Integer> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfInt<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    Spliterator<Integer> lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new DelegatingSpliterator.OfInt((Supplier<? extends Spliterator.OfInt>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        Spliterator.OfInt spl = adapter(spliterator);
        IntConsumer consumer = adapter(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Integer> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Integer[]> generator) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }


    @Override
    StreamShape getOutputShape() {
        return StreamShape.INT_VALUE;
    }

    @Override
    public Spliterator.OfInt spliterator() {
        return adapter(super.spliterator());
    }

    // int stream

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void accept(int value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Integer, U>(this, StreamShape.INT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedInt<U>(sink) {
                    @Override
                    public void accept(int value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(IntToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Integer>(this, StreamShape.INT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedInt<Byte>(sink) {
                    @Override
                    public void accept(int value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(IntToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Integer>(this, StreamShape.INT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedInt<Character>(sink) {
                    @Override
                    public void accept(int value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream mapToShort(IntToShortFunction mapper) {
        return new ShortPipeline.StatelessOp<Integer>(this, StreamShape.INT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedInt<Short>(sink) {
                    @Override
                    public void accept(int value) {
                        downstream.accept(mapper.applyAsShort(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(IntToLongFunction mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public FloatStream mapToFloat(IntToFloatFunction mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(int value) {
                        try (IntStream stream = mapper.apply(value)) {
                            if (Objects.nonNull(stream)) {
                                stream.sequential().forEach(downstream::accept);
                            }
                        }
                    }
                };
            }
        };
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }
                    @Override
                    public void accept(int value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public IntStream distinct() {
        return boxed().distinct().mapToInt(v -> v);
    }

    @Override
    public IntStream sorted() {
        return SortedOps.makeInt(this);
    }

    @Override
    public IntStream peek(IntConsumer action) {
        return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, 0) {
            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    @Override
                    public void accept(int value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public IntStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeInt(this, 0, maxSize);
    }

    @Override
    public IntStream skip(long skip) {
        positive(skip);
        if (skip == 0) {
            return this;
        }
        return SliceOps.makeInt(this, skip, -1);
    }

    @Override
    public void forEach(IntConsumer action) {
        evaluate(ForeachOps.makeInt(action, false));
    }

    @Override
    public void forEachOrdered(IntConsumer action) {
        evaluate(ForeachOps.makeInt(action, true));
    }

    @Override
    public int[] toArray() {
        return Nodes.flattenInt((Node.OfInt) evaluateToArrayNode(Integer[]::new)).asPrimitiveArray();
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        return evaluate(ReduceOps.makeInt(identity, op));
    }

    @Override
    public OptionalInt reduce(IntBinaryOperator op) {
        return evaluate(ReduceOps.makeInt(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeInt(supplier, accumulator, toCombiner(combiner)));
    }

    @Override
    public int sum() {
        return reduce(0, Integer::sum);
    }

    @Override
    public OptionalInt min() {
        return reduce(Math::min);
    }

    @Override
    public OptionalInt max() {
        return reduce(Math::max);
    }

    @Override
    public long count() {
        return mapToLong(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {
        ObjIntConsumer<long[]> consumer = ((longs, value) -> {
            longs[0]++;
            longs[1] += value;
        });
        long[] avg = collect(averageSupplier(), consumer, averageCombiner());
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[1] / avg[0])
                : OptionalDouble.empty();
    }

    @Override
    public IntSummaryStatistics summaryStatistics() {
        return collect(IntSummaryStatistics::new, IntSummaryStatistics::accept, IntSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(IntPredicate predicate) {
        return evaluate(MatchOps.makeInt(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalInt findFirst() {
        return evaluate(FindOps.makeInt(true));
    }

    @Override
    public OptionalInt findAny() {
        return evaluate(FindOps.makeInt(false));
    }

    @Override
    public Stream<Integer> boxed() {
        return mapToObj(Integer::valueOf);
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public IntStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Integer>(this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED) {

                @Override
                public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                    return sink;
                }
            };
        }
        return this;
    }

    static class Head<In> extends IntPipeline<In> {

        public Head(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sink<In> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(IntConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(IntConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatelessOp<In> extends IntPipeline<In> {

        public StatelessOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<In> extends IntPipeline<In> {
        public StatefulOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator);

    }
}
