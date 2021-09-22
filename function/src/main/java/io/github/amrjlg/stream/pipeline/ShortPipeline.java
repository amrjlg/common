package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.exception.NotImplementedException;
import io.github.amrjlg.function.ObjShortConsumer;
import io.github.amrjlg.function.ShortBinaryOperator;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.function.ShortFunction;
import io.github.amrjlg.function.ShortPredicate;
import io.github.amrjlg.function.ShortToByteFunction;
import io.github.amrjlg.function.ShortToCharFunction;
import io.github.amrjlg.function.ShortToFloatFunction;
import io.github.amrjlg.function.ShortToIntFunction;
import io.github.amrjlg.function.ShortToLongFunction;
import io.github.amrjlg.function.ShortUnaryOperator;
import io.github.amrjlg.stream.ByteStream;
import io.github.amrjlg.stream.CharStream;
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
import io.github.amrjlg.stream.operations.ForeachOps;
import io.github.amrjlg.stream.operations.SliceOps;
import io.github.amrjlg.stream.operations.SortedOps;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.util.OptionalShort;
import io.github.amrjlg.util.ShortSummaryStatistics;

import java.util.Objects;
import java.util.function.BiConsumer;
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
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return new ReferencePipeline.StatelessOp<>(this, StreamShape.SHORT_VALUE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return new BytePipeline.StateLessOp<>(this, StreamShape.SHORT_VALUE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return new CharPipeline.StateLessOp<>(this, StreamShape.SHORT_VALUE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        // todo impl
        throw new NotImplementedException();
    }

    @Override
    public LongStream mapToLong(ShortToLongFunction mapper) {
        // todo impl
        throw new NotImplementedException();
    }

    @Override
    public FloatStream mapToFloat(ShortToFloatFunction mapper) {
        // todo impl
        throw new NotImplementedException();
    }

    @Override
    public ShortStream flatMap(ShortFunction<? extends ShortStream> mapper) {
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, NOT_SORTED_AND_NOT_DISTINCT) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return new StatelessOp<>(this, StreamShape.SHORT_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return new StatelessOp<>(this,StreamShape.SHORT_VALUE,0) {
            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<>(sink) {
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
        return SliceOps.makeShort(this,0,maxSize);
    }

    @Override
    public ShortStream skip(long n) {
        positive(n);
        if (n==0){
            return this;
        }
        return SliceOps.makeShort(this,n,-1);
    }

    @Override
    public void forEach(ShortConsumer action) {
        evaluate(ForeachOps.makeShort(action,false));
    }

    @Override
    public void forEachOrdered(ShortConsumer action) {
        ForeachOps.makeShort(action,true);
    }

    @Override
    public short[] toArray() {
        return Nodes.flattenShort((Node.OfShort) evaluateToArrayNode(Short[]::new)).asPrimitiveArray();
    }

    @Override
    public short reduce(short identity, ShortBinaryOperator op) {
        return 0;
    }

    @Override
    public OptionalShort reduce(ShortBinaryOperator op) {
        return null;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjShortConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        return null;
    }

    @Override
    public short sum() {
        return 0;
    }

    @Override
    public OptionalShort min() {
        return null;
    }

    @Override
    public OptionalShort max() {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public OptionalShort average() {
        return null;
    }

    @Override
    public ShortSummaryStatistics summaryStatistics() {
        return null;
    }

    @Override
    public boolean anyMatch(ShortPredicate predicate) {
        return false;
    }

    @Override
    public boolean allMatch(ShortPredicate predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(ShortPredicate predicate) {
        return false;
    }

    @Override
    public OptionalShort findFirst() {
        return null;
    }

    @Override
    public OptionalShort findAny() {
        return null;
    }

    @Override
    public Stream<Short> boxed() {
        return null;
    }

    @Override
    public PrimitiveIterator.OfShort iterator() {
        return null;
    }

    @Override
    public ShortStream unordered() {
        return null;
    }

    @Override
    <P_IN> Node<Short> evaluateToNode(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Short[]> generator) {
        return null;
    }

    @Override
    <P_IN> Spliterator<Short> wrap(PipelineHelper<Short> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return null;
    }

    @Override
    Spliterator<Short> lazySpliterator(Supplier<? extends Spliterator<Short>> supplier) {
        return null;
    }

    @Override
    void forEachWithCancel(Spliterator<Short> spliterator, Sink<Short> sink) {

    }

    @Override
    public NodeBuilder<Short> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Short[]> generator) {
        return null;
    }


    @Override
    StreamShape getOutputShape() {
        return null;
    }


    @Override
    public Spliterator.OfShort spliterator() {
        return null;
    }

    protected static Spliterator.OfShort toShort(Spliterator<Short> spliterator) {
        if (spliterator instanceof Spliterator.OfShort) {
            return (Spliterator.OfShort) spliterator;
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
