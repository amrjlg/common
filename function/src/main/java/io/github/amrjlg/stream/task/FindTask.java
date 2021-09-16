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
import io.github.amrjlg.stream.operations.FindOperation;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.concurrent.CountedCompleter;

/**
 * @author amrjlg
 **/
public class FindTask<PipelineInput, PipelineOutput, Result>
        extends AbstractShortCircuitTask<PipelineInput, PipelineOutput, Result, FindTask<PipelineInput, PipelineOutput, Result>> {

    private final FindOperation<PipelineOutput, Result> operation;

    public FindTask(FindOperation<PipelineOutput, Result> operation, PipelineHelper<PipelineOutput> helper, Spliterator<PipelineInput> spliterator) {
        super(helper, spliterator);
        this.operation = operation;
    }

    public FindTask(FindTask<PipelineInput, PipelineOutput, Result> parent, Spliterator<PipelineInput> spliterator) {
        super(parent, spliterator);
        this.operation = parent.operation;

    }


    @Override
    protected Result getEmptyResult() {
        return operation.getEmptyValue();
    }

    @Override
    protected FindTask<PipelineInput, PipelineOutput, Result> makeChild(Spliterator<PipelineInput> spliterator) {
        return new FindTask<>(this, spliterator);
    }

    @Override
    protected Result doLeaf() {
        Result result = helper.wrapAndCopyInto(operation.getSinkSupplier().get(), spliterator).get();
        if (!operation.isFindFirst()) {
            if (result != null) {
                shortCircuit(result);
            }
            return null;
        }
        if (result != null) {
            foundResult(result);
            return result;
        }
        return null;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (operation.isFindFirst()) {
            FindTask<PipelineInput, PipelineOutput, Result> child = leftChild, parent = null;
            while (child != parent) {
                Result result = child.getLocalResult();
                if (result != null && operation.getPredicate().test(result)) {
                    setLocalResult(result);
                    foundResult(result);
                    break;
                }
                parent = child;
                child = rightChild;
            }

        }
        super.onCompletion(caller);
    }

    private void foundResult(Result result) {
        if (isLeftmostNode()) {
            shortCircuit(result);
        } else {
            cancelLaterNodes();
        }
    }
}
