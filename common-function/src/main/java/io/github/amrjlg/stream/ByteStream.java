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

import io.github.amrjlg.function.ByteBinaryOperator;
import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ByteFunction;
import io.github.amrjlg.function.BytePredicate;
import io.github.amrjlg.function.ByteToCharFunction;
import io.github.amrjlg.function.ByteToDoubleFunction;
import io.github.amrjlg.function.ByteToFloatFunction;
import io.github.amrjlg.function.ByteToIntFunction;
import io.github.amrjlg.function.ByteToLongFunction;
import io.github.amrjlg.function.ByteToShortFunction;
import io.github.amrjlg.function.ByteUnaryOperator;
import io.github.amrjlg.function.ObjByteConsumer;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.util.ByteSummaryStatistics;
import io.github.amrjlg.util.OptionalByte;

import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public interface ByteStream extends BaseStream<Byte, ByteStream> {
    ByteStream map(ByteUnaryOperator mapper);

    <U> Stream<U> mapToObj(ByteFunction<? extends U> mapper);

    CharStream mapToChar(ByteToCharFunction mapper);

    ShortStream mapToShort(ByteToShortFunction mapper);

    IntStream mapToInt(ByteToIntFunction mapper);

    LongStream mapToLong(ByteToLongFunction mapper);

    FloatStream mapToFloat(ByteToFloatFunction mapper);

    DoubleStream mapToDouble(ByteToDoubleFunction mapper);

    ByteStream flatMap(ByteFunction<? extends ByteStream> mapper);

    ByteStream filter(BytePredicate predicate);

    ByteStream distinct();

    ByteStream sorted();

    ByteStream peek(ByteConsumer action);

    ByteStream limit(long maxSize);

    ByteStream skip(long n);

    void forEach(ByteConsumer action);

    void forEachOrdered(ByteConsumer action);

    byte[] toArray();

    byte reduce(byte identity, ByteBinaryOperator op);

    OptionalByte reduce(ByteBinaryOperator op);

    <R> R collect(Supplier<R> supplier, ObjByteConsumer<R> accumulator, BiConsumer<R, R> combiner);

    int sum();

    OptionalByte min();

    OptionalByte max();

    long count();

    OptionalDouble average();

    ByteSummaryStatistics summaryStatistics();

    boolean anyMatch(BytePredicate predicate);

    boolean allMatch(BytePredicate predicate);

    boolean noneMatch(BytePredicate predicate);

    OptionalByte findFirst();

    OptionalByte findAny();

    Stream<Byte> boxed();

    ByteStream sequential();

    ByteStream parallel();

    PrimitiveIterator.OfByte iterator();

    Spliterator.OfByte spliterator();

    boolean isParallel();

    ByteStream unordered();

    ByteStream onClose(Runnable closeHandler);

    void close();
}
