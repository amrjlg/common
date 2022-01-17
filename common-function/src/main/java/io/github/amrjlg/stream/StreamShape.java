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

package io.github.amrjlg.stream;

/**
 * @see java.util.stream.StreamShape
 */
public enum StreamShape {
    /**
     * The shape specialization corresponding to {@code Stream} and elements
     * that are object references.
     */
    REFERENCE,
    /**
     * The shape specialization corresponding to {@code IntStream} and elements
     * that are {@code int} values.
     */
    BYTE_VALUE,

    CHAR_VALUE,

    SHORT_VALUE,

    INT_VALUE,
    /**
     * The shape specialization corresponding to {@code LongStream} and elements
     * that are {@code long} values.
     */
    LONG_VALUE,

    FLOAT_VALUE,
    /**
     * The shape specialization corresponding to {@code DoubleStream} and
     * elements that are {@code double} values.
     */
    DOUBLE_VALUE
}