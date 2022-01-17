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

import java.util.Optional;
import java.util.function.BinaryOperator;

public class ReducingOptionalSink<Input> extends Box<Optional<Input>> implements AccumulatingSink<Input, Optional<Input>, ReducingOptionalSink<Input>> {
    private boolean empty;

    private final BinaryOperator<Input> combiner;

    public ReducingOptionalSink(BinaryOperator<Input> combiner) {
        this.combiner = combiner;
        state = Optional.empty();
    }

    @Override
    public void begin(long size) {
        state = Optional.empty();
    }

    @Override
    public void combine(ReducingOptionalSink<Input> other) {
        other.state.ifPresent(this);
    }

    @Override
    public void accept(Input t) {
        state = state.map(input -> Optional.ofNullable(combiner.apply(input, t)))
                .orElseGet(() -> Optional.ofNullable(t));
    }
}