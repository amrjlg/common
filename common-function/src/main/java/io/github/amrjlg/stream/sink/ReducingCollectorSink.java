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

package io.github.amrjlg.stream.sink;

import io.github.amrjlg.stream.common.Box;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author amrjlg
 **/
public class ReducingCollectorSink<Input, Output> extends Box<Output> implements AccumulatingSink<Input, Output, ReducingCollectorSink<Input, Output>> {
    private final Supplier<Output> supplier;
    private final BiConsumer<Output, ? super Input> accumulator;
    private final BinaryOperator<Output> combiner;
    private final BiConsumer<Output, Output> consumer;


    public ReducingCollectorSink(Collector<? super Input, Output, ?> collector) {
        this.supplier = Objects.requireNonNull(collector).supplier();
        this.accumulator = collector.accumulator();
        this.combiner = collector.combiner();
        this.consumer = null;
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
    }

    public ReducingCollectorSink(Supplier<Output> supplier, BiConsumer<Output, ? super Input> accumulator, BiConsumer<Output, Output> consumer) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(consumer);
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = null;
        this.consumer = consumer;
    }

    @Override
    public void begin(long size) {
        state = supplier.get();
    }

    @Override
    public void combine(ReducingCollectorSink<Input, Output> other) {
        if (Objects.isNull(consumer)) {
            state = combiner.apply(state, other.state);
        } else {
            consumer.accept(state, other.state);
        }
    }

    @Override
    public void accept(Input input) {
        accumulator.accept(state, input);
    }
}
