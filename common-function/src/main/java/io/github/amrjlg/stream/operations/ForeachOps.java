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

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.TerminalOp;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public class ForeachOps {
    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfRef<>(consumer, ordered);
    }

    public static TerminalOp<Byte, Void> makeByte(ByteConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfByte(consumer, ordered);
    }

    public static TerminalOp<Short, Void> makeShort(ShortConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfShort(consumer, ordered);
    }

    public static TerminalOp<Character, Void> makeChar(CharConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfChar(consumer, ordered);
    }

    public static TerminalOp<Integer, Void> makeInt(IntConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfInt(consumer, ordered);
    }

    public static TerminalOp<Long, Void> makeLong(LongConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfLong(consumer, ordered);
    }

    public static TerminalOp<Float, Void> makeFloat(FloatConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfFloat(consumer, ordered);
    }

    public static TerminalOp<Double, Void> makeDouble(DoubleConsumer consumer, boolean ordered) {
        Objects.requireNonNull(consumer);
        return new ForeachOp.OfDouble(consumer, ordered);
    }

}
