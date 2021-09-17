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

import io.github.amrjlg.function.BytePredicate;
import io.github.amrjlg.function.CharPredicate;
import io.github.amrjlg.function.FloatPredicate;
import io.github.amrjlg.function.ShortPredicate;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.operations.MatchKind;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * @author amrjlg
 **/
public abstract class MatchSink<T> implements Sink<T> {
    protected boolean stop;
    protected boolean value;

    protected final MatchKind matchKind;

    public MatchSink(MatchKind matchKind) {
        this.value = !matchKind.isShortCircuitResult();
        this.matchKind = matchKind;
    }

    public boolean getAndClearState() {
        return value;
    }

    @Override
    public boolean cancellationRequested() {
        return stop;
    }

    public static final class ReferenceMatchSink<T> extends MatchSink<T> {

        private final Predicate<? super T> predicate;

        public ReferenceMatchSink(MatchKind matchKind, Predicate<? super T> predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(T t) {
            if (!stop && predicate.test(t) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class ByteMatchSink extends MatchSink<Byte> implements OfByte {

        private final BytePredicate predicate;

        public ByteMatchSink(MatchKind matchKind, BytePredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(byte value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class ShortMatchSink extends MatchSink<Short> implements Sink.SKinOfShort {

        private final ShortPredicate predicate;

        public ShortMatchSink(MatchKind matchKind, ShortPredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(short value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class CharMatchSink extends MatchSink<Character> implements Sink.SKinOfChar {

        private final CharPredicate predicate;

        public CharMatchSink(MatchKind matchKind, CharPredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(char value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class IntMatchSink extends MatchSink<Integer> implements Sink.SKinOfInt {

        private final IntPredicate predicate;

        public IntMatchSink(MatchKind matchKind, IntPredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(int value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class LongMatchSink extends MatchSink<Long> implements Sink.SKinOfLong {

        private final LongPredicate predicate;

        public LongMatchSink(MatchKind matchKind, LongPredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(long value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class FloatMatchSink extends MatchSink<Float> implements Sink.SKinOfFloat {

        private final FloatPredicate predicate;

        public FloatMatchSink(MatchKind matchKind, FloatPredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(float value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }

    public static final class DoubleMatchSink extends MatchSink<Double> implements Sink.SKinOfDouble {

        private final DoublePredicate predicate;

        public DoubleMatchSink(MatchKind matchKind, DoublePredicate predicate) {
            super(matchKind);
            this.predicate = predicate;
        }

        @Override
        public void accept(double value) {
            if (!stop && predicate.test(value) == matchKind.isStopOnPredicateMatches()) {
                stop = true;
                this.value = matchKind.isShortCircuitResult();
            }
        }
    }
}
