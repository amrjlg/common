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

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.stream.ByteStream;
import io.github.amrjlg.stream.CharStream;
import io.github.amrjlg.stream.DoubleStream;
import io.github.amrjlg.stream.FloatStream;
import io.github.amrjlg.stream.IntStream;
import io.github.amrjlg.stream.LongStream;
import io.github.amrjlg.stream.ShortStream;
import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.spliterator.Spliterator;

/**
 * @author amrjlg
 * @date 2021-09-27 16:42
 **/
public abstract class Pipelines {

    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        return new ReferencePipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static ByteStream byteStream(Spliterator.OfByte spl, boolean parallel) {
        return new BytePipeline.Head<>(spl, StreamOpFlag.fromCharacteristics(spl), parallel);
    }

    public static ShortStream shortStream(Spliterator.OfShort spliterator, boolean parallel) {
        return new ShortPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static CharStream charStream(Spliterator.OfChar spliterator, boolean parallel) {
        return new CharPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static LongStream longStream(Spliterator.OfLong spliterator, boolean parallel) {
        return new LongPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static FloatStream floatStream(Spliterator.OfFloat spliterator, boolean parallel) {
        return new FloatPipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }

    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator, boolean parallel) {
        return new DoublePipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), parallel);
    }
}
