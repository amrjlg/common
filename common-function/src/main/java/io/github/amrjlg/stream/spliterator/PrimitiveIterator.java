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

package io.github.amrjlg.stream.spliterator;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * primitive type Iterator
 *
 * @author amrjlg
 **/
public interface PrimitiveIterator<Primitive, PrimitiveConsumer> extends Iterator<Primitive> {

    void forEachRemaining(PrimitiveConsumer action);

    interface OfByte extends PrimitiveIterator<Byte, ByteConsumer> {
        byte nextByte();

        @Override
        default Byte next() {
            return nextByte();
        }

        @Override
        default void forEachRemaining(Consumer<? super Byte> action) {
            ByteConsumer consumer = action instanceof ByteConsumer ? (ByteConsumer) action : action::accept;
            forEachRemaining(consumer);
        }

        @Override
        default void forEachRemaining(ByteConsumer action) {
            Objects.requireNonNull(action);
            while (hasNext()) {
                action.accept(nextByte());
            }
        }
    }

    interface OfChar extends PrimitiveIterator<Character, CharConsumer> {

        char nextChar();

        @Override
        default void forEachRemaining(CharConsumer action) {
            while (hasNext()) {
                action.accept(nextChar());
            }
        }

        @Override
        default Character next() {
            return nextChar();
        }

        @Override
        default void forEachRemaining(Consumer<? super Character> action) {
            CharConsumer consumer = action instanceof CharConsumer ? (CharConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfShort extends PrimitiveIterator<Short, ShortConsumer> {

        short nextShort();

        @Override
        default void forEachRemaining(ShortConsumer action) {
            while (hasNext()) {
                action.accept(nextShort());
            }
        }

        @Override
        default Short next() {
            return nextShort();
        }

        @Override
        default void forEachRemaining(Consumer<? super Short> action) {
            ShortConsumer consumer = action instanceof ShortConsumer ? (ShortConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfInt extends PrimitiveIterator<Integer, IntConsumer> {

        int nextInt();

        @Override
        default void forEachRemaining(IntConsumer action) {
            while (hasNext()) {
                action.accept(nextInt());
            }
        }

        @Override
        default Integer next() {
            return nextInt();
        }

        @Override
        default void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
            } else {
                forEachRemaining((IntConsumer) action::accept);
            }
        }
    }

    interface OfLong extends PrimitiveIterator<Long, LongConsumer> {
        long nextLong();

        @Override
        default void forEachRemaining(LongConsumer action) {
            while (hasNext()) {
                action.accept(nextLong());
            }
        }

        @Override
        default Long next() {
            return nextLong();
        }

        @Override
        default void forEachRemaining(Consumer<? super Long> action) {
            LongConsumer consumer = action instanceof LongConsumer ? (LongConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfFloat extends PrimitiveIterator<Float, FloatConsumer> {
        float nextFloat();

        @Override
        default void forEachRemaining(FloatConsumer action) {
            while (hasNext()) {
                action.accept(nextFloat());
            }
        }

        @Override
        default Float next() {
            return nextFloat();
        }

        @Override
        default void forEachRemaining(Consumer<? super Float> action) {
            FloatConsumer consumer = action instanceof FloatConsumer ? (FloatConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

    interface OfDouble extends PrimitiveIterator<Double, DoubleConsumer> {
        double nextDouble();

        @Override
        default void forEachRemaining(DoubleConsumer action) {
            while (hasNext()) {
                action.accept(nextDouble());
            }
        }

        @Override
        default Double next() {
            return nextDouble();
        }

        @Override
        default void forEachRemaining(Consumer<? super Double> action) {
            DoubleConsumer consumer = action instanceof DoubleConsumer ? (DoubleConsumer) action : action::accept;
            forEachRemaining(consumer);
        }
    }

}
