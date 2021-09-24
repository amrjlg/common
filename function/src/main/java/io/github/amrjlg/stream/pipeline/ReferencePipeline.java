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
import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ToByteFunction;
import io.github.amrjlg.function.ToCharFunction;
import io.github.amrjlg.function.ToFloatFunction;
import io.github.amrjlg.function.ToShortFunction;
import io.github.amrjlg.stream.spliterator.DelegatingSpliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;
import io.github.amrjlg.stream.spliterator.WrappingSpliterator;
import io.github.amrjlg.stream.operations.DistinctOps;
import io.github.amrjlg.stream.operations.FindOps;
import io.github.amrjlg.stream.operations.ForeachOps;
import io.github.amrjlg.stream.operations.MatchKind;
import io.github.amrjlg.stream.operations.MatchOps;
import io.github.amrjlg.stream.operations.ReduceOps;
import io.github.amrjlg.stream.operations.SliceOps;
import io.github.amrjlg.stream.operations.SortedOps;
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
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.NodeBuilder;
import io.github.amrjlg.stream.node.Nodes;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;


/**
 * @author amrjlg
 **/
public abstract class ReferencePipeline<Input, Output>
        extends AbstractPipeline<Input, Output, Stream<Output>>
        implements Stream<Output> {

    public ReferencePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public ReferencePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    ReferencePipeline(AbstractPipeline<?, Input, ?> previousStage, int opFlags) {
        super(previousStage, opFlags);
    }

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.REFERENCE;
    }

    @Override
    <P_IN> Node<Output> evaluateToNode(PipelineHelper<Output> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Output[]> generator) {
        return Nodes.collect(helper, spliterator, flattenTree, generator);
    }

    @Override
    <P_IN> Spliterator<Output> wrap(PipelineHelper<Output> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfRef<>(ph, supplier, isParallel);
    }

    @Override
    Spliterator<Output> lazySpliterator(Supplier<? extends Spliterator<Output>> supplier) {
        return new DelegatingSpliterator<>(supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Output> spliterator, Sink<Output> sink) {
        while (!sink.cancellationRequested() && spliterator.tryAdvance(sink)) {

        }
    }

    @Override
    public NodeBuilder<Output> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Output[]> generator) {
        return Nodes.builder(exactSizeIfKnown, generator);
    }

    @Override
    public Iterator<Output> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Stream<Output> unordered() {
        if (!isOrdered()) {
            return this;
        }

        return new StatelessOp<Output, Output>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Output> sink) {
                return sink;
            }
        };
    }

    @Override
    public Stream<Output> filter(Predicate<? super Output> predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<Output, Output>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Output> sink) {
                return new Sink.ChainedReference<Output, Output>(sink) {

                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(Output output) {
                        if (predicate.test(output)) {
                            downstream.accept(output);
                        }
                    }
                };
            }
        };
    }

    @Override
    public <R> Stream<R> map(Function<? super Output, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<Output, R>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<Output, R>(sink) {

                    @Override
                    public void accept(Output output) {
                        downstream.accept(mapper.apply(output));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(ToByteFunction<? super Output> mapper) {
        return new BytePipeline.StateLessOp<Output>(this, StreamShape.REFERENCE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {

            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedReference<Output, Byte>(sink) {
                    @Override
                    public void accept(Output output) {
                        downstream.accept(mapper.applyAsByte(output));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream mapToShort(ToShortFunction<? super Output> mapper) {
        return new ShortPipeline.StatelessOp<Output>(this, StreamShape.REFERENCE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedReference<Output, Short>(sink) {
                    @Override
                    public void accept(Output output) {
                        downstream.accept(mapper.applyAsShort(output));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(ToCharFunction<? super Output> mapper) {
        return new CharPipeline.StateLessOp<Output>(this, StreamShape.REFERENCE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedReference<Output, Character>(sink) {
                    @Override
                    public void accept(Output output) {
                        downstream.accept(mapper.applyAsChar(output));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super Output> mapper) {
        return new IntPipeline.StatelessOp<Output>(this, StreamShape.REFERENCE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedReference<Output, Integer>(sink) {
                    @Override
                    public void accept(Output output) {
                        downstream.accept(mapper.applyAsInt(output));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super Output> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public FloatStream mapToFloat(ToFloatFunction<? super Output> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super Output> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super Output, ? extends Stream<? extends R>> mapper) {
        return new StatelessOp<Output, R>(this, StreamShape.REFERENCE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<R> sink) {
                return new Sink.ChainedReference<Output, R>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(Output output) {
                        Stream<? extends R> stream = mapper.apply(output);
                        if (Objects.nonNull(stream)) {
                            stream.sequential().forEach(downstream);
                        }
                    }
                };
            }
        };
    }

    @Override
    public ByteStream flatMapToByte(Function<? super Output, ? extends ByteStream> mapper) {
        return new BytePipeline.StateLessOp<Output>(this, StreamShape.REFERENCE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedReference<Output, Byte>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(Output output) {
                        Optional.ofNullable(mapper.apply(output))
                                .ifPresent(s -> s.sequential().forEach((ByteConsumer) downstream::accept));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream flatMapToShort(Function<? super Output, ? extends ShortStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public CharStream flatMapToChar(Function<? super Output, ? extends CharStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public IntStream flatMapToInt(Function<? super Output, ? extends IntStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public LongStream flatMapToLong(Function<? super Output, ? extends LongStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public FloatStream flatMapToFloat(Function<? super Output, ? extends FloatStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super Output, ? extends DoubleStream> mapper) {
        // TODO IMPL
        throw new NotImplementedException();
    }

    @Override
    public Stream<Output> peek(Consumer<? super Output> action) {
        return new StatelessOp<Output, Output>(this, StreamShape.REFERENCE, 0) {
            @Override
            public Sink<Output> opWrapSink(int flags, Sink<Output> sink) {
                return new Sink.ChainedReference<Output, Output>(sink) {

                    @Override
                    public void accept(Output output) {
                        action.accept(output);
                        downstream.accept(output);
                    }
                };
            }
        };
    }

    @Override
    public Stream<Output> distinct() {
        return DistinctOps.makeRef(this);
    }

    @Override
    public Stream<Output> sorted() {
        return SortedOps.makeRef(this);
    }

    @Override
    public Stream<Output> sorted(Comparator<? super Output> comparator) {
        return SortedOps.makeRef(this, comparator);
    }

    @Override
    public Stream<Output> limit(long maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }
        return SliceOps.makeRef(this, 0, maxSize);
    }

    @Override
    public Stream<Output> skip(long count) {
        if (count < 0) {
            throw new IllegalArgumentException(Long.toString(count));
        }
        if (count == 0) {
            return this;
        }
        return SliceOps.makeRef(this, count, -1);
    }

    @Override
    public void forEach(Consumer<? super Output> action) {
        evaluate(ForeachOps.makeRef(action, false));
    }

    @Override
    public void forEachOrdered(Consumer<? super Output> action) {
        evaluate(ForeachOps.makeRef(action, true));
    }

    @Override
    public Object[] toArray() {
        return toArray(Object[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return (A[]) Nodes.flatten(evaluateToArrayNode((IntFunction) generator), generator).asArray((IntFunction) generator);
    }

    @Override
    public Output reduce(Output identity, BinaryOperator<Output> accumulator) {
        return evaluate(ReduceOps.makeRef(identity, accumulator, accumulator));
    }

    @Override
    public Optional<Output> reduce(BinaryOperator<Output> accumulator) {
        return evaluate(ReduceOps.makeRef(accumulator));
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super Output, U> accumulator, BinaryOperator<U> combiner) {
        return evaluate(ReduceOps.makeRef(identity, accumulator, combiner));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Output> accumulator, BiConsumer<R, R> combiner) {
        return evaluate(ReduceOps.makeRef(supplier, accumulator, combiner));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, A> R collect(Collector<? super Output, A, R> collector) {
        A container;
        Set<Collector.Characteristics> characteristics = collector.characteristics();
        if (isParallel()
                && characteristics.contains(Collector.Characteristics.CONCURRENT)
                && (!isOrdered() || characteristics.contains(Collector.Characteristics.UNORDERED))
        ) {
            container = collector.supplier().get();
            BiConsumer<A, ? super Output> accumulator = collector.accumulator();
            forEach(u -> accumulator.accept(container, u));
        } else {
            container = evaluate(ReduceOps.makeRef(collector));
        }

        if (characteristics.contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return (R) container;
        } else {
            return collector.finisher().apply(container);
        }

    }

    @Override
    public Optional<Output> min(Comparator<? super Output> comparator) {
        return reduce(BinaryOperator.minBy(comparator));
    }

    @Override
    public Optional<Output> max(Comparator<? super Output> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }

    @Override
    public long count() {
        return mapToLong(ele -> 1L).sum();
    }

    @Override
    public boolean anyMatch(Predicate<? super Output> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(Predicate<? super Output> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(Predicate<? super Output> predicate) {
        return evaluate(MatchOps.makeRef(predicate, MatchKind.NONE));
    }

    @Override
    public Optional<Output> findFirst() {
        return evaluate(FindOps.makeRef(true));
    }

    @Override
    public Optional<Output> findAny() {
        return evaluate(FindOps.makeRef(false));
    }


    public abstract static class StatelessOp<In, Out> extends ReferencePipeline<In, Out> {
        public StatelessOp(AbstractPipeline<?, In, ?> inputOutputReferencePipeline, StreamShape shape, int opFlags) {
            super(inputOutputReferencePipeline, opFlags);
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public abstract static class StatefulOp<In, Out> extends ReferencePipeline<In, Out> {

        public StatefulOp(AbstractPipeline<?, In, ?> upstream,
                          StreamShape inputShape,
                          int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == inputShape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        public abstract <P_IN> Node<Out> opEvaluateParallel(PipelineHelper<Out> helper, Spliterator<P_IN> spliterator, IntFunction<Out[]> generator);
    }

    static class Head<In, Out> extends ReferencePipeline<In, Out> {
        Head(Supplier<? extends Spliterator<?>> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        Head(Spliterator<?> source,
             int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final Sink<In> opWrapSink(int flags, Sink<Out> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(Consumer<? super Out> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(Consumer<? super Out> action) {
            if (!isParallel()) {
                sourceStageSpliterator().forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }
    }
}
