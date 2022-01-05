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

import io.github.amrjlg.function.FloatToDoubleFunction;
import io.github.amrjlg.function.ObjFloatConsumer;
import io.github.amrjlg.function.FloatBinaryOperator;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.FloatFunction;
import io.github.amrjlg.function.FloatPredicate;
import io.github.amrjlg.function.FloatToByteFunction;
import io.github.amrjlg.function.FloatToCharFunction;
import io.github.amrjlg.function.FloatToIntFunction;
import io.github.amrjlg.function.FloatToLongFunction;
import io.github.amrjlg.function.FloatUnaryOperator;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.util.OptionalFloat;
import io.github.amrjlg.util.FloatSummaryStatistics;

import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public interface FloatStream extends BaseStream<Float,FloatStream> {

    FloatStream map(FloatUnaryOperator mapper);

    <U> Stream<U> mapToObj(FloatFunction<? extends U> mapper);

    ByteStream mapToByte(FloatToByteFunction mapper);

    CharStream mapToChar(FloatToCharFunction mapper);

    IntStream mapToInt(FloatToIntFunction mapper);

    LongStream mapToLong(FloatToLongFunction mapper);

    DoubleStream mapToDouble(FloatToDoubleFunction mapper);

    FloatStream flatMap(FloatFunction<? extends FloatStream> mapper);

    FloatStream filter(FloatPredicate predicate);

    FloatStream distinct();

    FloatStream sorted();

    FloatStream peek(FloatConsumer action);

    FloatStream limit(long maxSize);

    FloatStream skip(long n);

    void forEach(FloatConsumer action);

    void forEachOrdered(FloatConsumer action);

    float[] toArray();

    float reduce(float identity, FloatBinaryOperator op);

    OptionalFloat reduce(FloatBinaryOperator op);

    <R> R collect(Supplier<R> supplier, ObjFloatConsumer<R> accumulator, BiConsumer<R, R> combiner);

    float sum();

    OptionalFloat min();

    OptionalFloat max();

    long count();

    OptionalDouble average();

    FloatSummaryStatistics summaryStatistics();

    boolean anyMatch(FloatPredicate predicate);

    boolean allMatch(FloatPredicate predicate);

    boolean noneMatch(FloatPredicate predicate);

    OptionalFloat findFirst();

    OptionalFloat findAny();

    Stream<Float> boxed();

    FloatStream sequential();

    FloatStream parallel();

    PrimitiveIterator.OfFloat iterator();

    Spliterator.OfFloat spliterator();

    boolean isParallel();

    FloatStream unordered();

    FloatStream onClose(Runnable closeHandler);

    void close();
}
