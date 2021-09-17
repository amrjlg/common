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
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.pipeline.PipelineHelper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;

/**
 * @author amrjlg
 **/
public class ForEachOrderedTask<S, T> extends CountedCompleter<Void> {

    private final PipelineHelper<T> helper;
    private final long targetSize;
    private Spliterator<S> spliterator;

    private final ConcurrentHashMap<ForEachOrderedTask<S, T>, ForEachOrderedTask<S, T>> completionMap;
    private final Sink<T> action;
    private final ForEachOrderedTask<S, T> leftPredecessor;
    private Node<T> node;


    public ForEachOrderedTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<T> action) {
        super(null);
        this.helper = helper;
        this.spliterator = spliterator;
        this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
        this.completionMap = new ConcurrentHashMap<>(Math.max(16, AbstractTask.LEAF_TARGET << 1));
        this.action = action;
        this.leftPredecessor = null;
    }

    public ForEachOrderedTask(ForEachOrderedTask<S, T> parent, PipelineHelper<T> helper, Spliterator<S> spliterator, ForEachOrderedTask<S, T> leftPredecessor) {
        super(parent);
        this.helper = helper;
        this.spliterator = spliterator;
        this.targetSize = parent.targetSize;
        this.completionMap = parent.completionMap;
        this.action = parent.action;
        this.leftPredecessor = leftPredecessor;
    }

    @Override
    public void compute() {

    }
}
