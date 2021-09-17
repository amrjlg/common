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
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.TerminalSink;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.task.ForEachOrderedTask;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author amrjlg
 **/
public class ForeachOps {
    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfRef<>(consumer, ordered);
    }


    abstract static class ForeachOp<T> implements TerminalOp<T, Void>, TerminalSink<T, Void> {
        private final boolean ordered;

        public ForeachOp(boolean ordered) {
            this.ordered = ordered;
        }

        @Override
        public int getOpFlags() {
            return ordered ? 0 : StreamOpFlag.NOT_SORTED;
        }

        @Override
        public <Out> Void evaluateSequential(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
            return helper.wrapAndCopyInto(this, spliterator).get();
        }

        @Override
        public <Out> Void evaluateParallel(PipelineHelper<T> helper, Spliterator<Out> spliterator) {
            if (ordered) {
                new ForEachOrderedTask<>(helper, spliterator, this).invoke();
            } else {
                new ForEachOrderedTask<>(helper, spliterator, helper.wrapSink(this)).invoke();
            }
            return null;
        }

        @Override
        public Void get() {
            return null;
        }

        static final class OfRef<T> extends ForeachOp<T> {
            final Consumer<? super T> consumer;

            public OfRef(Consumer<? super T> consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public void accept(T t) {

            }
        }
    }
}
