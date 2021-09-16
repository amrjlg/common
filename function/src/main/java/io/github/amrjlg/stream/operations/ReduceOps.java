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
import io.github.amrjlg.stream.sink.ReducingCollectorSink;
import io.github.amrjlg.stream.sink.ReducingOptionalSink;
import io.github.amrjlg.stream.sink.ReducingSink;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author amrjlg
 **/
public class ReduceOps {

    public static <Input, Output> TerminalOp<Input, Output> makeRef(Output seed, BiFunction<Output, ? super Input, Output> reducer, BinaryOperator<Output> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);


        return new ReduceOp<Input, Output, ReducingSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink<Input, Output> makeSink() {
                return new ReducingSink<>(seed, reducer, combiner);
            }
        };
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<T, Optional<T>, ReducingOptionalSink<T>>(StreamShape.REFERENCE) {
            @Override
            public ReducingOptionalSink<T> makeSink() {
                return new ReducingOptionalSink<T>(operator);
            }
        };
    }

    public static <Input, Output> TerminalOp<Input, Output> makeRef(Collector<? super Input, Output, ?> collector) {

        Objects.requireNonNull(collector);
        return new ReduceOp<Input, Output, ReducingCollectorSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingCollectorSink<Input, Output> makeSink() {
                return new ReducingCollectorSink<>(collector);
            }

            @Override
            public int getOpFlags() {
                return collector.characteristics().contains(Collector.Characteristics.UNORDERED)
                        ? StreamOpFlag.NOT_SORTED
                        : 0;
            }
        };
    }

    public static <Input, Output> TerminalOp<Input, Output> makeRef(
            Supplier<Output> supplier,
            BiConsumer<Output, ? super Input> accumulator,
            BiConsumer<Output, Output> reducer
    ) {
        return new ReduceOp<Input, Output, ReducingCollectorSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingCollectorSink<Input, Output> makeSink() {
                return new ReducingCollectorSink<Input, Output>(supplier, accumulator, reducer);
            }
        };
    }

}
