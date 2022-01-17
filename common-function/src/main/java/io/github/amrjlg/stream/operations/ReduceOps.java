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

import io.github.amrjlg.function.ByteBinaryOperator;
import io.github.amrjlg.function.CharBinaryOperator;
import io.github.amrjlg.function.FloatBinaryOperator;
import io.github.amrjlg.function.ObjByteConsumer;
import io.github.amrjlg.function.ObjCharConsumer;
import io.github.amrjlg.function.ObjFloatConsumer;
import io.github.amrjlg.function.ObjShortConsumer;
import io.github.amrjlg.function.ShortBinaryOperator;
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
import io.github.amrjlg.util.OptionalFloat;
import io.github.amrjlg.util.OptionalShort;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
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
                    empty = false;
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

    public static TerminalOp<Short, OptionalShort> makeShort(ShortBinaryOperator op) {
        class Adapter implements AccumulatingSink<Short, OptionalShort, Adapter>, Sink.OfShort {
            short state;
            boolean empty;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(short value) {
                if (empty) {
                    state = value;
                    empty = false;
                } else {
                    state = op.applyAsShort(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalShort get() {
                return empty ? OptionalShort.empty() : OptionalShort.of(state);
            }
        }
        return new ReduceOp<Short, OptionalShort, Adapter>(StreamShape.SHORT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Short, Short> makeShort(short identity, ShortBinaryOperator op) {
        class Adapter implements AccumulatingSink<Short, Short, Adapter>, Sink.OfShort {
            short state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(short value) {
                state = op.applyAsShort(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Short get() {
                return state;
            }
        }
        return new ReduceOp<Short, Short, Adapter>(StreamShape.SHORT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Short, R> makeShort(Supplier<R> supplier, ObjShortConsumer<R> accumulator, BinaryOperator<R> combine) {
        class Adapter extends Box<R> implements AccumulatingSink<Short, R, Adapter>, Sink.OfShort {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(short value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                state = combine.apply(state, other.state);
            }
        }
        return new ReduceOp<Short, R, Adapter>(StreamShape.SHORT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(IntBinaryOperator op) {
        class Adapter implements AccumulatingSink<Integer, OptionalInt, Adapter>, Sink.OfInt {
            boolean empty;
            int state;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(int value) {
                if (empty) {
                    state = value;
                    empty = false;
                } else {
                    state = op.applyAsInt(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalInt get() {
                return empty ? OptionalInt.empty() : OptionalInt.of(state);
            }
        }
        return new ReduceOp<Integer, OptionalInt, Adapter>(StreamShape.INT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Integer, Integer> makeInt(int identity, IntBinaryOperator op) {
        class Adapter implements AccumulatingSink<Integer, Integer, Adapter>, Sink.OfInt {
            int state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(int value) {
                state = op.applyAsInt(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Integer get() {
                return state;
            }
        }
        return new ReduceOp<Integer, Integer, Adapter>(StreamShape.INT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Integer, R> makeInt(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BinaryOperator<R> combiner) {

        class Adapter extends Box<R> implements AccumulatingSink<Integer, R, Adapter>, Sink.OfInt {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(int value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Integer, R, Adapter>(StreamShape.INT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Long, OptionalLong> makeLong(LongBinaryOperator op) {
        class Adapter implements AccumulatingSink<Long, OptionalLong, Adapter>, Sink.OfLong {
            boolean empty;
            long state;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(long value) {
                if (empty) {
                    state = value;
                    empty = false;
                } else {
                    state = op.applyAsLong(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalLong get() {
                return empty ? OptionalLong.empty() : OptionalLong.of(state);
            }
        }
        return new ReduceOp<Long, OptionalLong, Adapter>(StreamShape.LONG_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Long, Long> makeLong(long identity, LongBinaryOperator op) {
        class Adapter implements AccumulatingSink<Long, Long, Adapter>, Sink.OfLong {
            long state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(long value) {
                state = op.applyAsLong(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Long get() {
                return state;
            }
        }
        return new ReduceOp<Long, Long, Adapter>(StreamShape.LONG_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Long, R> makeLong(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BinaryOperator<R> combiner) {
        class Adapter extends Box<R> implements AccumulatingSink<Long, R, Adapter>, Sink.OfLong {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(long value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Long, R, Adapter>(StreamShape.LONG_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Float, OptionalFloat> makeFloat(FloatBinaryOperator op) {
        class Adapter implements AccumulatingSink<Float, OptionalFloat, Adapter>, Sink.OfFloat {
            boolean empty;
            float state;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(float value) {
                if (empty) {
                    empty = false;
                    state = value;
                } else {
                    state = op.applyAsFloat(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalFloat get() {
                return empty ? OptionalFloat.empty() : OptionalFloat.of(state);
            }
        }
        return new ReduceOp<Float, OptionalFloat, Adapter>(StreamShape.FLOAT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Float, Float> makeFloat(float identity, FloatBinaryOperator op) {
        class Adapter implements AccumulatingSink<Float, Float, Adapter>, Sink.OfFloat {
            float state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(float value) {
                state = op.applyAsFloat(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Float get() {
                return state;
            }
        }

        return new ReduceOp<Float, Float, Adapter>(StreamShape.FLOAT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Float, R> makeFloat(Supplier<R> supplier, ObjFloatConsumer<R> accumulator, BinaryOperator<R> combiner) {
        class Adapter extends Box<R> implements AccumulatingSink<Float, R, Adapter>, Sink.OfFloat {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(float value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Float, R, Adapter>(StreamShape.FLOAT_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(DoubleBinaryOperator op) {
        class Adapter implements AccumulatingSink<Double, OptionalDouble, Adapter>, Sink.OfDouble {

            boolean empty;
            double state;

            @Override
            public void begin(long size) {
                empty = true;
            }

            @Override
            public void accept(double value) {
                if (empty) {
                    state = value;
                    empty = false;
                } else {
                    state = op.applyAsDouble(state, value);
                }
            }

            @Override
            public void combine(Adapter other) {
                if (!other.empty) {
                    accept(other.state);
                }
            }

            @Override
            public OptionalDouble get() {
                return empty ? OptionalDouble.empty() : OptionalDouble.of(state);
            }
        }
        return new ReduceOp<Double, OptionalDouble, Adapter>(StreamShape.DOUBLE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static TerminalOp<Double, Double> makeDouble(double identity, DoubleBinaryOperator op) {
        class Adapter implements AccumulatingSink<Double, Double, Adapter>, Sink.OfDouble {
            double state;

            @Override
            public void begin(long size) {
                state = identity;
            }

            @Override
            public void accept(double value) {
                state = op.applyAsDouble(state, value);
            }

            @Override
            public void combine(Adapter other) {
                accept(other.state);
            }

            @Override
            public Double get() {
                return state;
            }
        }
        return new ReduceOp<Double, Double, Adapter>(StreamShape.DOUBLE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }

    public static <R> TerminalOp<Double, R> makeDouble(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BinaryOperator<R> combiner) {
        class Adapter extends Box<R> implements AccumulatingSink<Double, R, Adapter>, Sink.OfDouble {

            @Override
            public void begin(long size) {
                state = supplier.get();
            }

            @Override
            public void accept(double value) {
                accumulator.accept(state, value);
            }

            @Override
            public void combine(Adapter other) {
                state = combiner.apply(state, other.state);
            }
        }
        return new ReduceOp<Double, R, Adapter>(StreamShape.DOUBLE_VALUE) {
            @Override
            public Adapter makeSink() {
                return new Adapter();
            }
        };
    }
}
