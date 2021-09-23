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


import io.github.amrjlg.function.ObjShortConsumer;
import io.github.amrjlg.function.ShortBinaryOperator;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.function.ShortFunction;
import io.github.amrjlg.function.ShortPredicate;
import io.github.amrjlg.function.ShortToByteFunction;
import io.github.amrjlg.function.ShortToCharFunction;
import io.github.amrjlg.function.ShortToDoubleFunction;
import io.github.amrjlg.function.ShortToFloatFunction;
import io.github.amrjlg.function.ShortToIntFunction;
import io.github.amrjlg.function.ShortToLongFunction;
import io.github.amrjlg.function.ShortUnaryOperator;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.util.OptionalShort;
import io.github.amrjlg.util.ShortSummaryStatistics;

import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public interface ShortStream extends BaseStream<Short, ShortStream> {
    ShortStream map(ShortUnaryOperator mapper);

    <U> Stream<U> mapToObj(ShortFunction<? extends U> mapper);

    ByteStream mapToByte(ShortToByteFunction mapper);

    CharStream mapToChar(ShortToCharFunction mapper);

    IntStream mapToInt(ShortToIntFunction mapper);

    LongStream mapToLong(ShortToLongFunction mapper);

    FloatStream mapToFloat(ShortToFloatFunction mapper);

    DoubleStream mapToDouble(ShortToDoubleFunction mapper);

    ShortStream flatMap(ShortFunction<? extends ShortStream> mapper);

    ShortStream filter(ShortPredicate predicate);

    ShortStream distinct();

    ShortStream sorted();

    ShortStream peek(ShortConsumer action);

    ShortStream limit(long maxSize);

    ShortStream skip(long n);

    void forEach(ShortConsumer action);

    void forEachOrdered(ShortConsumer action);

    short[] toArray();

    short reduce(short identity, ShortBinaryOperator op);

    OptionalShort reduce(ShortBinaryOperator op);

    <R> R collect(Supplier<R> supplier, ObjShortConsumer<R> accumulator, BiConsumer<R, R> combiner);

    short sum();

    OptionalShort min();

    OptionalShort max();

    long count();

    OptionalDouble average();

    ShortSummaryStatistics summaryStatistics();

    boolean anyMatch(ShortPredicate predicate);

    boolean allMatch(ShortPredicate predicate);

    boolean noneMatch(ShortPredicate predicate);

    OptionalShort findFirst();

    OptionalShort findAny();

    Stream<Short> boxed();

    ShortStream sequential();

    ShortStream parallel();

    PrimitiveIterator.OfShort iterator();

    Spliterator.OfShort spliterator();

    boolean isParallel();

    ShortStream unordered();

    ShortStream onClose(Runnable closeHandler);

    void close();
}
