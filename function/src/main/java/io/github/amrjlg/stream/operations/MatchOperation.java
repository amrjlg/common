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

import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.sink.MatchSink;
import io.github.amrjlg.stream.task.MatchTask;

import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class MatchOperation<T> implements TerminalOp<T, Boolean> {
    private final StreamShape inputShape;

    private final MatchKind matchKind;
    private final Supplier<MatchSink<T>> sinkSupplier;

    public MatchOperation(StreamShape inputShape, MatchKind matchKind, Supplier<MatchSink<T>> sinkSupplier) {
        this.inputShape = inputShape;
        this.matchKind = matchKind;
        this.sinkSupplier = sinkSupplier;
    }

    @Override
    public int getOpFlags() {
        return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
    }

    @Override
    public StreamShape inputShape() {
        return inputShape;
    }

    @Override
    public <Out> Boolean evaluateSequential(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
        return helper.wrapAndCopyInto(sinkSupplier.get(),spliterator).getAndClearState();
    }

    @Override
    public <Out> Boolean evaluateParallel(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
        return new MatchTask<>(this,helper,spliterator).invoke();
    }

    public MatchKind getMatchKind() {
        return matchKind;
    }


    public Supplier<MatchSink<T>> getSinkSupplier() {
        return sinkSupplier;
    }
}
