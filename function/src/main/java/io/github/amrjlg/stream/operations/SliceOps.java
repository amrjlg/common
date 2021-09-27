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
import io.github.amrjlg.stream.DoubleStream;
import io.github.amrjlg.stream.FloatStream;
import io.github.amrjlg.stream.IntStream;
import io.github.amrjlg.stream.LongStream;
import io.github.amrjlg.stream.ShortStream;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.pipeline.BytePipeline;
import io.github.amrjlg.stream.pipeline.CharPipeline;
import io.github.amrjlg.stream.pipeline.DoublePipeline;
import io.github.amrjlg.stream.pipeline.FloatPipeline;
import io.github.amrjlg.stream.pipeline.IntPipeline;
import io.github.amrjlg.stream.pipeline.LongPipeline;
import io.github.amrjlg.stream.pipeline.ShortPipeline;
import io.github.amrjlg.stream.spliterator.SliceSpliterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.UnorderedSliceSpliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.AbstractPipeline;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.pipeline.ReferencePipeline;
import io.github.amrjlg.stream.task.SliceTask;

import java.util.function.IntFunction;


/**
 * @author amrjlg
 * @see java.util.stream.SliceOps
 **/
public class SliceOps {

    public static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, long skip, long limit) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be non-negative: " + skip);
        }
        return new ReferencePipeline.StatefulOp<T, T>(upstream, StreamShape.REFERENCE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfRef<>(helper.wrapSpliterator(spliterator), skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, castingArray(), skip, limit).
                            invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.REFERENCE,
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                }
            }

            @Override
            public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> s = sliceSpliterator(StreamShape.REFERENCE, spliterator, skip, limit);
                    return Nodes.collect(helper, s, true, generator);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<T> s = unorderedSkipLimitSpliterator(helper.getSourceShape(), helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collect(this, s, true, generator);
                }

            }

            @Override
            public Sink<T> opWrapSink(int flags, Sink<T> sink) {
                return new Sink.ChainedReference<T, T>(sink) {
                    long n = skip;
                    long m = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void begin(long size) {
                        downstream.begin(calcSize(size, skip, m));
                    }

                    @Override
                    public boolean cancellationRequested() {
                        return m == 0 || downstream.cancellationRequested();
                    }

                    @Override
                    public void accept(T t) {
                        if (n == 0) {
                            if (m > 0) {
                                m--;
                                downstream.accept(t);
                            }
                        } else {
                            n--;
                        }
                    }
                };
            }
        };
    }

    public static ByteStream makeByte(AbstractPipeline<?, Byte, ?> upstream, long skip, long limit) {
        AbstractPipeline.positive(skip);
        return new BytePipeline.StatefulOp<Byte>(upstream, StreamShape.BYTE_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<Byte> opEvaluateParallelLazy(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfByte((Spliterator.OfByte) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Byte[]::new, skip, limit)
                            .invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.BYTE_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Byte> opEvaluateParallel(PipelineHelper<Byte> helper, Spliterator<P_IN> spliterator, IntFunction<Byte[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(StreamShape.BYTE_VALUE, spliterator, skip, limit);
                    return Nodes.collectByte(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator.OfByte spl = (Spliterator.OfByte) unorderedSkipLimitSpliterator(
                            StreamShape.BYTE_VALUE,
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);

                    return Nodes.collectByte(this, spl, true);
                }
            }

            @Override
            public Sink<Byte> opWrapSink(int flags, Sink<Byte> sink) {
                return new Sink.ChainedByte<Byte>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(byte value) {
                        if (s == 0) {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        } else {
                            s--;
                        }
                    }
                };
            }
        };
    }

    public static CharStream makeChar(AbstractPipeline<?, Character, ?> upstream, long skip, long limit) {
        return new CharPipeline.StatefulOp<Character>(upstream, StreamShape.CHAR_VALUE, flags(limit)) {

            @Override
            protected <P_IN> Spliterator<Character> opEvaluateParallelLazy(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfChar((Spliterator.OfChar) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Character[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.CHAR_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Character> opEvaluateParallel(PipelineHelper<Character> helper, Spliterator<P_IN> spliterator, IntFunction<Character[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(StreamShape.CHAR_VALUE, spliterator, skip, limit);
                    return Nodes.collectChar(helper, spl, true);
                } else if (!StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator.OfChar spl = (Spliterator.OfChar) unorderedSkipLimitSpliterator(StreamShape.CHAR_VALUE, spliterator, skip, limit, size);
                    return Nodes.collectChar(this, spl, true);
                }
            }

            @Override
            public Sink<Character> opWrapSink(int flags, Sink<Character> sink) {
                return new Sink.ChainedChar<Character>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(char value) {
                        if (s == 0) {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        } else {
                            s--;
                        }
                    }
                };
            }
        };
    }

    public static <Input> ShortStream makeShort(AbstractPipeline<Input, Short, ShortStream> upstream, long skip, long limit) {
        return new ShortPipeline.StatefulOp<Short>(upstream, StreamShape.SHORT_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Node<Short> opEvaluateParallel(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator, IntFunction<Short[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectShort(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<P_IN> spl = unorderedSkipLimitSpliterator(StreamShape.SHORT_VALUE, spliterator, skip, limit, size);
                    return Nodes.collectShort(this, spl, true);
                }
            }

            @Override
            protected <P_IN> Spliterator<Short> opEvaluateParallelLazy(PipelineHelper<Short> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfShort((Spliterator.OfShort) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Short[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.SHORT_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            public Sink<Short> opWrapSink(int flags, Sink<Short> sink) {
                return new Sink.ChainedShort<Short>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(short value) {
                        if (s == 0) {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        } else {
                            s--;
                        }
                    }
                };
            }
        };
    }

    public static <In> IntStream makeInt(AbstractPipeline<In, Integer, IntStream> upstream, long skip, long limit) {
        return new IntPipeline.StatefulOp<Integer>(upstream, StreamShape.INT_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<Integer> opEvaluateParallelLazy(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfInt((Spliterator.OfInt) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Integer[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.INT_VALUE,
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectInt(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<Integer> spl = unorderedSkipLimitSpliterator(StreamShape.INT_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collectInt(this, spl, true);
                }
            }

            @Override
            public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(int value) {
                        if (s == 0) {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        } else {
                            s--;
                        }
                    }
                };
            }
        };
    }

    public static <Input> LongStream makeLong(AbstractPipeline<Input, Long, LongStream> upstream, long skip, long limit) {
        return new LongPipeline.StatefulOp<Long>(upstream, StreamShape.LONG_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<Long> opEvaluateParallelLazy(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfLong((Spliterator.OfLong) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Long[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.LONG_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectLong(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<Long> spl = unorderedSkipLimitSpliterator(StreamShape.LONG_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collectLong(this, spl, true);
                }
            }

            @Override
            public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedLong<Long>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(long value) {
                        if (s == 0) {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        } else {
                            s--;
                        }
                    }
                };
            }
        };
    }

    public static <Input> FloatStream makeFloat(AbstractPipeline<Input, Float, FloatStream> upstream, long skip, long limit) {
        return new FloatPipeline.StatefulOp<Float>(upstream, StreamShape.FLOAT_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<Float> opEvaluateParallelLazy(PipelineHelper<Float> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfFloat((Spliterator.OfFloat) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Float[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.FLOAT_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Float> opEvaluateParallel(PipelineHelper<Float> helper, Spliterator<P_IN> spliterator, IntFunction<Float[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectFloat(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<Float> spl = unorderedSkipLimitSpliterator(StreamShape.FLOAT_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collectFloat(this, spl, true);
                }
            }

            @Override
            public Sink<Float> opWrapSink(int flags, Sink<Float> sink) {
                return new Sink.ChainedFloat<Float>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(float value) {
                        if (s > 0) {
                            s--;
                        } else {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        }
                    }
                };
            }
        };
    }

    public static <T> DoubleStream makeDouble(AbstractPipeline<T, Double, DoubleStream> upstream, long skip, long limit) {
        return new DoublePipeline.StatefulOp<Double>(upstream, StreamShape.DOUBLE_VALUE, flags(limit)) {
            @Override
            protected <P_IN> Spliterator<Double> opEvaluateParallelLazy(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfDouble((Spliterator.OfDouble) helper.wrapSpliterator(spliterator),
                            skip, calcSliceFence(skip, limit));
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, Double[]::new, skip, limit).invoke().spliterator();
                } else {
                    return unorderedSkipLimitSpliterator(StreamShape.DOUBLE_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                }
            }

            @Override
            protected <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> spl = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collectDouble(helper, spl, true);
                } else if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
                } else {
                    Spliterator<Double> spl = unorderedSkipLimitSpliterator(StreamShape.DOUBLE_VALUE, helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collectDouble(this, spl, true);
                }
            }

            @Override
            public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedDouble<Double>(sink) {
                    long s = skip;
                    long l = limit >= 0 ? limit : Long.MAX_VALUE;

                    @Override
                    public void accept(double value) {
                        if (s > 0) {
                            s--;
                        } else {
                            if (l > 0) {
                                l--;
                                downstream.accept(value);
                            }
                        }
                    }
                };
            }
        };
    }


    private static int flags(long limit) {
        return StreamOpFlag.NOT_SIZED | ((limit != -1) ? StreamOpFlag.IS_SHORT_CIRCUIT : 0);
    }

    private static long calcSize(long size, long skip, long limit) {
        return size >= 0 ? Math.max(-1, Math.min(size - skip, limit)) : -1;
    }

    private static long calcSliceFence(long skip, long limit) {
        long sliceFence = limit >= 0 ? skip + limit : Long.MAX_VALUE;
        // Check for overflow
        return (sliceFence >= 0) ? sliceFence : Long.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    private static <In> Spliterator<In> unorderedSkipLimitSpliterator(StreamShape shape, Spliterator<In> spl, long skip, long limit, long size) {
        if (skip <= size) {
            limit = limit >= 0 ? Math.min(limit, size - skip) : size - skip;
            skip = 0;
        }
        Spliterator<In> spliterator;
        switch (shape) {
            case REFERENCE:
                spliterator = new UnorderedSliceSpliterator.OfRef<>(spl, skip, limit);
                break;
            case BYTE_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfByte((Spliterator.OfByte) spl, skip, limit);
                break;
            case CHAR_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfChar((Spliterator.OfChar) spl, skip, limit);
                break;
            case SHORT_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfShort((Spliterator.OfShort) spl, skip, limit);
                break;
            case INT_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfInt((Spliterator.OfInt) spl, skip, limit);
                break;
            case LONG_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfLong((Spliterator.OfLong) spl, skip, limit);
                break;
            case FLOAT_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfFloat((Spliterator.OfFloat) spl, skip, limit);
                break;
            case DOUBLE_VALUE:
                spliterator = (Spliterator<In>) new UnorderedSliceSpliterator.OfDouble((Spliterator.OfDouble) spl, skip, limit);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return spliterator;
    }

    @SuppressWarnings("unchecked")
    private static <P_IN> Spliterator<P_IN> sliceSpliterator(StreamShape shape,
                                                             Spliterator<P_IN> s,
                                                             long skip, long limit) {
        assert s.hasCharacteristics(Spliterator.SUBSIZED);
        long sliceFence = calcSliceFence(skip, limit);
        Spliterator<P_IN> spliterator;
        switch (shape) {
            case REFERENCE:
                spliterator = new SliceSpliterator.OfRef<>(s, skip, sliceFence);
                break;
            case BYTE_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfByte((SliceSpliterator.OfByte) s, skip, sliceFence);
                break;
            case CHAR_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfChar((SliceSpliterator.OfChar) s, skip, sliceFence);
                break;
            case SHORT_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfShort((SliceSpliterator.OfShort) s, skip, sliceFence);
                break;
            case INT_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfInt((Spliterator.OfInt) s, skip, sliceFence);
                break;
            case LONG_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfLong((Spliterator.OfLong) s, skip, sliceFence);
                break;
            case FLOAT_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfFloat((Spliterator.OfFloat) s, skip, sliceFence);
                break;
            case DOUBLE_VALUE:
                spliterator = (Spliterator<P_IN>) new SliceSpliterator.OfDouble((Spliterator.OfDouble) s, skip, sliceFence);
                break;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
        return spliterator;
    }

    @SuppressWarnings("unchecked")
    private static <T> IntFunction<T[]> castingArray() {
        return size -> (T[]) new Object[size];
    }
}
