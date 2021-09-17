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

import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.operations.ReduceOp;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.sink.AccumulatingSink;

import java.util.concurrent.CountedCompleter;

/**
 * @author amrjlg
 **/
public class ReduceTask<SplIn, PipelineOut, ReduceOut, Sink extends AccumulatingSink<PipelineOut, ReduceOut, Sink>>
        extends AbstractTask<SplIn, PipelineOut, Sink, ReduceTask<SplIn, PipelineOut, ReduceOut, Sink>> {


    private final ReduceOp<PipelineOut, ReduceOut, Sink> reduceOp;

    public ReduceTask(
            ReduceOp<PipelineOut, ReduceOut, Sink> reduce,
            PipelineHelper<PipelineOut> helper,
            Spliterator<SplIn> spliterator) {
        super(helper, spliterator);
        this.reduceOp = reduce;
    }

    public ReduceTask(ReduceTask<SplIn, PipelineOut, ReduceOut, Sink> parent,
                      Spliterator<SplIn> spliterator) {
        super(parent, spliterator);
        this.reduceOp = parent.reduceOp;
    }

    @Override
    protected ReduceTask<SplIn, PipelineOut, ReduceOut, Sink> makeChild(Spliterator<SplIn> spliterator) {
        return new ReduceTask<>(this, spliterator);
    }

    @Override
    protected Sink doLeaf() {
        return helper.wrapAndCopyInto(reduceOp.makeSink(), spliterator);
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (!isLeaf()) {
            Sink localResult = leftChild.getLocalResult();
            localResult.combine(rightChild.getLocalResult());
            setLocalResult(localResult);
        }
        super.onCompletion(caller);
    }


}
