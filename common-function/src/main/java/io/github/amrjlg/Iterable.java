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

package io.github.amrjlg;

import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;

import java.util.Iterator;
import java.util.Objects;

import java.util.function.Consumer;

/**
 * @author amrjlg
 * @date 2021-09-22 11:04
 **/
public interface Iterable<T> {
    Iterator<T> iterator();

    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        iterator().forEachRemaining(action);
    }

    default Spliterator<T> spliterator() {
        return new Spliterators.IteratorSpliterator<>(iterator(), 0);
    }
}
