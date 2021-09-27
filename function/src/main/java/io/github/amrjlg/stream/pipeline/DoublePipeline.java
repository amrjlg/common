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

import io.github.amrjlg.function.DoubleToByteFunction;
import io.github.amrjlg.function.DoubleToCharFunction;
import io.github.amrjlg.function.DoubleToFloatFunction;
import io.github.amrjlg.function.DoubleToShortFunction;
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

import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author amrjlg
 * @date 2021-09-26 18:03
 **/
public abstract class DoublePipeline<T> extends AbstractPipeline<T, Double, DoubleStream>
        implements DoubleStream {
    public DoublePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public DoublePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    DoublePipeline(AbstractPipeline<?, T, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    public static Spliterator.OfDouble adapter(Spliterator<Double> spliterator) {
        if (spliterator instanceof Spliterator.OfDouble) {
            return (Spliterator.OfDouble) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    public static DoubleConsumer adapter(Sink<Double> sink) {
        if (sink instanceof DoubleConsumer) {
            return (DoubleConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    <P_IN> Node<Double> evaluateToNode(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Double[]> generator) {
        return Nodes.collectDouble(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Double> wrap(PipelineHelper<Double> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfDouble<>(ph, supplier, isParallel);
    }

    @Override
    Spliterator<Double> lazySpliterator(Supplier<? extends Spliterator<Double>> supplier) {
        return new DelegatingSpliterator.OfDouble((Supplier<? extends Spliterator.OfDouble>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Double> spliterator, Sink<Double> sink) {
        Spliterator.OfDouble spl = adapter(spliterator);
        DoubleConsumer consumer = adapter(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Double> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Double[]> generator) {
        return Nodes.doubleBuilder(exactSizeIfKnown);
    }

    @Override
    StreamShape getOutputShape() {
        return StreamShape.DOUBLE_VALUE;
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        return adapter(super.spliterator());
    }

    @Override
    public DoubleStream map(DoubleUnaryOperator mapper) {
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsDouble(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Double, U>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedDouble<U>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(DoubleToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedDouble<Byte>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(DoubleToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedDouble<Character>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream mapToShort(DoubleToShortFunction mapper) {
        return new ShortPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedDouble<Short>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsShort(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(DoubleToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedDouble<Integer>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(DoubleToLongFunction mapper) {
        return new LongPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedDouble<Long>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream mapToFloat(DoubleToFloatFunction mapper) {
        return new FloatPipeline.StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedDouble<Float>(sink) {
                    @Override
                    public void accept(double value) {
                        downstream.accept(mapper.applyAsFloat(value));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    @Override
                    public void accept(double value) {
                        Optional.ofNullable(mapper.apply(value))
                                .ifPresent(s -> s.sequential().forEach(downstream::accept));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream filter(DoublePredicate predicate) {
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }
                    @Override
                    public void accept(double value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream distinct() {
        return boxed().distinct().mapToDouble(Double::doubleValue);
    }

    @Override
    public DoubleStream sorted() {
        return SortedOps.makeDouble(this);
    }

    @Override
    public DoubleStream peek(DoubleConsumer action) {
        return new StatelessOp<Double>(this, StreamShape.DOUBLE_VALUE, 0) {
            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    @Override
                    public void accept(double value) {
                        action.andThen(downstream::accept)
                                .accept(value);

                    }
                };
            }
        };
    }

    @Override
    public DoubleStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeDouble(this,0,maxSize);
    }

    @Override
    public DoubleStream skip(long n) {
        positive(n);
        if (n == 0){
            return this;
        }
        return SliceOps.makeDouble(this,n,-1);
    }

    @Override
    public void forEach(DoubleConsumer action) {
        evaluate(ForeachOps.makeDouble(action,false));
    }

    @Override
    public void forEachOrdered(DoubleConsumer action) {
        evaluate(ForeachOps.makeDouble(action,true));
    }

    @Override
    public double[] toArray() {
        return Nodes.flattenDouble((Node.OfDouble)evaluateToArrayNode(Double[]::new)).asPrimitiveArray();
    }

    @Override
    public double reduce(double identity, DoubleBinaryOperator op) {
        return evaluate(ReduceOps.makeDouble(identity,op));
    }

    @Override
    public OptionalDouble reduce(DoubleBinaryOperator op) {
        return evaluate(ReduceOps.makeDouble(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeDouble(supplier,accumulator,toCombiner(combiner)));
    }

    @Override
    public double sum() {
        return reduce(0,Double::sum);
    }

    @Override
    public OptionalDouble min() {
        return reduce((Double::min));
    }

    @Override
    public OptionalDouble max() {
        return reduce(Double::max);
    }

    @Override
    public long count() {
        return mapToLong(v->1L).sum();
    }

    @Override
    public OptionalDouble average() {
        ObjDoubleConsumer<double[]> consumer = (values,value)->{
            values[0]+=1;
            values[1]=value;
        };
        double[] avg = collect(()->new double[2],consumer,(l,r)->{
            l[0]+=r[0];
            l[1]+=r[1];

        });
        return avg[0] > 0
                ? OptionalDouble.of(avg[1] / avg[0])
                : OptionalDouble.empty();
    }

    @Override
    public DoubleSummaryStatistics summaryStatistics() {
        return collect(DoubleSummaryStatistics::new,DoubleSummaryStatistics::accept,DoubleSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(DoublePredicate predicate) {
        return evaluate(MatchOps.makeDouble(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalDouble findFirst() {
        return evaluate(FindOps.makeDouble(true));
    }

    @Override
    public OptionalDouble findAny() {
        return evaluate(FindOps.makeDouble(false));
    }

    @Override
    public Stream<Double> boxed() {
        return mapToObj(Double::valueOf);
    }

    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public DoubleStream unordered() {
        if (isOrdered()){
            return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,StreamOpFlag.NOT_ORDERED) {
                @Override
                public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                    return sink;
                }
            };
        }else {
            return this;
        }

    }

    static class Head<T> extends DoublePipeline<T> {

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
        public Sink<T> opWrapSink(int flags, Sink<Double> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(DoubleConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(DoubleConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                adapter(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatelessOp<T> extends DoublePipeline<T> {

        StatelessOp(AbstractPipeline<?, T, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<T> extends DoublePipeline<T> {

        public StatefulOp(AbstractPipeline<?, T, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract  <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator);
    }
}
