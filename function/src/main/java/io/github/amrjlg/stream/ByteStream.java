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

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ByteFunction;
import io.github.amrjlg.function.BytePredicate;
import io.github.amrjlg.function.ByteReduceOperator;
import io.github.amrjlg.function.ByteToDoubleFunction;
import io.github.amrjlg.function.ByteToLongFunction;
import io.github.amrjlg.function.ByteUnaryOperator;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author amrjlg
 **/
public interface ByteStream extends BaseStream<Byte, ByteStream> {
    ByteStream filter(BytePredicate predicate);

    ByteStream map(ByteUnaryOperator mapper);

    <U> Stream<U> mapToObj(ByteFunction<? extends U> mapper);

    LongStream mapToLong(ByteToLongFunction mapper);

    DoubleStream mapToDouble(ByteToDoubleFunction mapper);

    ByteStream flatMap(ByteFunction<? extends ByteStream> mapper);

    ByteStream distinct();

    ByteStream sorted();

    ByteStream peek(ByteConsumer action);

    ByteStream limit(long maxSize);

    ByteStream skip(long n);

    void forEach(ByteConsumer action);

    void forEachOrdered(ByteConsumer action);

    byte[] toArray();

    int reduce(int identity, ByteReduceOperator op);

    OptionalInt reduce(ByteReduceOperator op);

    <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner);

    int sum();

    OptionalInt min();

    OptionalInt max();

    long count();

    OptionalDouble average();

    IntSummaryStatistics summaryStatistics();

    boolean anyMatch(BytePredicate predicate);

    boolean allMatch(BytePredicate predicate);

    boolean noneMatch(BytePredicate predicate);

    OptionalInt findFirst();

    OptionalInt findAny();

    LongStream asLongStream();

    DoubleStream asDoubleStream();

    Stream<Byte> boxed();
}
