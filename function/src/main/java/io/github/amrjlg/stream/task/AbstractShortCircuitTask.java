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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author amrjlg
 **/
public abstract class AbstractShortCircuitTask<Input, Output, Result, Task extends AbstractShortCircuitTask<Input, Output, Result, Task>>
        extends AbstractTask<Input, Output, Result, Task> {

    protected final AtomicReference<Result> sharedResult;

    protected volatile boolean canceled;

    public AbstractShortCircuitTask(PipelineHelper<Output> helper, Spliterator<Input> spliterator) {
        super(helper, spliterator);
        sharedResult = new AtomicReference<>(null);
    }

    public AbstractShortCircuitTask(Task parent, Spliterator<Input> spliterator) {
        super(parent, spliterator);
        sharedResult = parent.sharedResult;
    }

    protected abstract Result getEmptyResult();

    protected void shortCircuit(Result result) {
        if (result != null) {
            sharedResult.compareAndSet(null, result);
        }
    }

    @Override
    protected void setLocalResult(Result result) {
        if (isRoot()) {
            shortCircuit(result);
        } else {
            super.setLocalResult(result);
        }
    }

    @Override
    public Result getRawResult() {
        return getLocalResult();
    }

    protected void cancel() {
        canceled = true;
    }

    protected boolean taskCanceled() {
        boolean canceled = this.canceled;
        if (!canceled) {
            for (Task parent = getParent(); !canceled && parent != null; parent = parent.getParent()) {
                canceled = parent.canceled;
            }
        }
        return canceled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void compute() {
        Spliterator<Input> rs = spliterator, ls;
        long sizeEstimate = rs.estimateSize();
        long sizeThreshold = getTargetSize(sizeEstimate);
        boolean forkRight = false;
        Task task = (Task) this;
        Result result;
        while ((result = sharedResult.get()) == null) {
            if (task.taskCanceled()) {
                result = task.getEmptyResult();
                break;
            }
            if (sizeEstimate <= sizeThreshold || (ls = rs.trySplit()) == null) {
                result = task.doLeaf();
                break;
            }
            Task leftChild, rightChild, taskToFork;
            task.leftChild = leftChild = task.makeChild(ls);
            task.rightChild = rightChild = task.makeChild(rs);
            task.setPendingCount(1);
            if (forkRight) {
                forkRight = false;
                rs = ls;
                task = leftChild;
                taskToFork = rightChild;
            } else {
                forkRight = true;
                task = rightChild;
                taskToFork = leftChild;
            }
            taskToFork.fork();
            sizeEstimate = rs.estimateSize();
        }
        task.setLocalResult(result);
        task.tryComplete();
    }
    @SuppressWarnings("unchecked")
    protected void cancelLaterNodes() {
        Task node = (Task) this;
        Task parent = getParent();
        while (parent != null) {
            if (parent.leftChild == node) {
                Task rightChild = parent.rightChild;
                if (!rightChild.canceled) {
                    rightChild.cancel();
                }
            }
            node = parent;
            parent = parent.getParent();
        }
    }
}
