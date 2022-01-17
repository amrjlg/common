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

package io.github.amrjlg.stream.operations;

import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.sink.FindTerminalSink;
import io.github.amrjlg.util.OptionalByte;
import io.github.amrjlg.util.OptionalChar;
import io.github.amrjlg.util.OptionalFloat;
import io.github.amrjlg.util.OptionalShort;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author amrjlg
 * @see java.util.stream.FindOps
 **/
public class FindOps {

    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean findFirst) {
        return new FindOperation<>(StreamShape.REFERENCE, findFirst, Optional.empty(), Optional::isPresent, FindTerminalSink.OfRef::new);
    }

    public static TerminalOp<Byte, OptionalByte> makeByte(boolean findFirst) {
        return new FindOperation<>(StreamShape.BYTE_VALUE, findFirst, OptionalByte.empty(), OptionalByte::isPresent, FindTerminalSink.OfByte::new);
    }

    public static TerminalOp<Short, OptionalShort> makeShort(boolean findFirst) {
        return new FindOperation<>(StreamShape.SHORT_VALUE, findFirst, OptionalShort.empty(), OptionalShort::isPresent, FindTerminalSink.OfShort::new);
    }

    public static TerminalOp<Character, OptionalChar> makeChar(boolean findFirst) {
        return new FindOperation<>(StreamShape.CHAR_VALUE, findFirst, OptionalChar.empty(), OptionalChar::isPresent, FindTerminalSink.OfChar::new);
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(boolean findFirst) {
        return new FindOperation<>(StreamShape.INT_VALUE, findFirst, OptionalInt.empty(), OptionalInt::isPresent, FindTerminalSink.OfInt::new);
    }

    public static TerminalOp<Long, OptionalLong> makeLong(boolean findFirst) {
        return new FindOperation<>(StreamShape.LONG_VALUE, findFirst, OptionalLong.empty(), OptionalLong::isPresent, FindTerminalSink.OfLong::new);
    }

    public static TerminalOp<Float, OptionalFloat> makeFloat(boolean findFirst) {
        return new FindOperation<>(StreamShape.FLOAT_VALUE, findFirst, OptionalFloat.empty(), OptionalFloat::isPresent, FindTerminalSink.OfFloat::new);
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean findFirst) {
        return new FindOperation<>(StreamShape.DOUBLE_VALUE, findFirst, OptionalDouble.empty(), OptionalDouble::isPresent, FindTerminalSink.OfDouble::new);
    }
}
