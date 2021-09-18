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

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.concurrent.CountedCompleter;

/**
 * @author amrjlg
 **/
public class ForEachTask<S, T> extends CountedCompleter<Void> {

    private final PipelineHelper<T> helper;
    private final Sink<S> sink;
    private Spliterator<S> spliterator;
    private long targetSize;

    public ForEachTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<S> sink) {
        super(null);
        this.helper = helper;
        this.sink = sink;
        this.spliterator = spliterator;
        this.targetSize = 0;
    }

    public ForEachTask(ForEachTask<S, T> parent, Spliterator<S> spliterator) {
        this.helper = parent.helper;
        this.sink = parent.sink;
        this.targetSize = parent.targetSize;
        this.spliterator = spliterator;
    }

    @Override
    public void compute() {
        Spliterator<S> right = spliterator, left;
        long remain = right.estimateSize(), threshold = targetSize;
        if (threshold == 0L) {
            targetSize = threshold = AbstractTask.suggestTargetSize(remain);
        }

        boolean shortCircuitKnown = StreamOpFlag.SHORT_CIRCUIT.isKnown(helper.getStreamAndOpFlags());

        boolean forkRight = false;
        Sink<S> taskSink = sink;
        ForEachTask<S, T> task = this;
        while (!shortCircuitKnown || !taskSink.cancellationRequested()) {
            if (remain <= threshold || (left = right.trySplit()) == null) {
                task.helper.copyInto(taskSink, right);
                break;
            }

            ForEachTask<S, T> leftTask = new ForEachTask<>(task, left);

            task.addToPendingCount(1);
            ForEachTask<S, T> taskToFork;
            if (forkRight) {
                forkRight = false;
                right = left;
                taskToFork = task;
                task = leftTask;
            } else {
                forkRight = true;
                taskToFork = leftTask;
            }

            taskToFork.fork();
            remain = right.estimateSize();
        }
        task.spliterator = null;
        task.propagateCompletion();
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        System.out.println("-------------------");
        super.onCompletion(caller);
    }
}
