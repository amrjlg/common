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

import io.github.amrjlg.function.CharBinaryOperator;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.CharFunction;
import io.github.amrjlg.function.CharPredicate;
import io.github.amrjlg.function.CharToByteFunction;
import io.github.amrjlg.function.CharToDoubleFunction;
import io.github.amrjlg.function.CharToFloatFunction;
import io.github.amrjlg.function.CharToIntFunction;
import io.github.amrjlg.function.CharToLongFunction;
import io.github.amrjlg.function.CharUnaryOperator;
import io.github.amrjlg.function.ObjCharConsumer;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.util.CharSummaryStatistics;
import io.github.amrjlg.util.OptionalChar;

import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public interface CharStream extends BaseStream<Character, CharStream> {

    CharStream map(CharUnaryOperator mapper);

    <U> Stream<U> mapToObj(CharFunction<? extends U> mapper);

    ByteStream mapToByte(CharToByteFunction mapper);

    IntStream mapToInt(CharToIntFunction mapper);

    LongStream mapToLong(CharToLongFunction mapper);

    FloatStream mapToFloat(CharToFloatFunction mapper);

    DoubleStream mapToDouble(CharToDoubleFunction mapper);

    CharStream flatMap(CharFunction<? extends CharStream> mapper);

    CharStream filter(CharPredicate predicate);

    CharStream distinct();

    CharStream sorted();

    CharStream peek(CharConsumer action);

    CharStream limit(long maxSize);

    CharStream skip(long n);

    void forEach(CharConsumer action);

    void forEachOrdered(CharConsumer action);

    char[] toArray();

    char reduce(char identity, CharBinaryOperator op);

    OptionalChar reduce(CharBinaryOperator op);

    <R> R collect(Supplier<R> supplier, ObjCharConsumer<R> accumulator, BiConsumer<R, R> combiner);

    char sum();

    OptionalChar min();

    OptionalChar max();

    long count();

    OptionalDouble average();

    CharSummaryStatistics summaryStatistics();

    boolean anyMatch(CharPredicate predicate);

    boolean allMatch(CharPredicate predicate);

    boolean noneMatch(CharPredicate predicate);

    OptionalChar findFirst();

    OptionalChar findAny();

    Stream<Character> boxed();

    CharStream sequential();

    CharStream parallel();

    PrimitiveIterator.OfChar iterator();

    Spliterator.OfChar spliterator();

    boolean isParallel();

    CharStream unordered();

    CharStream onClose(Runnable closeHandler);

    void close();
}
