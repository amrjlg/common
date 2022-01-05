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
import io.github.amrjlg.function.ByteBinaryOperator;
import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ByteFunction;
import io.github.amrjlg.function.BytePredicate;
import io.github.amrjlg.function.ByteToCharFunction;
import io.github.amrjlg.function.ByteToDoubleFunction;
import io.github.amrjlg.function.ByteToFloatFunction;
import io.github.amrjlg.function.ByteToIntFunction;
import io.github.amrjlg.function.ByteToLongFunction;
import io.github.amrjlg.function.ByteToShortFunction;
import io.github.amrjlg.function.ByteUnaryOperator;
import io.github.amrjlg.function.ObjByteConsumer;
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
import io.github.amrjlg.util.ByteSummaryStatistics;
import io.github.amrjlg.util.OptionalByte;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public abstract class BytePipeline<Input>
        extends AbstractPipeline<Input, Byte, ByteStream>
        implements ByteStream {

    public BytePipeline(Supplier<? extends Spliterator<?>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    public BytePipeline(Spliterator<?> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    BytePipeline(AbstractPipeline<?, Input, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static Spliterator.OfByte toByte(Spliterator<Byte> spliterator) {
        if (spliterator instanceof Spliterator.OfByte) {
            return (Spliterator.OfByte) spliterator;
        }
        throw new UnsupportedOperationException();
    }

    private static ByteConsumer toByte(Sink<Byte> sink) {
        if (sink instanceof ByteConsumer) {
            return (ByteConsumer) sink;
        }
        throw new UnsupportedOperationException();
    }


    @Override
    <P_IN> Node<Byte> evaluateToNode(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Byte[]> generator) {
        return Nodes.collectByte(helper, spliterator, flattenTree);
    }


    @Override
    <P_IN> Spliterator<Byte> wrap(PipelineHelper<Byte> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new WrappingSpliterator.OfByte<>(ph, supplier, isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    Spliterator<Byte> lazySpliterator(Supplier<? extends Spliterator<Byte>> supplier) {
        return new DelegatingSpliterator.OfByte((Supplier<? extends Spliterator.OfByte>) supplier);
    }

    @Override
    void forEachWithCancel(Spliterator<Byte> spliterator, Sink<Byte> sink) {
        Spliterator.OfByte spl = toByte(spliterator);
        ByteConsumer consumer = toByte(sink);
        while (!sink.cancellationRequested() && spl.tryAdvance(consumer)) {

        }
    }

    @Override
    public NodeBuilder<Byte> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Byte[]> generator) {
        return Nodes.byteBuilder(exactSizeIfKnown);
    }


    @Override
    public ByteStream map(ByteUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<Byte>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsByte(value));
                    }
                };
            }
        };
    }

    @Override
    public <U> Stream<U> mapToObj(ByteFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return new ReferencePipeline.StatelessOp<Byte, U>(this, StreamShape.BYTE_VALUE, MAP_OP_FLAGS) {

            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedByte<U>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.apply(value));
                    }
                };
            }
        };
    }

    @Override
    public CharStream mapToChar(ByteToCharFunction mapper) {
        return new CharPipeline.StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedByte<Character>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsChar(value));
                    }
                };
            }
        };
    }

    @Override
    public ShortStream mapToShort(ByteToShortFunction mapper) {
        return new ShortPipeline.StatelessOp<Byte>(this, StreamShape.BYTE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedByte<Short>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsShort(value));
                    }
                };
            }
        };
    }

    @Override
    public IntStream mapToInt(ByteToIntFunction mapper) {
        return new IntPipeline.StatelessOp<Byte>(this, StreamShape.BYTE_VALUE, MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedByte<Integer>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsInt(value));
                    }
                };
            }
        };
    }

    @Override
    public LongStream mapToLong(ByteToLongFunction mapper) {
        return new LongPipeline.StatelessOp<Byte>(this,StreamShape.BYTE_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedByte<Long>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsLong(value));
                    }
                };
            }
        };
    }

    @Override
    public FloatStream mapToFloat(ByteToFloatFunction mapper) {
        return new FloatPipeline.StatelessOp<Byte>(this,StreamShape.BYTE_VALUE,MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedByte<Float>(sink) {
                    @Override
                    public void accept(byte value) {
                        downstream.accept(mapper.applyAsFloat(value));
                    }
                };
            }
        };
    }

    @Override
    public DoubleStream mapToDouble(ByteToDoubleFunction mapper) {
        // TODO impl
        throw new NotImplementedException();
    }

    @Override
    public ByteStream flatMap(ByteFunction<? extends ByteStream> mapper) {
        return new StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, FLAT_MAP_OP_FLAGS) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<Byte>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(byte value) {
                        try (ByteStream stream = mapper.apply(value)) {
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
    public ByteStream filter(BytePredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, StreamOpFlag.NOT_SIZED) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<Byte>(sink) {
                    @Override
                    public void begin(long size) {
                        downstream.begin(-1);
                    }
                    @Override
                    public void accept(byte value) {
                        if (predicate.test(value)) {
                            downstream.accept(value);
                        }
                    }
                };
            }
        };
    }

    @Override
    public ByteStream distinct() {
        return boxed().distinct().mapToByte(v -> v);
    }

    @Override
    public ByteStream sorted() {
        return SortedOps.makeByte(this);
    }

    @Override
    public ByteStream peek(ByteConsumer action) {
        Objects.requireNonNull(action);
        return new StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, 0) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<Byte>(sink) {
                    @Override
                    public void accept(byte value) {
                        action.accept(value);
                        downstream.accept(value);
                    }
                };
            }
        };
    }

    @Override
    public ByteStream limit(long maxSize) {
        positive(maxSize);
        return SliceOps.makeByte(this, 0, maxSize);
    }

    @Override
    public ByteStream skip(long n) {
        positive(n);
        if (n == 0) {
            return this;
        }
        return SliceOps.makeByte(this, n, -1);
    }

    @Override
    public void forEach(ByteConsumer action) {
        evaluate(ForeachOps.makeByte(action, false));
    }

    @Override
    public void forEachOrdered(ByteConsumer action) {
        evaluate(ForeachOps.makeByte(action, true));
    }

    @Override
    public byte[] toArray() {
        return Nodes.flattenByte((Node.OfByte) evaluateToArrayNode(Byte[]::new))
                .asPrimitiveArray();
    }

    @Override
    public byte reduce(byte identity, ByteBinaryOperator op) {
        return evaluate(ReduceOps.makeByte(identity, op));
    }

    @Override
    public OptionalByte reduce(ByteBinaryOperator op) {
        return evaluate(ReduceOps.makeByte(op));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjByteConsumer<R> accumulator, BiConsumer<R, R> combiner) {

        BinaryOperator<R> operator = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return evaluate(ReduceOps.makeByte(supplier, accumulator, operator));
    }

    @Override
    public int sum() {
        return reduce((byte) 0, (l, r) -> (byte) (l + r));
    }

    @Override
    public OptionalByte min() {
        return reduce(((left, right) -> left < right ? left : right));
    }

    @Override
    public OptionalByte max() {
        return reduce((left, right) -> left > right ? left : right);
    }

    @Override
    public long count() {
        return mapToLong(v -> 1L).sum();
    }

    @Override
    public OptionalDouble average() {
        ObjByteConsumer<long[]> consumer = ((longs, value) -> {
            longs[0]++;
            longs[1] += value;
        });
        long[] avg = collect(averageSupplier(), consumer, averageCombiner());
        return avg[0] > 0
                ? OptionalDouble.of((double) avg[1] / avg[0])
                : OptionalDouble.empty();
    }

    @Override
    public ByteSummaryStatistics summaryStatistics() {
        return collect(ByteSummaryStatistics::new, ByteSummaryStatistics::accept, ByteSummaryStatistics::combine);
    }

    @Override
    public boolean anyMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchKind.ANY));
    }

    @Override
    public boolean allMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchKind.ALL));
    }

    @Override
    public boolean noneMatch(BytePredicate predicate) {
        return evaluate(MatchOps.makeByte(predicate, MatchKind.NONE));
    }

    @Override
    public OptionalByte findFirst() {
        return evaluate(FindOps.makeByte(true));
    }

    @Override
    public OptionalByte findAny() {
        return evaluate(FindOps.makeByte(false));
    }

    @Override
    public Stream<Byte> boxed() {
        return mapToObj(Byte::valueOf);
    }

    @Override
    public PrimitiveIterator.OfByte iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.BYTE_VALUE;
    }

    @Override
    public ByteStream unordered() {
        if (!isOrdered()) {
            return this;
        }
        return new StateLessOp<Byte>(this, StreamShape.BYTE_VALUE, StreamOpFlag.NOT_ORDERED) {
            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return sink;
            }
        };
    }

    @Override
    public Spliterator.OfByte spliterator() {
        return toByte(super.spliterator());
    }


    static class Head<In> extends BytePipeline<In> {

        public Head(Supplier<? extends Spliterator<Byte>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        public Head(Spliterator<Byte> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sink<In> opWrapSink(int flags, Sink<Byte> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(ByteConsumer action) {
            if (!isParallel()) {
                BytePipeline.toByte(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(ByteConsumer action) {
            if (!isParallel()) {
                BytePipeline.toByte(sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEachOrdered(action);
            }
        }
    }

    public static abstract class StateLessOp<In> extends BytePipeline<In> {

        public StateLessOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    public static abstract class StatefulOp<In> extends BytePipeline<In> {
        public StatefulOp(AbstractPipeline<?, In, ?> upstream, StreamShape shape, int opFlags) {
            super(upstream, opFlags);
            assert upstream.getOutputShape() == shape;
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        protected abstract <P_IN> Node<Byte> opEvaluateParallel(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator, IntFunction<Byte[]> generator);
    }

}
