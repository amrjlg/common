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

import io.github.amrjlg.function.ByteBinaryOperator;
import io.github.amrjlg.function.CharBinaryOperator;
import io.github.amrjlg.function.ObjByteConsumer;
import io.github.amrjlg.function.ObjCharConsumer;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.common.Box;
import io.github.amrjlg.stream.sink.AccumulatingSink;
import io.github.amrjlg.stream.sink.ReducingCollectorSink;
import io.github.amrjlg.stream.sink.ReducingOptionalSink;
import io.github.amrjlg.stream.sink.ReducingSink;
import io.github.amrjlg.util.OptionalByte;
import io.github.amrjlg.util.OptionalChar;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author amrjlg
 * @see java.util.stream.ReduceOps
 **/
public class ReduceOps {

    public static <Input, Output> TerminalOp<Input, Output> makeRef(Output seed, BiFunction<Output, ? super Input, Output> reducer, BinaryOperator<Output> combiner) {
        Objects.requireNonNull(reducer);
        Objects.requireNonNull(combiner);


        return new ReduceOp<Input, Output, ReducingSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingSink<Input, Output> makeSink() {
                return new ReducingSink<>(seed, reducer, combiner);
            }
        };
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(BinaryOperator<T> operator) {
        Objects.requireNonNull(operator);
        return new ReduceOp<T, Optional<T>, ReducingOptionalSink<T>>(StreamShape.REFERENCE) {
            @Override
            public ReducingOptionalSink<T> makeSink() {
                return new ReducingOptionalSink<T>(operator);
            }
        };
    }

    public static <Input, Output> TerminalOp<Input, Output> makeRef(Collector<? super Input, Output, ?> collector) {

        Objects.requireNonNull(collector);
        return new ReduceOp<Input, Output, ReducingCollectorSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingCollectorSink<Input, Output> makeSink() {
                return new ReducingCollectorSink<>(collector);
            }

            @Override
            public int getOpFlags() {
                return collector.characteristics().contains(Collector.Characteristics.UNORDERED)
                        ? StreamOpFlag.NOT_SORTED
                        : 0;
            }
        };
    }

    public static <Input, Output> TerminalOp<Input, Output> makeRef(
            Supplier<Output> supplier,
            BiConsumer<Output, ? super Input> accumulator,
            BiConsumer<Output, Output> reducer
    ) {
        return new ReduceOp<Input, Output, ReducingCollectorSink<Input, Output>>(StreamShape.REFERENCE) {
            @Override
            public ReducingCollectorSink<Input, Output> makeSink() {
                return new ReducingCollectorSink<Input, Output>(supplier, accumulator, reducer);
            }
        };
    }

    public static TerminalOp<Byte, Byte> makeByte(byte identity, ByteBinaryOperator op) {
        class Adapter implements AccumulatingSink<Byte, Byte, Adapter>, Sink.OfByte {
            byte state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public void accept(byte value) {
                state = op.applyAsByte(state, value);
            }

            @Override
            public Byte get() {
                return state;
            }
        }
        return new ReduceOp<Byte, Byte, Adapter>(StreamShape.BYTE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Byte, OptionalByte> makeByte(ByteBinaryOperator operator) {
        class Adapter implements AccumulatingSink<Byte, OptionalByte, Adapter>, Sink.OfByte {
            boolean empty;
            byte state;

            @Override
            public void begin(long size) {
                empty = true;
                state = 0;
            }

            @Override
            public void accept(byte value) {
                if (empty) {
                    state = value;
                    empty = false;
                } else {
                    state = operator.applyAsByte(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalByte get() {
                return empty ? OptionalByte.empty() : OptionalByte.of(state);
            }
        }
        return new ReduceOp<Byte, OptionalByte, Adapter>(StreamShape.BYTE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };

    }

    public static <R> TerminalOp<Byte, R> makeByte(Supplier<R> supplier, ObjByteConsumer<R> accumulator, BinaryOperator<R> operator) {
        class Adapter extends Box<R> implements AccumulatingSink<Byte, R, Adapter>, Sink.OfByte {
            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(byte value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                state = operator.apply(state, other.state);
            }
        }

        return new ReduceOp<Byte, R, Adapter>(StreamShape.BYTE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Character, Character> makeChar(char identity, CharBinaryOperator op) {
        class Adapter implements AccumulatingSink<Character, Character, Adapter>, Sink.OfChar {
            char state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(char value) {
                state = op.applyAsChar(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Character get() {
                return state;
            }
        }

        return new ReduceOp<Character, Character, Adapter>(StreamShape.CHAR_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Character, OptionalChar> makeChar(CharBinaryOperator operator) {
        class Adapter implements AccumulatingSink<Character, OptionalChar, Adapter>, Sink.OfChar {
            boolean empty;
            char state;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(char value) {
                if (empty) {
                    state = value;
                } else {
                    state = operator.applyAsChar(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalChar get() {
                return empty ? OptionalChar.empty() : OptionalChar.of(state);
            }
        }
        return new ReduceOp<Character, OptionalChar, Adapter>(StreamShape.CHAR_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Character, R> makeChar(Supplier<R> supplier, ObjCharConsumer<R> accumulator, BinaryOperator<R> combiner) {
        class Adapter extends Box<R> implements AccumulatingSink<Character, R, Adapter>, Sink.OfChar {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(char value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Character, R, Adapter>(StreamShape.CHAR_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }
}
