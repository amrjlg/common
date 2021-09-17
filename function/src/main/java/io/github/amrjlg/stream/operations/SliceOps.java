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

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.iterator.SliceSpliterator;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.UnorderedSliceSpliterator;
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


            Spliterator<T> unorderedSkipLimitSpliterator(Spliterator<T> s,
                                                         long skip, long limit, long sizeIfKnown) {
                if (skip <= sizeIfKnown) {
                    // Use just the limit if the number of elements
                    // to skip is <= the known pipeline size
                    limit = limit >= 0 ? Math.min(limit, sizeIfKnown - skip) : sizeIfKnown - skip;
                    skip = 0;
                }
                return new UnorderedSliceSpliterator.OfRef<>(s, skip, limit);
            }

            @Override
            protected <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    return new SliceSpliterator.OfRef<>(helper.wrapSpliterator(spliterator), skip, calcSliceFence(skip, limit));
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return unorderedSkipLimitSpliterator(
                            helper.wrapSpliterator(spliterator),
                            skip, limit, size);
                } else {
                    return new SliceTask<>(this, helper, spliterator, castingArray(), skip, limit).
                            invoke().spliterator();
                }

            }

            @Override
            public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
                long size = helper.exactOutputSizeIfKnown(spliterator);
                if (size > 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
                    Spliterator<P_IN> s = sliceSpliterator(helper.getSourceShape(), spliterator, skip, limit);
                    return Nodes.collect(helper, s, true, generator);
                } else if (!StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    Spliterator<T> s = unorderedSkipLimitSpliterator(helper.wrapSpliterator(spliterator), skip, limit, size);
                    return Nodes.collect(this, s, true, generator);
                } else {
                    return new SliceTask<>(this, helper, spliterator, generator, skip, limit).invoke();
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
