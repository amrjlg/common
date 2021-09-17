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

package io.github.amrjlg.stream.sink;

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.TerminalSink;
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
 **/
public abstract class FindTerminalSink<Input, Output> implements TerminalSink<Input, Output> {

    protected boolean find;
    protected Input value;

    @Override
    public void accept(Input input) {
        if (!find) {
            find = true;
            this.value = input;
        }
    }

    @Override
    public boolean cancellationRequested() {
        return find;
    }

    public static final class OfRef<T> extends FindTerminalSink<T, Optional<T>> {

        @Override
        @SuppressWarnings("all")
        public Optional<T> get() {
            return find ? Optional.of(value) : (Optional<T>) null;
        }
    }

    public static final class OfByte extends FindTerminalSink<Byte, OptionalByte>
            implements Sink.OfByte {

        @Override
        public void accept(byte value) {
            accept(Byte.valueOf(value));
        }

        @Override
        public OptionalByte get() {
            return find ? OptionalByte.of(value) : null;
        }
    }

    public static final class OfShort extends FindTerminalSink<Short, OptionalShort>
            implements Sink.OfShort {

        @Override
        public void accept(short value) {
            accept(Short.valueOf(value));
        }

        @Override
        public OptionalShort get() {
            return find ? OptionalShort.of(value) : null;
        }
    }

    public static final class OfChar extends FindTerminalSink<Character, OptionalChar>
            implements Sink.OfChar {

        @Override
        public void accept(char value) {
            accept(Character.valueOf(value));
        }

        @Override
        public OptionalChar get() {
            return find ? OptionalChar.of(value) : null;
        }
    }

    public static final class OfInt extends FindTerminalSink<Integer, OptionalInt>
            implements Sink.OfInt {

        @Override
        public void accept(int value) {
            accept(Integer.valueOf(value));
        }

        @Override
        public OptionalInt get() {
            return find ? OptionalInt.of(value) : null;
        }
    }

    public static final class OfLong extends FindTerminalSink<Long, OptionalLong>
            implements Sink.OfLong {

        @Override
        public void accept(long value) {
            accept(Long.valueOf(value));
        }

        @Override
        public OptionalLong get() {
            return find ? OptionalLong.of(value) : null;
        }
    }

    public static final class OfFloat extends FindTerminalSink<Float, OptionalFloat>
            implements Sink.OfFloat {

        @Override
        public void accept(float value) {
            accept(Float.valueOf(value));
        }

        @Override
        public OptionalFloat get() {
            return find ? OptionalFloat.of(value) : null;
        }
    }

    public static final class OfDouble extends FindTerminalSink<Double, OptionalDouble>
            implements Sink.OfDouble {

        @Override
        public void accept(double value) {
            accept(Double.valueOf(value));
        }

        @Override
        public OptionalDouble get() {
            return find ? OptionalDouble.of(value) : null;
        }
    }
}
