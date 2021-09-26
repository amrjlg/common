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
import io.github.amrjlg.function.FloatBinaryOperator;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.FloatFunction;
import io.github.amrjlg.function.FloatPredicate;
import io.github.amrjlg.function.FloatToByteFunction;
import io.github.amrjlg.function.FloatToCharFunction;
import io.github.amrjlg.function.FloatToDoubleFunction;
import io.github.amrjlg.function.FloatToIntFunction;
import io.github.amrjlg.function.FloatToLongFunction;
import io.github.amrjlg.function.FloatUnaryOperator;
import io.github.amrjlg.function.ObjFloatConsumer;
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
import io.github.amrjlg.util.FloatSummaryStatistics;
import io.github.amrjlg.util.OptionalFloat;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author amrjlg
 * @date 2021-09-26 11:13
 **/
public abstract class FloatPipeline<Input> extends AbstractPipeline<Input, Float, FloatStream>
        implements FloatStream {
    public FloatPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public FloatPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    FloatPipeline(AbstractPipeline<?, Input, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    public static Spliterator.OfFloat adapter(Spliterator<Float> spliterator) {
        if (spliterator instanceof Spliterator.OfFloat) {
            return (Spliterator.OfFloat) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    public static FloatConsumer adapter(Sink<Float> sink) {
        if (sink instanceof FloatConsumer) {
            return (FloatConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    <P_IN> Node<Float> evaluateToNode(PipelineHelper<Float> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Float[]> generator) {
        return Nodes.collectFloat(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Float> wrap(PipelineHelper<Float> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfFloat<>(ph, supplier, isParallel);
    }

    @Override
    Spliterator<Float> lazySpliterator(Supplier<? extends Spliterator<Float>> supplier) {
        return new DelegatingSpliterator.OfFloat((Supplier<? extends Spliterator.OfFloat>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Float> spliterator, Sink<Float> sink) {
        Spliterator.OfFloat spl = adapter(spliterator);
        FloatConsumer consumer = adapter(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Float> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Float[]> generator) {
        return Nodes.floatBuilder(exactSizeIfKnown);
    }

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.FLOAT_VALUE;
    }

    @Override
    public Spliterator.OfFloat spliterator() {
        return adapter(super.spliterator());
    }


    @Override
    public FloatStream unordered() {
        if (isOrdered()) {
            return new StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, StreamOpFlag.NOT_ORDERED) {
                @Override
                public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                    return sink;
                }
            };
        } else {
            return this;
        }
    }

    @Override
    public FloatStream map(FloatUnaryOperator mapper) {
        return new StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<Float>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.applyAsFloat(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(FloatFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Float, U>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedFloat<U>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(FloatToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Float>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedFloat<Byte>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(FloatToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Float>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedFloat<Character>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(FloatToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedFloat<Integer>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(FloatToLongFunction mapper) {
        return new LongPipeline.StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedFloat<Long>(sink) {
                    @Override
                    public void accept(float value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream mapToDouble(FloatToDoubleFunction mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public FloatStream flatMap(FloatFunction<? extends FloatStream> mapper) {
        return new StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<Float>(sink) {
                    @Override
                    public void accept(float value) {
                        Optional.ofNullable(mapper.apply(value))
                                .ifPresent(s -> s.sequential().forEach(downstream::accept));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream filter(FloatPredicate predicate) {
        return new StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<Float>(sink) {
                    @Override
                    public void accept(float value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public FloatStream distinct() {
        return boxed().distinct().mapToFloat(v -> v);
    }

    @Override
    public FloatStream sorted() {
        return SortedOps.makeFLoat(this);
    }

    @Override
    public FloatStream peek(FloatConsumer action) {
        return new StatelessOp<Float>(this, StreamShape.FLOAT_VALUE, 0) {
            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<Float>(sink) {
                    @Override
                    public void accept(float value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public FloatStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeFloat(this, 0, maxSize);
    }

    @Override
    public FloatStream skip(long n) {
        positive(n);
        if (n == 0) {
            return this;
        }
        return SliceOps.makeFloat(this, n, -1);
    }

    @Override
    public void forEach(FloatConsumer action) {
        evaluate(ForeachOps.makeFloat(action, false));
    }

    @Override
    public void forEachOrdered(FloatConsumer action) {
        evaluate(ForeachOps.makeFloat(action, true));
    }

    @Override
    public float[] toArray() {
        return Nodes.flattenFloat((Node.OfFloat) evaluateToArrayNode(Float[]::new)).asPrimitiveArray();
    }

    @Override
    public float reduce(float identity, FloatBinaryOperator op) {
        return evaluate(ReduceOps.makeFloat(identity, op));
    }

    @Override
    public OptionalFloat reduce(FloatBinaryOperator op) {
        return evaluate(ReduceOps.makeFloat(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjFloatConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeFloat(supplier, accumulator, toCombiner(combiner)));
    }

    @Override
    public float sum() {
        return reduce(0, Float::sum);
    }

    @Override
    public OptionalFloat min() {
        return reduce(Float::min);
    }

    @Override
    public OptionalFloat max() {
        return reduce(Float::max);
    }

    @Override
    public long count() {
        return mapToLong(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {
        Supplier<long[]> supplier = averageSupplier();
        BiConsumer<long[], long[]> consumer = averageCombiner();
        ObjFloatConsumer<long[]> acc = (values, value) -> {
            values[0]++;
            values[1] += value;
        };
        ;
        long[] avg = collect(supplier, acc, consumer);
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[0] / avg[1])
                : OptionalDouble.empty();
    }

    @Override
    public FloatSummaryStatistics summaryStatistics() {
        return collect(FloatSummaryStatistics::new, FloatSummaryStatistics::accept, FloatSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(FloatPredicate predicate) {
        return evaluate(MatchOps.makeFloat(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalFloat findFirst() {
        return evaluate(FindOps.makeFloat(true));
    }

    @Override
    public OptionalFloat findAny() {
        return evaluate(FindOps.makeFloat(false));
    }

    @Override
    public Stream<Float> boxed() {
        return mapToObj(Float::valueOf);
    }

    @Override
    public PrimitiveIterator.OfFloat iterator() {
        return Spliterators.iterator(spliterator());
    }

    static class Head<In> extends FloatPipeline<In> {

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
        public Sink<In> opWrapSink(int flags, Sink<Float> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(FloatConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(FloatConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatelessOp<T> extends FloatPipeline<T> {

        StatelessOp(AbstractPipeline<?, T, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<T> extends FloatPipeline<T> {

        public StatefulOp(AbstractPipeline<?, T, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Float> opEvaluateParallel(PipelineHelper<Float> helper, Spliterator<P_IN> spliterator, IntFunction<Float[]> generator);
    }
}
