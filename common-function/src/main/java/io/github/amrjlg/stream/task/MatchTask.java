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

import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.operations.MatchOperation;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

/**
 * @author amrjlg
 **/
public class MatchTask<Input, Output> extends AbstractDefaultResultTask<Input, Output, Boolean, MatchTask<Input, Output>> {

    private final MatchOperation<Output> operation;

    public MatchTask(MatchOperation<Output> operation, PipelineHelper<Output> helper, Spliterator<Input> spliterator) {
        super(helper, spliterator);
        this.operation = operation;
    }

    public MatchTask(MatchTask<Input, Output> parent, Spliterator<Input> spliterator) {
        super(parent, spliterator);
        this.operation = parent.operation;
    }

    @Override
    protected Boolean getEmptyResult() {
        return !operation.getMatchKind().isShortCircuitResult();
    }

    @Override
    protected MatchTask<Input, Output> makeChild(Spliterator<Input> spliterator) {
        return new MatchTask<>(this, spliterator);
    }

    @Override
    protected Boolean doLeaf() {
        boolean state = helper.wrapAndCopyInto(operation.getSinkSupplier().get(), spliterator).getAndClearState();

        if (state == operation.getMatchKind().isShortCircuitResult()) {
            shortCircuit(state);
        }
        return null;
    }
}
