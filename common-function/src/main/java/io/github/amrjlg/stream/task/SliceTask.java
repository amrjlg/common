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
 * limitations under the License.
 *
 */

package io.github.amrjlg.stream.task;

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.NodeBuilder;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.AbstractPipeline;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.concurrent.CountedCompleter;
import java.util.function.IntFunction;

/**
 * @author amrjlg
 **/
public class SliceTask<Input, Output>
        extends AbstractDefaultResultTask<Input, Output, Node<Output>, SliceTask<Input, Output>> {
    private final AbstractPipeline<Output, Output, ?> pipeline;
    private final IntFunction<Output[]> generator;
    private final long skip, limit;

    private volatile long nodeSize;
    private volatile boolean complete;

    public SliceTask(
            AbstractPipeline<Output, Output, ?> pipeline,
            PipelineHelper<Output> helper,
            Spliterator<Input> spliterator,
            IntFunction<Output[]> generator,
            long skip, long limit
    ) {
        super(helper, spliterator);
        this.pipeline = pipeline;
        this.generator = generator;
        this.skip = skip;
        this.limit = limit;
    }

    public SliceTask(SliceTask<Input, Output> parent, Spliterator<Input> spliterator) {
        super(parent, spliterator);
        this.pipeline = parent.pipeline;
        this.generator = parent.generator;
        this.skip = parent.skip;
        this.limit = parent.limit;
    }

    @Override
    protected SliceTask<Input, Output> makeChild(Spliterator<Input> spliterator) {
        return new SliceTask<>(this, spliterator);
    }


    @Override
    protected Node<Output> getEmptyResult() {
        return Nodes.empty(pipeline.getSourceShape());
    }

    @Override
    protected Node<Output> doLeaf() {
        if (isRoot()) {
            long sizeIfKnown = StreamOpFlag.SIZED.isPreserved(pipeline.getStreamAndOpFlags())
                    ? pipeline.exactOutputSizeIfKnown(spliterator)
                    : -1;
            NodeBuilder<Output> nodeBuilder = pipeline.makeNodeBuilder(sizeIfKnown, generator);
            Sink<Output> sink = pipeline.opWrapSink(helper.getStreamAndOpFlags(), nodeBuilder);
            helper.copyIntoWithCancel(helper.wrapSink(sink), spliterator);

            return nodeBuilder.build();
        }
        Node<Output> node = helper.wrapAndCopyInto(helper.makeNodeBuilder(-1, generator), spliterator).build();
        nodeSize = node.count();
        complete = true;
        spliterator = null;

        return node;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (!isLeaf()) {
            Node<Output> result;
            nodeSize = leftChild.nodeSize + rightChild.nodeSize;
            if (canceled) {
                nodeSize = 0;
                result = getEmptyResult();
            } else if (nodeSize == 0) {
                result = getEmptyResult();
            } else if (leftChild.nodeSize == 0) {
                result = rightChild.getLocalResult();
            } else {
                result = Nodes.concat(pipeline.getSourceShape(), leftChild.getLocalResult(), rightChild.getLocalResult());
            }
            setLocalResult(isRoot() ? doTruncate(result) : result);
            complete = true;
        }
        if (targetSize >= 0 && isRoot() && isLeftCompleted(skip + targetSize)) {
            cancelLaterNodes();
        }
        super.onCompletion(caller);
    }

    @Override
    protected void cancel() {
        super.cancel();
        if (canceled) {
            setLocalResult(getEmptyResult());
        }
    }

    private Node<Output> doTruncate(Node<Output> node) {
        long size = limit >= 0 ? Math.min(node.count(), skip + limit) : nodeSize;
        return node.truncate(skip, size, generator);
    }

    private boolean isLeftCompleted(long target) {
        long size = complete ? nodeSize : completeSize(target);
        if (size >= target) {
            return true;
        }
        SliceTask<Input, Output> parent = getParent(), node = this;
        while (parent != null) {
            if (node == parent.rightChild) {
                SliceTask<Input, Output> leftChild = parent.leftChild;
                if (leftChild != null) {
                    size += leftChild.completeSize(target);
                    if (size >= target) {
                        return true;
                    }
                }
            }
            node = parent;
            parent = parent.getParent();
        }
        return false;
    }

    private long completeSize(long target) {
        if (complete) {
            return nodeSize;
        }
        SliceTask<Input, Output> leftChild = this.leftChild;
        SliceTask<Input, Output> rightChild = this.rightChild;
        if (leftChild == null || rightChild == null) {
            return nodeSize;
        }
        long size = leftChild.completeSize(target);

        return (size >= target) ? size : size + rightChild.completeSize(target);

    }
}
