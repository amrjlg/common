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

package io.github.amrjlg.stream.task;

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.stream.pipeline.PipelineHelper;


import java.util.concurrent.CountedCompleter;

/**
 * @author amrjlg
 **/
public abstract class SizedCollectorTask<Input, Output, TypeSink extends Sink<Output>, Task extends SizedCollectorTask<Input, Output, TypeSink, Task>>
        extends CountedCompleter<Void> implements Sink<Output> {

    protected final Spliterator<Input> spliterator;
    protected final PipelineHelper<Output> helper;
    protected final long targetSize;
    protected long offset;
    protected long length;
    protected int index, end;

    SizedCollectorTask(Spliterator<Input> spliterator, PipelineHelper<Output> helper, int arrayLength) {
        assert spliterator.hasCharacteristics(Spliterator.SUBSIZED);
        this.spliterator = spliterator;
        this.helper = helper;
        this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
        this.offset = 0;
        this.length = arrayLength;
    }

    SizedCollectorTask(Task parent, Spliterator<Input> spliterator, long offset, long length, int arrayLength) {
        super(parent);
        assert spliterator.hasCharacteristics(Spliterator.SUBSIZED);
        this.spliterator = spliterator;
        this.helper = parent.helper;
        this.targetSize = parent.targetSize;
        this.offset = offset;
        this.length = length;

        if (offset < 0 || length < 0 || (offset + length - 1 >= arrayLength)) {
            throw new IllegalArgumentException(
                    String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)",
                            offset, offset, length, arrayLength));
        }
    }

    abstract Task makeChild(Spliterator<Input> spliterator, long index, long offset);

    @Override
    public void compute() {
        SizedCollectorTask<Input, Output, TypeSink, Task> task = this;
        Spliterator<Input> rightSpl = this.spliterator, leftSpl;
        while (rightSpl.estimateSize() > task.targetSize && (leftSpl = rightSpl.trySplit()) != null) {
            task.setPendingCount(1);
            long estimateSize = leftSpl.estimateSize();
            task.makeChild(leftSpl, task.offset, estimateSize).fork();
            task = makeChild(rightSpl, task.offset + estimateSize, task.length - estimateSize);
        }
        assert task.offset + task.length < Nodes.MAX_ARRAY_SIZE;

        TypeSink sink = (TypeSink) task;
        task.helper.wrapAndCopyInto(sink, rightSpl);
        task.propagateCompletion();

    }

    @Override
    public void begin(long size) {
        if (size > length) {
            throw new IllegalStateException("size passed to Sink.begin exceeds array length");
        }
        index = (int) offset;
        end = index + (int) length;
    }

    public static final class OfRef<Input, Output>
            extends SizedCollectorTask<Input, Output, Sink<Output>, OfRef<Input, Output>>
            implements Sink<Output> {

        private final Output[] array;

        public OfRef(Spliterator<Input> spliterator, PipelineHelper<Output> helper, Output[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfRef(OfRef<Input, Output> parent, Spliterator<Input> spliterator, long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        OfRef<Input, Output> makeChild(Spliterator<Input> spliterator, long index, long offset) {
            return new OfRef<>(this, spliterator, index, offset);
        }

        @Override
        public void accept(Output value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfByte<Input> extends SizedCollectorTask<Input, Byte, SKinOfByte, OfByte<Input>>
            implements SKinOfByte {
        private final byte[] array;

        public OfByte(Spliterator<Input> spliterator, PipelineHelper<Byte> helper, byte[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfByte(SizedCollectorTask.OfByte<Input> parent, Spliterator<Input> spliterator,
                      long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfByte<Input> makeChild(Spliterator<Input> spliterator,
                                                   long offset, long size) {
            return new SizedCollectorTask.OfByte<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(byte value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfShort<Input> extends SizedCollectorTask<Input, Short, SKinOfShort, OfShort<Input>>
            implements SKinOfShort {
        private final short[] array;

        public OfShort(Spliterator<Input> spliterator, PipelineHelper<Short> helper, short[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfShort(SizedCollectorTask.OfShort<Input> parent, Spliterator<Input> spliterator,
                       long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfShort<Input> makeChild(Spliterator<Input> spliterator,
                                                    long offset, long size) {
            return new SizedCollectorTask.OfShort<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(short value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfChar<Input> extends SizedCollectorTask<Input, Character, SKinOfChar, OfChar<Input>>
            implements SKinOfChar {
        private final char[] array;

        public OfChar(Spliterator<Input> spliterator, PipelineHelper<Character> helper, char[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfChar(SizedCollectorTask.OfChar<Input> parent, Spliterator<Input> spliterator,
                      long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfChar<Input> makeChild(Spliterator<Input> spliterator,
                                                   long offset, long size) {
            return new SizedCollectorTask.OfChar<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(char value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfInt<Input> extends SizedCollectorTask<Input, Integer, SKinOfInt, OfInt<Input>>
            implements SKinOfInt {
        private final int[] array;

        public OfInt(Spliterator<Input> spliterator, PipelineHelper<Integer> helper, int[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfInt(SizedCollectorTask.OfInt<Input> parent, Spliterator<Input> spliterator,
                     long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfInt<Input> makeChild(Spliterator<Input> spliterator,
                                                  long offset, long size) {
            return new SizedCollectorTask.OfInt<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(int value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfLong<Input> extends SizedCollectorTask<Input, Long, SKinOfLong, OfLong<Input>>
            implements SKinOfLong {
        private final long[] array;

        public OfLong(Spliterator<Input> spliterator, PipelineHelper<Long> helper, long[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfLong(SizedCollectorTask.OfLong<Input> parent, Spliterator<Input> spliterator,
                      long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfLong<Input> makeChild(Spliterator<Input> spliterator,
                                                   long offset, long size) {
            return new SizedCollectorTask.OfLong<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(long value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfFloat<Input> extends SizedCollectorTask<Input, Float, SKinOfFloat, OfFloat<Input>>
            implements SKinOfFloat {
        private final float[] array;

        public OfFloat(Spliterator<Input> spliterator, PipelineHelper<Float> helper, float[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfFloat(SizedCollectorTask.OfFloat<Input> parent, Spliterator<Input> spliterator,
                       long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfFloat<Input> makeChild(Spliterator<Input> spliterator,
                                                    long offset, long size) {
            return new SizedCollectorTask.OfFloat<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(float value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }

    public static final class OfDouble<Input> extends SizedCollectorTask<Input, Double, SKinOfDouble, OfDouble<Input>>
            implements SKinOfDouble {
        private final double[] array;

        public OfDouble(Spliterator<Input> spliterator, PipelineHelper<Double> helper, double[] array) {
            super(spliterator, helper, array.length);
            this.array = array;
        }

        public OfDouble(SizedCollectorTask.OfDouble<Input> parent, Spliterator<Input> spliterator,
                        long offset, long length) {
            super(parent, spliterator, offset, length, parent.array.length);
            this.array = parent.array;
        }

        @Override
        SizedCollectorTask.OfDouble<Input> makeChild(Spliterator<Input> spliterator,
                                                     long offset, long size) {
            return new SizedCollectorTask.OfDouble<>(this, spliterator, offset, size);
        }

        @Override
        public void accept(double value) {
            if (index >= end) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            array[index++] = value;
        }
    }
}
