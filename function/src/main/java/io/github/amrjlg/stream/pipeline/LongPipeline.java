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

import io.github.amrjlg.function.LongToByteFunction;
import io.github.amrjlg.function.LongToCharFunction;
import io.github.amrjlg.function.LongToFloatFunction;
import io.github.amrjlg.function.LongToShortFunction;
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

import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 * @date 2021-09-24 10:53
 **/
public abstract class LongPipeline<Input> extends AbstractPipeline<Input, Long, LongStream>
        implements LongStream {
    public LongPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public LongPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    LongPipeline(AbstractPipeline<?, Input, ?> previousStage, int opFlags) {
        super(previousStage, opFlags);
    }

    @Override
    <P_IN> Node<Long> evaluateToNode(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Long[]> generator) {
        return Nodes.collectLong(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Long> wrap(PipelineHelper<Long> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfLong<>(ph, supplier, isParallel);
    }

    @Override
    Spliterator<Long> lazySpliterator(Supplier<? extends Spliterator<Long>> supplier) {
        return new DelegatingSpliterator.OfLong((Supplier<? extends Spliterator.OfLong>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Long> spliterator, Sink<Long> sink) {
        Spliterator.OfLong spl = adapter(spliterator);
        LongConsumer consumer = adapter(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Long> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Long[]> generator) {
        return Nodes.longBuilder(exactSizeIfKnown);
    }

    @Override
    StreamShape getOutputShape() {
        return StreamShape.LONG_VALUE;
    }

    @Override
    public Spliterator.OfLong spliterator() {
        return adapter(super.spliterator());
    }

    @Override
    public LongStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_ORDERED) {
                @Override
                public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                    return sink;
                }
            };
        } else {
            return this;
        }
    }

    @Override
    public LongStream map(LongUnaryOperator mapper) {
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Long, U>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedLong<U>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(LongToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Long>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedLong<Byte>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(LongToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Long>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedLong<Character>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream mapToShort(LongToShortFunction mapper) {
        return new ShortPipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedLong<Short>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.applyAsShort(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(LongToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Long>(this, StreamShape.LONG_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedLong<Integer>(sink) {
                    @Override
                    public void accept(long value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream mapToFloat(LongToFloatFunction mapper) {
        //TODO IMPL
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleStream mapToDouble(LongToDoubleFunction mapper) {
        //TODO IMPL
        throw new UnsupportedOperationException();
    }

    @Override
    public LongStream flatMap(LongFunction<? extends LongStream> mapper) {
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(long value) {
                        Optional.ofNullable(mapper.apply(value))
                                .ifPresent(s -> s.sequential().forEach(downstream::accept));
                    }
                };
            }
        };
    }

    @Override
    public LongStream filter(LongPredicate predicate) {
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    @Override
                    public void accept(long value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public LongStream distinct() {
        return boxed().distinct().mapToLong(v -> v);
    }

    @Override
    public LongStream sorted() {
        return SortedOps.makeLong(this);
    }

    @Override
    public LongStream peek(LongConsumer action) {
        return new StatelessOp<Long>(this, StreamShape.LONG_VALUE, 0) {
            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    @Override
                    public void accept(long value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public LongStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeLong(this, 0, maxSize);
    }

    @Override
    public LongStream skip(long n) {
        positive(n);
        if (n == 0) {
            return this;
        }
        return SliceOps.makeLong(this, n, -1);
    }

    @Override
    public void forEach(LongConsumer action) {
        evaluate(ForeachOps.makeLong(action, false));
    }

    @Override
    public void forEachOrdered(LongConsumer action) {
        evaluate(ForeachOps.makeLong(action, true));
    }

    @Override
    public long[] toArray() {
        return Nodes.flattenLong((Node.OfLong) evaluateToArrayNode(Long[]::new)).asPrimitiveArray();
    }

    @Override
    public long reduce(long identity, LongBinaryOperator op) {
        return evaluate(ReduceOps.makeLong(identity, op));
    }

    @Override
    public OptionalLong reduce(LongBinaryOperator op) {

        return evaluate(ReduceOps.makeLong(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeLong(supplier, accumulator, toCombiner(combiner)));
    }

    @Override
    public long sum() {
        return reduce(0, Long::sum);
    }

    @Override
    public OptionalLong min() {
        return reduce(Math::min);
    }

    @Override
    public OptionalLong max() {
        return reduce(Math::max);
    }

    @Override
    public long count() {
        return map(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {
        ObjLongConsumer<long[]> consumer = (values, value) -> {
            values[0]++;
            values[1]+=value;
        };
        long[] avg = collect(averageSupplier(), consumer, averageCombiner());
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[0] / avg[1])
                : OptionalDouble.empty();
    }

    @Override
    public LongSummaryStatistics summaryStatistics() {
        return collect(LongSummaryStatistics::new, LongSummaryStatistics::accept, LongSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(LongPredicate predicate) {
        return evaluate(MatchOps.makeLong(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(LongPredicate predicate) {
        return evaluate(MatchOps.makeLong(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(LongPredicate predicate) {
        return evaluate(MatchOps.makeLong(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalLong findFirst() {
        return evaluate(FindOps.makeLong(true));
    }

    @Override
    public OptionalLong findAny() {
        return evaluate(FindOps.makeLong(false));
    }

    @Override
    public Stream<Long> boxed() {
        return mapToObj(Long::valueOf);
    }

    @Override
    public PrimitiveIterator.OfLong iterator() {
        return Spliterators.iterator(spliterator());
    }

    public static Spliterator.OfLong adapter(Spliterator<Long> spliterator) {
        if (spliterator instanceof Spliterator.OfLong) {
            return (Spliterator.OfLong) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    public static LongConsumer adapter(Sink<Long> sink) {
        if (sink instanceof LongConsumer) {
            return (LongConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    static class Head<In> extends LongPipeline<In> {

        public Head(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<?> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sink<In> opWrapSink(int flags, Sink<Long> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(LongConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(LongConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatelessOp<In> extends LongPipeline<In> {

        StatelessOp(AbstractPipeline<?, In, ?> previousStage, StreamShape shape, int opFlags) {
            super(previousStage, opFlags);
            assert previousStage.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<In> extends LongPipeline<In> {

        public StatefulOp(AbstractPipeline<?, In, ?> previousStage, StreamShape shape, int opFlags) {
            super(previousStage, opFlags);
            assert previousStage.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator);
    }
}
