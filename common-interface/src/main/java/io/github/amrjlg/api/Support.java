/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.api;

import java.util.function.Predicate;

/**
 * interface to predicate  one object of {@link T}  can do something
 *
 * @param <T> type
 * @author amrjlg
 */
@FunctionalInterface
public interface Support<T> extends Predicate<T> {
    /**
     * 支持的对象
     *
     * @param t object
     * @return {@code true} if the args could do something
     */
    boolean support(T t);

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    default boolean test(T t) {
        return support(t);
    }
}
