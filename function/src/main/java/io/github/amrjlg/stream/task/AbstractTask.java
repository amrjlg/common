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
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

/**
 * @author amrjlg
 * @see java.util.stream.AbstractTask
 **/
public abstract class AbstractTask<Input, Output, Result, Task extends AbstractTask<Input, Output, Result, Task>>
        extends CountedCompleter<Result> {

    static final int LEAF_TARGET = ForkJoinPool.getCommonPoolParallelism() << 2;

    protected final PipelineHelper<Output> helper;

    protected Spliterator<Input> spliterator;

    protected long targetSize;

    protected Task leftChild;
    protected Task rightChild;
    private Result localResult;


    public AbstractTask(PipelineHelper<Output> helper, Spliterator<Input> spliterator) {
        super(null);
        this.helper = helper;
        this.spliterator = spliterator;
        this.targetSize = 0L;
    }

    public AbstractTask(Task parent, Spliterator<Input> spliterator) {
        super(parent);
        this.spliterator = spliterator;
        this.helper = parent.helper;
        this.targetSize = parent.targetSize;
    }

    protected abstract Task makeChild(Spliterator<Input> spliterator);

    protected abstract Result doLeaf();

    public static long suggestTargetSize(long sizeEstimate) {
        long est = sizeEstimate / LEAF_TARGET;
        return est > 0L ? est : 1L;
    }


    protected final long getTargetSize(long sizeEstimate) {
        long s;
        return ((s = targetSize) != 0 ? s :
                (targetSize = suggestTargetSize(sizeEstimate)));
    }


    @Override
    public Result getRawResult() {
        return localResult;
    }

    @Override
    protected void setRawResult(Result result) {
        if (result != null) {
            throw new IllegalStateException();
        }
    }

    protected Result getLocalResult() {
        return localResult;
    }

    protected void setLocalResult(Result result) {
        this.localResult = result;
    }

    protected boolean isLeaf() {
        return leftChild == null;
    }

    @SuppressWarnings("unchecked")
    protected Task getParent() {
        return (Task) getCompleter();
    }

    protected boolean isRoot() {
        return getParent() == null;
    }


    @Override
    public void compute() {
        Spliterator<Input> right = spliterator, left;
        long estimateSize = right.estimateSize();
        long threshold = getTargetSize(estimateSize);

        boolean forkRight = false;
        @SuppressWarnings("unchecked")
        Task task = (Task) this;

        while (estimateSize > threshold && (left = right.trySplit()) != null) {
            Task leftChild, rightChild, taskToFork;

            task.leftChild = leftChild = task.makeChild(left);
            task.rightChild = rightChild = task.makeChild(right);
            task.setPendingCount(1);
            if (forkRight) {
                forkRight = false;
                right = left;
                task = leftChild;
                taskToFork = rightChild;
            } else {
                forkRight = true;
                task = rightChild;
                taskToFork = leftChild;
            }
            taskToFork.fork();
            estimateSize = right.estimateSize();
        }
        task.setLocalResult(task.doLeaf());
        task.tryComplete();

    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        spliterator = null;
        leftChild = rightChild = null;
    }
    protected boolean isLeftmostNode() {
        @SuppressWarnings("unchecked")
        Task node = (Task) this;
        while (node != null) {
            Task parent = node.getParent();
            if (parent != null && parent.leftChild != node)
                return false;
            node = parent;
        }
        return true;
    }

}
