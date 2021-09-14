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


import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.node.NodeBuilder;

import java.util.function.IntFunction;

/**
 * @author amrjlg
 **/
public interface PipelineHelper<Output> {

     StreamShape getSourceShape();

     int getStreamAndOpFlags();


    <Input> long exactOutputSizeIfKnown(Spliterator<Input> spliterator);

    <Input, S extends Sink<Output>> S wrapAndCopyInto(S sink, Spliterator<Input> spliterator);


    <Input> void copyInto(Sink<Input> wrappedSink, Spliterator<Input> spliterator);


     <Input> void copyIntoWithCancel(Sink<Input> wrappedSink, Spliterator<Input> spliterator);


    <Input> Sink<Input> wrapSink(Sink<Output> sink);


    <Input> Spliterator<Output> wrapSpliterator(Spliterator<Input> spliterator);


     NodeBuilder<Output> makeNodeBuilder(long exactSizeIfKnown,
                                         IntFunction<Output[]> generator);

    <Input> Node<Output> evaluate(Spliterator<Input> spliterator,
                                  boolean flatten,
                                  IntFunction<Output[]> generator);
}
