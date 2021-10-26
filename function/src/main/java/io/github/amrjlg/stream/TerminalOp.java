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

package io.github.amrjlg.stream;

import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.spliterator.Spliterator;

/**
 * @param <Input>
 * @param <R>
 * @see java.util.stream.TerminalOp
 */
public interface TerminalOp<Input, R> {
    /**
     * Gets the shape of the input type of this operation.
     *
     * @return StreamShape of the input type of this operation
     * @implSpec The default returns {@code StreamShape.REFERENCE}.
     */
    default StreamShape inputShape() {
        return StreamShape.REFERENCE;
    }

    /**
     * Gets the stream flags of the operation.  Terminal operations may set a
     * limited subset of the stream flags defined in {@link StreamOpFlag}, and
     * these flags are combined with the previously combined stream and
     * intermediate operation flags for the pipeline.
     *
     * @return the stream flags for this operation
     * @implSpec The default implementation returns zero.
     * @see StreamOpFlag
     */
    default int getOpFlags() {
        return 0;
    }

    /**
     * Performs a parallel evaluation of the operation using the specified
     * {@code PipelineHelper}, which describes the upstream intermediate
     * operations.
     *
     * @param helper      the pipeline helper
     * @param spliterator the source spliterator
     * @return the result of the evaluation
     * @implSpec The default performs a sequential evaluation of the operation
     * using the specified {@code PipelineHelper}.
     */
    default <Out> R evaluateParallel(PipelineHelper<Input> helper,
                                     Spliterator<Out> spliterator) {
        return evaluateSequential(helper, spliterator);
    }

    /**
     * Performs a sequential evaluation of the operation using the specified
     * {@code PipelineHelper}, which describes the upstream intermediate
     * operations.
     *
     * @param helper      the pipeline helper
     * @param spliterator the source spliterator
     * @return the result of the evaluation
     */
    <Out> R evaluateSequential(PipelineHelper<Input> helper,
                               Spliterator<Out> spliterator);
}