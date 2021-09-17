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
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.iterator.DistinctSpliterator;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.AbstractPipeline;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.pipeline.ReferencePipeline;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * @author amrjlg
 * @see java.util.stream.DistinctOps
 **/
public class DistinctOps {
    public static <T> ReferencePipeline<T, T> makeRef(AbstractPipeline<?, T, ?> pipeline) {


        return new ReferencePipeline.StatefulOp<T, T>(pipeline, StreamShape.REFERENCE, StreamOpFlag.IS_DISTINCT | StreamOpFlag.NOT_SIZED) {

            @Override
            public Sink<T> opWrapSink(int flags, Sink<T> sink) {
                Objects.requireNonNull(sink);
                if (StreamOpFlag.DISTINCT.isKnown(flags)) {
                    return sink;
                } else if (StreamOpFlag.SORTED.isKnown(flags)) {
                    return new Sink.ChainedReference<T, T>(sink) {
                        boolean seenNull;
                        T lastSeen;

                        @Override
                        public void begin(long size) {
                            seenNull = false;
                            lastSeen = null;
                            downstream.begin(-1);
                        }

                        @Override
                        public void end() {
                            seenNull = false;
                            lastSeen = null;
                            downstream.end();
                        }

                        @Override
                        public void accept(T t) {
                            if (t == null) {
                                if (!seenNull) {
                                    seenNull = true;
                                    downstream.accept(lastSeen = null);
                                }
                            } else if (lastSeen == null || !t.equals(lastSeen)) {
                                downstream.accept(lastSeen = t);
                            }
                        }
                    };
                } else {
                    return new Sink.ChainedReference<T, T>(sink) {
                        Set<T> seen;

                        @Override
                        public void begin(long size) {
                            seen = new HashSet<>();
                            downstream.begin(-1);
                        }

                        @Override
                        public void end() {
                            seen = null;
                            downstream.end();
                        }

                        @Override
                        public void accept(T t) {
                            if (!seen.contains(t)) {
                                seen.add(t);
                                downstream.accept(t);
                            }
                        }
                    };
                }
            }

            @Override
            public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    return helper.evaluate(spliterator, false, generator);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return reduce(helper, spliterator);
                } else {
                    AtomicBoolean seenNull = new AtomicBoolean(false);

                    ConcurrentHashMap<T, Boolean> map = new ConcurrentHashMap<>();

                    TerminalOp<T, Void> forEachOp = ForeachOps.makeRef((T t) -> {
                        if (t == null) {
                            seenNull.set(true);
                        } else {
                            map.putIfAbsent(t, Boolean.TRUE);
                        }

                    }, false);


                    forEachOp.evaluateParallel(helper, spliterator);

                    Set<T> set = map.keySet();

                    if (seenNull.get()) {
                        set = new HashSet<>(set);
                        set.add(null);
                    }
                    return Nodes.node(set);
                }
            }

            @Override
            protected <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    return helper.wrapSpliterator(spliterator);
                } else if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return reduce(helper, spliterator).spliterator();
                } else {
                    return new DistinctSpliterator<>(helper.wrapSpliterator(spliterator));
                }
            }


            private <P_IN> Node<T> reduce(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                return null;
            }
        };
    }
}
