package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.exception.NotImplementedException;
import io.github.amrjlg.function.ObjShortConsumer;
import io.github.amrjlg.function.ShortBinaryOperator;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.function.ShortFunction;
import io.github.amrjlg.function.ShortPredicate;
import io.github.amrjlg.function.ShortToByteFunction;
import io.github.amrjlg.function.ShortToCharFunction;
import io.github.amrjlg.function.ShortToDoubleFunction;
import io.github.amrjlg.function.ShortToFloatFunction;
import io.github.amrjlg.function.ShortToIntFunction;
import io.github.amrjlg.function.ShortToLongFunction;
import io.github.amrjlg.function.ShortUnaryOperator;
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
import io.github.amrjlg.util.OptionalShort;
import io.github.amrjlg.util.ShortSummaryStatistics;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class ShortPipeline<Input> extends AbstractPipeline<Input, Short, ShortStream>
        implements ShortStream {
    public ShortPipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public ShortPipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    ShortPipeline(AbstractPipeline<?, Input, ?> previousStage, int opFlags) {
        super(previousStage, opFlags);
    }

    @Override
    public ShortStream map(ShortUnaryOperator mapper) {
        return new StatelessOp<Short>(this, StreamShape.SHORT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<Short>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsShort(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(ShortFunction<? extends U> mapper) {
        return new ReferencePipeline.StatelessOp<Short, U>(this, StreamShape.SHORT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedShort<U>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public ByteStream mapToByte(ShortToByteFunction mapper) {
        return new BytePipeline.StateLessOp<Short>(this, StreamShape.SHORT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedShort<Byte>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(ShortToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Short>(this, StreamShape.SHORT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedShort<Character>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(ShortToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Short>(this, StreamShape.SHORT_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedShort<Integer>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(ShortToLongFunction mapper) {
        return new LongPipeline.StatelessOp<Short>(this,StreamShape.SHORT_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedShort<Long>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream mapToFloat(ShortToFloatFunction mapper) {
        return new FloatPipeline.StatelessOp<Short>(this,StreamShape.SHORT_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedShort<Float>(sink) {
                    @Override
                    public void accept(short value) {
                        downstream.accept(mapper.applyAsFloat(value));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream mapToDouble(ShortToDoubleFunction mapper) {
        // todo impl
        throw new NotImplementedException();
    }

    @Override
    public ShortStream flatMap(ShortFunction<? extends ShortStream> mapper) {
        return new StatelessOp<Short>(this, StreamShape.SHORT_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<Short>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(short value) {
                        try (ShortStream stream = mapper.apply(value)) {
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
    public ShortStream filter(ShortPredicate predicate) {
        return new StatelessOp<Short>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<Short>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }
                    @Override
                    public void accept(short value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public ShortStream distinct() {
        return boxed().distinct().mapToShort(v -> v);
    }

    @Override
    public ShortStream sorted() {
        return SortedOps.makeShort(this);
    }

    @Override
    public ShortStream peek(ShortConsumer action) {
        return new StatelessOp<Short>(this, StreamShape.SHORT_VALUE, 0) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<Short>(sink) {
                    @Override
                    public void accept(short value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public ShortStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeShort(this, 0, maxSize);
    }

    @Override
    public ShortStream skip(long n) {
        positive(n);
        if (n == 0) {
            return this;
        }
        return SliceOps.makeShort(this, n, -1);
    }

    @Override
    public void forEach(ShortConsumer action) {
        evaluate(ForeachOps.makeShort(action, false));
    }

    @Override
    public void forEachOrdered(ShortConsumer action) {
        ForeachOps.makeShort(action, true);
    }

    @Override
    public short[] toArray() {
        return Nodes.flattenShort((Node.OfShort) evaluateToArrayNode(Short[]::new)).asPrimitiveArray();
    }

    @Override
    public short reduce(short identity, ShortBinaryOperator op) {
        return evaluate(ReduceOps.makeShort(identity, op));
    }

    @Override
    public OptionalShort reduce(ShortBinaryOperator op) {
        return evaluate(ReduceOps.makeShort(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjShortConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        BinaryOperator<R> combine = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return evaluate(ReduceOps.makeShort(supplier, accumulator, combine));
    }

    @Override
    public short sum() {
        return reduce((short) 0, (l, r) -> (short) (l + r));
    }

    @Override
    public OptionalShort min() {
        return reduce((l, r) -> l > r ? r : l);
    }

    @Override
    public OptionalShort max() {
        return reduce((l, r) -> l < r ? r : l);
    }

    @Override
    public long count() {
        return mapToLong(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {

        ObjShortConsumer<long[]> consumer = ((longs, value) -> {
            longs[0]++;
            longs[1] += value;
        });

        long[] avg = collect(averageSupplier(), consumer, averageCombiner());
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[1] / avg[0])
                : OptionalDouble.empty();
    }

    @Override
    public ShortSummaryStatistics summaryStatistics() {
        return collect(ShortSummaryStatistics::new, ShortSummaryStatistics::accept, ShortSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(ShortPredicate predicate) {
        return evaluate(MatchOps.makeShort(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalShort findFirst() {
        return evaluate(FindOps.makeShort(true));
    }

    @Override
    public OptionalShort findAny() {
        return evaluate(FindOps.makeShort(false));
    }

    @Override
    public Stream<Short> boxed() {
        return mapToObj(Short::valueOf);
    }

    @Override
    public PrimitiveIterator.OfShort iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public ShortStream unordered() {
        if (!isOrdered()) {
            return this;
        }
        return new StatelessOp<Short>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return sink;
            }
        };
    }

    @Override
    <P_IN> Node<Short> evaluateToNode(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Short[]> generator) {
        return Nodes.collectShort(helper, spliterator, flattenTree);
    }

    @Override
    <P_IN> Spliterator<Short> wrap(PipelineHelper<Short> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfShort<>(ph, supplier, isParallel);
    }

    @Override
    Spliterator<Short> lazySpliterator(Supplier<? extends Spliterator<Short>> supplier) {
        return new DelegatingSpliterator.OfShort((Supplier<? extends Spliterator.OfShort>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Short> spliterator, Sink<Short> sink) {
        Spliterator.OfShort spl = toShort(spliterator);
        ShortConsumer consumer = toShort(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Short> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Short[]> generator) {
        return Nodes.shortBuilder(exactSizeIfKnown);
    }


    @Override
    StreamShape getOutputShape() {
        return StreamShape.SHORT_VALUE;
    }


    @Override
    public Spliterator.OfShort spliterator() {
        return toShort(super.spliterator());
    }

    protected static Spliterator.OfShort toShort(Spliterator<Short> spliterator) {
        if (spliterator instanceof Spliterator.OfShort) {
            return (Spliterator.OfShort) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    protected static ShortConsumer toShort(Sink<Short> sink) {
        if (sink instanceof ShortConsumer) {
            return (ShortConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }

    static class Head<In> extends ShortPipeline<In> {

        public Head(Supplier<? extends Spliterator<Short>> sourceSupplier, int sourceFlag, boolean parallel) {
            super(sourceSupplier, sourceFlag, parallel);
        }

        public Head(Spliterator<Short> source, int sourceFlag, boolean parallel) {
            super(source, sourceFlag, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sink<In> opWrapSink(int flags, Sink<Short> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(ShortConsumer action) {
            if (isParallel()) {
                super.forEach(action);
            } else {
                toShort(sourceStageSpliterator()).forEachRemaining(action);
            }
        }

        @Override
        public void forEachOrdered(ShortConsumer action) {
            if (isParallel()) {
                super.forEachOrdered(action);
            } else {
                toShort(sourceStageSpliterator()).forEachRemaining(action);
            }
        }
    }

    public static abstract class StatelessOp<In> extends ShortPipeline<In> {

        public StatelessOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<In> extends ShortPipeline<In> {
        public StatefulOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Short> opEvaluateParallel(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator, IntFunction<Short[]> generator);


    }
}
