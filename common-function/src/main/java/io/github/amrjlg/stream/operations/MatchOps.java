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

package io.github.amrjlg.stream.operations;

import io.github.amrjlg.function.BytePredicate;
import io.github.amrjlg.function.CharPredicate;
import io.github.amrjlg.function.FloatPredicate;
import io.github.amrjlg.function.ShortPredicate;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.sink.MatchSink;

import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * @author amrjlg
 * @see java.util.stream.MatchOps
 **/
public class MatchOps {

    public static <Output> TerminalOp<Output, Boolean> makeRef(Predicate<? super Output> predicate, MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOperation<>(StreamShape.REFERENCE, matchKind, () -> new MatchSink.ReferenceMatchSink<>(matchKind, predicate));
    }

    public static TerminalOp<Byte, Boolean> makeByte(BytePredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.BYTE_VALUE, matchKind, () -> new MatchSink.ByteMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Short, Boolean> makeShort(ShortPredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.SHORT_VALUE, matchKind, () -> new MatchSink.ShortMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Character, Boolean> makeChar(CharPredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.CHAR_VALUE, matchKind, () -> new MatchSink.CharMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Integer, Boolean> makeInt(IntPredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.INT_VALUE, matchKind, () -> new MatchSink.IntMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Long, Boolean> makeLong(LongPredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.LONG_VALUE, matchKind, () -> new MatchSink.LongMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Float, Boolean> makeFloat(FloatPredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.FLOAT_VALUE, matchKind, () -> new MatchSink.FloatMatchSink(matchKind, predicate));
    }

    public static TerminalOp<Double, Boolean> makeDouble(DoublePredicate predicate, MatchKind matchKind) {
        return new MatchOperation<>(StreamShape.DOUBLE_VALUE, matchKind, () -> new MatchSink.DoubleMatchSink(matchKind, predicate));
    }
}
