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

import io.github.amrjlg.exception.NotImplementedException;
import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public interface Sink<T> extends Consumer<T> {

    default void begin(long size) {
    }

    default void end() {
    }

    default boolean cancellationRequested() {
        return false;
    }

    default void accept(byte b) {
        throw new NotImplementedException();
    }

    default void accept(char c) {
        throw new NotImplementedException();
    }

    default void accept(short s) {
        throw new NotImplementedException();
    }

    default void accept(int i) {
        throw new NotImplementedException();
    }

    default void accept(long l) {
        throw new NotImplementedException();
    }

    default void accept(float f) {
        throw new NotImplementedException();
    }

    default void accept(double d) {
        throw new NotImplementedException();
    }

    interface OfByte extends Sink<Byte>, ByteConsumer {
        @Override
        void accept(byte b);

        @Override
        default void accept(Byte b) {
            accept(b.byteValue());
        }
    }


    interface OfShort extends Sink<Short>, ShortConsumer {
        @Override
        void accept(short s);

        @Override
        default void accept(Short s) {
            accept(s.shortValue());
        }
    }

    interface OfChar extends Sink<Character>, CharConsumer {
        @Override
        void accept(char c);

        @Override
        default void accept(Character character) {
            accept(character.charValue());
        }
    }


    interface OfInt extends Sink<Integer>, IntConsumer {
        @Override
        void accept(int i);

        @Override
        default void accept(Integer integer) {
            accept(integer.intValue());
        }
    }

    interface OfLong extends Sink<Long>, LongConsumer {

        @Override
        void accept(long l);

        @Override
        default void accept(Long l) {
            accept(l.longValue());
        }

    }

    interface OfFloat extends Sink<Float>, FloatConsumer {
        @Override
        void accept(float f);

        @Override
        default void accept(Float f) {
            accept(f.floatValue());
        }
    }

    interface OfDouble extends Sink<Double>, DoubleConsumer {
        @Override
        void accept(double d);

        @Override
        default void accept(Double d) {
            accept(d.doubleValue());
        }
    }


    abstract class ChainedReference<T, R> implements Sink<T> {
        protected final Sink<? super R> downstream;

        public ChainedReference(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedByte<R> implements OfByte {

        protected final Sink<? super R> downstream;

        public ChainedByte(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedShort<R> implements OfShort {

        protected final Sink<? super R> downstream;

        public ChainedShort(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedChar<R> implements OfChar {

        protected final Sink<? super R> downstream;

        public ChainedChar(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedInt<R> implements OfInt {

        protected final Sink<? super R> downstream;

        public ChainedInt(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedLong<R> implements OfLong {

        protected final Sink<? super R> downstream;

        public ChainedLong(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedFloat<R> implements OfFloat {

        protected final Sink<? super R> downstream;

        public ChainedFloat(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }

    abstract class ChainedDouble<R> implements OfDouble {

        protected final Sink<? super R> downstream;

        public ChainedDouble(Sink<? super R> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        @Override
        public void end() {
            downstream.end();
        }

        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }
    }
}
