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

import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.sink.AccumulatingSink;
import io.github.amrjlg.stream.task.ReduceTask;

public abstract class ReduceOp<Input, Output, Sink extends AccumulatingSink<Input, Output, Sink>>
        implements TerminalOp<Input, Output> {
    private final StreamShape inputShape;

    protected ReduceOp(StreamShape inputShape) {
        this.inputShape = inputShape;
    }

    public abstract Sink makeSink();

    @Override
    public StreamShape inputShape() {
        return inputShape;
    }

    @Override
    public <Out> Output evaluateParallel(PipelineHelper<Input> helper, Spliterator<Out> spliterator) {
        return new ReduceTask<>(this, helper, spliterator).invoke().get();
    }

    @Override
    public <Out> Output evaluateSequential(PipelineHelper<Input> helper, Spliterator<Out> spliterator) {
        return helper.wrapAndCopyInto(makeSink(), spliterator).get();
    }

}