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

package io.github.amrjlg.stream.task;

import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.node.ConcatNode;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.NodeBuilder;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.concurrent.CountedCompleter;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

/**
 * @author amrjlg
 **/
public class CollectorTask<Input, Output, TypeNode extends Node<Output>, TypeBuilder extends NodeBuilder<Output>>
        extends AbstractTask<Input, Output, TypeNode, CollectorTask<Input, Output, TypeNode, TypeBuilder>> {

    protected final PipelineHelper<Output> helper;
    protected final LongFunction<TypeBuilder> builderFactory;
    protected BinaryOperator<TypeNode> concatFactory;

    public CollectorTask(
            PipelineHelper<Output> helper,
            Spliterator<Input> spliterator,
            LongFunction<TypeBuilder> builderFactory,
            BinaryOperator<TypeNode> concatFactory
    ) {
        super(helper, spliterator);
        this.helper = helper;
        this.builderFactory = builderFactory;
        this.concatFactory = concatFactory;
    }

    public CollectorTask(CollectorTask<Input, Output, TypeNode, TypeBuilder> parent, Spliterator<Input> spliterator) {
        super(parent, spliterator);
        this.helper = parent.helper;
        this.builderFactory = parent.builderFactory;
        this.concatFactory = parent.concatFactory;
    }

    @Override
    protected CollectorTask<Input, Output, TypeNode, TypeBuilder> makeChild(Spliterator<Input> spliterator) {
        return new CollectorTask<>(this, spliterator);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TypeNode doLeaf() {
        TypeBuilder builder = builderFactory.apply(helper.exactOutputSizeIfKnown(spliterator));
        return (TypeNode) helper.wrapAndCopyInto(builder, spliterator).build();
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (!isLeaf()) {
            setLocalResult(concatFactory.apply(leftChild.getLocalResult(), rightChild.getLocalResult()));
        }
        super.onCompletion(caller);
    }

    public static final class OfRef<Input, Output> extends CollectorTask<Input, Output, Node<Output>, NodeBuilder<Output>> {

        public OfRef(PipelineHelper<Output> helper, IntFunction<Output[]> generator, Spliterator<Input> spliterator) {
            super(helper, spliterator, s -> Nodes.builder(s, generator), ConcatNode::new);
        }
    }

    public static final class OfByte<Input>
            extends CollectorTask<Input, Byte, Node.OfByte, NodeBuilder.OfByte> {
        public OfByte(PipelineHelper<Byte> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::byteBuilder, ConcatNode.OfByte::new);
        }
    }

    public static final class OfShort<Input>
            extends CollectorTask<Input, Short, Node.OfShort, NodeBuilder.OfShort> {
        public OfShort(PipelineHelper<Short> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::shortBuilder, ConcatNode.OfShort::new);
        }
    }

    public static final class OfChar<Input>
            extends CollectorTask<Input, Character, Node.OfChar, NodeBuilder.OfChar> {
        public OfChar(PipelineHelper<Character> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::charBuilder, ConcatNode.OfChar::new);
        }
    }

    public static final class OfInt<Input>
            extends CollectorTask<Input, Integer, Node.OfInt, NodeBuilder.OfInt> {
        public OfInt(PipelineHelper<Integer> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::intBuilder, ConcatNode.OfInt::new);
        }
    }

    public static final class OfLong<Input>
            extends CollectorTask<Input, Long, Node.OfLong, NodeBuilder.OfLong> {
        public OfLong(PipelineHelper<Long> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::longBuilder, ConcatNode.OfLong::new);
        }
    }

    public static final class OfFloat<Input>
            extends CollectorTask<Input, Float, Node.OfFloat, NodeBuilder.OfFloat> {
        public OfFloat(PipelineHelper<Float> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::floatBuilder, ConcatNode.OfFloat::new);
        }
    }

    public static final class OfDouble<Input>
            extends CollectorTask<Input, Double, Node.OfDouble, NodeBuilder.OfDouble> {
        public OfDouble(PipelineHelper<Double> helper, Spliterator<Input> spliterator) {
            super(helper, spliterator, Nodes::doubleBuilder, ConcatNode.OfDouble::new);
        }
    }
}
