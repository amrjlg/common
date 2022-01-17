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

package io.github.amrjlg.stream.operations;

import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.TerminalSink;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.task.FindTask;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class FindOperation<Input, Output> implements TerminalOp<Input, Output> {

    private final StreamShape shape;
    private final boolean findFirst;
    private final Output emptyValue;
    private final Predicate<Output> predicate;
    private final Supplier<TerminalSink<Input, Output>> sinkSupplier;

    public FindOperation(StreamShape shape, boolean findFirst, Output emptyValue, Predicate<Output> predicate, Supplier<TerminalSink<Input, Output>> sinkSupplier) {
        this.shape = shape;
        this.findFirst = findFirst;
        this.emptyValue = emptyValue;
        this.predicate = predicate;
        this.sinkSupplier = sinkSupplier;
    }

    @Override
    public int getOpFlags() {
        return StreamOpFlag.IS_SHORT_CIRCUIT | (findFirst ? 0 : StreamOpFlag.NOT_ORDERED);
    }

    @Override
    public StreamShape inputShape() {
        return shape;
    }

    @Override
    public <Out> Output evaluateSequential(PipelineHelper<Input> helper, Spliterator<Out> spliterator) {
        Output output = helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).get();
        return output == null ? emptyValue : output;
    }

    @Override
    public <Out> Output evaluateParallel(PipelineHelper<Input> helper, Spliterator<Out> spliterator) {
        return new FindTask<>(this, helper, spliterator).invoke();
    }

    public boolean isFindFirst() {
        return findFirst;
    }

    public Output getEmptyValue() {
        return emptyValue;
    }

    public Predicate<Output> getPredicate() {
        return predicate;
    }

    public Supplier<TerminalSink<Input, Output>> getSinkSupplier() {
        return sinkSupplier;
    }
}
