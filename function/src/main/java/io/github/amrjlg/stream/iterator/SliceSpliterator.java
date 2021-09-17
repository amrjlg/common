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

package io.github.amrjlg.stream.iterator;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public abstract class SliceSpliterator<T, Spl extends Spliterator<T>> {

    protected final long originBegin;
    protected final long originEnd;

    protected Spl spl;

    protected long begin;
    protected long end;

    protected SliceSpliterator(Spl spl, long originBegin, long originEnd, long begin, long end) {
        assert spl.hasCharacteristics(Spliterator.SUBSIZED);
        this.originBegin = originBegin;
        this.originEnd = originEnd;
        this.begin = begin;
        this.end = end;

    }


    protected abstract Spl makeSpliterator(Spl spl, long originBegin, long originEnd, long begin, long end);

    public Spl trySplit() {
        if (originBegin >= end || begin >= end) {
            return null;
        }

        while (true) {
            Spl left = (Spl) spl.trySplit();
            if (left == null) {
                return null;
            }

            long leftEndBounded = begin + left.estimateSize();
            long leftEnd = Math.min(leftEndBounded, originEnd);
            if (originBegin >= leftEnd) {
                begin = leftEnd;
            } else if (leftEndBounded >= originEnd) {
                spl = left;
                end = leftEnd;
            } else if (begin >= originBegin && leftEndBounded <= originEnd) {
                begin = leftEndBounded;
                return left;
            } else {
                return makeSpliterator(left, originBegin, originEnd, begin, begin = leftEnd);
            }

        }

    }

    public long estimateSize() {
        if (originBegin < end) {
            return end - Math.max(originBegin, begin);
        }
        return 0;
    }

    public int characteristics() {
        return spl.characteristics();
    }

    public static final class OfRef<T> extends SliceSpliterator<T, Spliterator<T>>
            implements Spliterator<T> {

        public OfRef(Spliterator<T> spl, long originBegin, long originEnd) {
            this(spl, originBegin, originEnd, 0, Math.min(spl.estimateSize(), originEnd));
        }

        public OfRef(Spliterator<T> spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        @Override
        protected Spliterator<T> makeSpliterator(Spliterator<T> spliterator, long originBegin, long originEnd, long begin, long end) {
            return new OfRef(spliterator, originBegin, originEnd, begin, end);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            if (originBegin >= end) {
                return false;
            }
            while (originBegin > begin) {
                spl.tryAdvance(e -> {
                });
                begin++;
            }
            if (begin >= end) {
                return false;
            }
            begin++;
            return spl.tryAdvance(consumer);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {

            Objects.requireNonNull(consumer);

            if (originBegin >= end || begin >= end) {
                return;
            }
            if (begin >= originBegin && (begin + spl.estimateSize()) <= originEnd) {
                spl.forEachRemaining(consumer);
                begin = end;
            } else {

                while (originBegin > begin) {
                    spl.tryAdvance(e -> {
                    });
                    begin++;
                }
                for (; begin < end; begin++) {
                    spl.tryAdvance(consumer);
                }
            }

        }
    }


    public static abstract class OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator extends Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator>>
            extends SliceSpliterator<Primitive, PrimitiveSpliterator>
            implements Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, PrimitiveSpliterator> {

        public OfPrimitive(PrimitiveSpliterator spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfPrimitive(PrimitiveSpliterator spliterator, long begin, long end) {
            this(spliterator, begin, end, 0, Math.min(spliterator.estimateSize(), end));
        }

        @Override
        public boolean tryAdvance(PrimitiveConsumer action) {
            Objects.requireNonNull(action);
            if (originBegin >= end) {
                return false;
            }
            while (originBegin > begin) {
                spl.tryAdvance(emptyConsumer());
                begin++;
            }
            if (begin >= end) {
                return false;
            }
            begin++;
            return spl.tryAdvance(action);
        }

        @Override
        public void forEachRemaining(PrimitiveConsumer consumer) {
            Objects.requireNonNull(consumer);

            if (originBegin >= end || begin >= end) {
                return;
            }
            if (begin >= originBegin && (begin + spl.estimateSize()) <= originEnd) {
                spl.forEachRemaining(consumer);
                begin = end;
            } else {

                while (originBegin > begin) {
                    spl.tryAdvance(e -> {
                    });
                    begin++;
                }
                for (; begin < end; begin++) {
                    spl.tryAdvance(consumer);
                }
            }
        }

        protected abstract PrimitiveConsumer emptyConsumer();
    }


    public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, Spliterator.OfByte>
            implements Spliterator.OfByte {

        public OfByte(Spliterator.OfByte ofByte, long originBegin, long originEnd, long begin, long end) {
            super(ofByte, originBegin, originEnd, begin, end);
        }

        public OfByte(Spliterator.OfByte spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfByte makeSpliterator(Spliterator.OfByte spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfByte(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected ByteConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfShort extends OfPrimitive<Short, ShortConsumer, Spliterator.OfShort>
            implements Spliterator.OfShort {

        public OfShort(Spliterator.OfShort ofShort, long originBegin, long originEnd, long begin, long end) {
            super(ofShort, originBegin, originEnd, begin, end);
        }

        public OfShort(Spliterator.OfShort spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfShort makeSpliterator(Spliterator.OfShort spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfShort(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected ShortConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfChar extends OfPrimitive<Character, CharConsumer, Spliterator.OfChar>
            implements Spliterator.OfChar {

        public OfChar(Spliterator.OfChar spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfChar(Spliterator.OfChar spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfChar makeSpliterator(Spliterator.OfChar spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfChar(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected CharConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfInt extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
            implements Spliterator.OfInt {

        public OfInt(Spliterator.OfInt spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfInt(Spliterator.OfInt spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfInt makeSpliterator(Spliterator.OfInt spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfInt(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected IntConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfLong extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
            implements Spliterator.OfLong {

        public OfLong(Spliterator.OfLong spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfLong(Spliterator.OfLong spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfLong makeSpliterator(Spliterator.OfLong spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfLong(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected LongConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, Spliterator.OfFloat>
            implements Spliterator.OfFloat {

        public OfFloat(Spliterator.OfFloat spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfFloat(Spliterator.OfFloat spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfFloat makeSpliterator(Spliterator.OfFloat spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfFloat(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected FloatConsumer emptyConsumer() {
            return e->{};
        }
    }

    public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
            implements Spliterator.OfDouble {

        public OfDouble(Spliterator.OfDouble spliterator, long originBegin, long originEnd, long begin, long end) {
            super(spliterator, originBegin, originEnd, begin, end);
        }

        public OfDouble(Spliterator.OfDouble spliterator, long begin, long end) {
            super(spliterator, begin, end);
        }

        @Override
        protected Spliterator.OfDouble makeSpliterator(Spliterator.OfDouble spl, long originBegin, long originEnd, long begin, long end) {
            return new SliceSpliterator.OfDouble(spl,originBegin,originEnd,begin,end);
        }

        @Override
        protected DoubleConsumer emptyConsumer() {
            return e->{};
        }
    }
}
