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

package io.github.amrjlg.stream.buffer;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.spliterator.PrimitiveIterator;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;
import io.github.amrjlg.stream.node.Nodes;
import io.github.amrjlg.Iterable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 * @see java.util.stream.SpinedBuffer
 **/
public class SpinedBuffer<Element> extends AbstractSpinedBuffer
        implements Consumer<Element>, Iterable<Element> {

    protected Element[] currentChunk;

    protected Element[][] spine;

    @SuppressWarnings("unchecked")
    public SpinedBuffer() {
        super();
        currentChunk = (Element[]) new Object[1 << initialChunkPower];
    }

    @SuppressWarnings("unchecked")
    public SpinedBuffer(int initialCapacity) {
        super(initialCapacity);
        currentChunk = (Element[]) new Object[1 << initialChunkPower];
    }

    protected long capacity() {
        return (spineIndex == 0)
                ? currentChunk.length
                : priorElementCount[spineIndex] + spine[spineIndex].length;
    }

    @SuppressWarnings("unchecked")
    private void inflateSpine() {
        if (Objects.isNull(spine)) {
            spine = (Element[][]) new Object[MIN_SPINE_SIZE][];
            priorElementCount = new long[MIN_SPINE_SIZE];

            spine[0] = currentChunk;
        }

    }

    @SuppressWarnings("unchecked")
    protected final void ensureCapacity(long targetSize) {
        long capacity = capacity();
        if (targetSize > capacity) {
            inflateSpine();
            for (int i = spineIndex + 1; targetSize > capacity; i++) {
                if (i >= spine.length) {
                    int newSpineSize = spine.length * 2;
                    spine = Arrays.copyOf(spine, newSpineSize);

                    priorElementCount = Arrays.copyOf(priorElementCount, newSpineSize);
                }
                int chunkSize = chunkSize(i);

                spine[i] = (Element[]) new Object[chunkSize];
                priorElementCount[i] = priorElementCount[i - 1] + spine[i - 1].length;
                capacity += chunkSize;
            }
        }
    }

    protected void increaseCapacity() {
        ensureCapacity(capacity() + 1);
    }

    public Element get(long index) {
        if (spineIndex == 0) {
            if (index < elementIndex) {
                return currentChunk[(int) index];
            } else {
                throw new IndexOutOfBoundsException(Long.toString(index));
            }
        }

        if (index >= count()) {
            throw new IndexOutOfBoundsException(Long.toString(index));
        }

        for (int i = 0; i <= spineIndex; i++) {
            if (index < priorElementCount[i] + spine[i].length) {
                return spine[i][(int) (index - priorElementCount[i])];
            }
        }

        throw new IndexOutOfBoundsException(Long.toString(index));
    }

    public void copyInto(Element[] array, int offset) {
        long end = offset + count();
        if (end > array.length || end < offset) {
            throw new IndexOutOfBoundsException("does not fit");
        }

        if (spineIndex == 0) {
            System.arraycopy(currentChunk, 0, array, offset, elementIndex);
        } else {
            for (int i = 0; i < spineIndex; i++) {
                System.arraycopy(spine[i], 0, array, offset, spine[i].length);
                offset += spine[i].length;
            }
            if (elementIndex > 0) {
                System.arraycopy(currentChunk, 0, array, offset, elementIndex);
            }
        }
    }

    public Element[] asArray(IntFunction<Element[]> generator) {
        long size = count();

        if (size >= Nodes.MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        }

        Element[] elements = generator.apply((int) size);

        copyInto(elements, 0);
        return elements;
    }

    @Override
    public void clear() {
        if (spine != null) {
            currentChunk = spine[0];
            Arrays.fill(currentChunk, null);
            spine = null;
            priorElementCount = null;


        } else {
            for (int i = 0; i < elementIndex; i++) {
                currentChunk[i] = null;
            }
        }
        elementIndex = 0;
        spineIndex = 0;

    }

    @Override
    public Iterator<Element> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public void forEach(Consumer<? super Element> action) {
        for (int i = 0; i < spineIndex; i++) {
            for (Element element : spine[i]) {
                action.accept(element);
            }
        }

        for (int i = 0; i < elementIndex; i++) {
            action.accept(currentChunk[i]);
        }
    }

    @Override
    public void accept(Element element) {
        if (elementIndex == currentChunk.length) {
            inflateSpine();
            if (spineIndex + 1 >= spine.length || spine[spineIndex + 1] == null) {
                increaseCapacity();
            }
            elementIndex = 0;
            ++spineIndex;
            currentChunk = spine[spineIndex];
        }
        currentChunk[elementIndex++] = element;
    }

    private static final int SPLITERATOR_CHARACTERISTICS
            = Spliterator.SIZED | Spliterator.ORDERED | Spliterator.SUBSIZED;


    @Override
    public Spliterator<Element> spliterator() {
        class SpliteratorAdapter implements Spliterator<Element> {
            int splSpineIndex;

            // Last spine index
            final int lastSpineIndex;

            // The current element index into the current spine
            int splElementIndex;

            // Last spine's last element index + 1
            final int lastSpineElementFence;

            // When splSpineIndex >= lastSpineIndex and
            // splElementIndex >= lastSpineElementFence then
            // this spliterator is fully traversed
            // tryAdvance can set splSpineIndex > spineIndex if the last spine is full

            // The current spine array
            Element[] splChunk;

            SpliteratorAdapter(
                    int firstSpineIndex,
                    int lastSpineIndex,
                    int firstSpineElementIndex,
                    int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpineIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? currentChunk : spine[firstSpineIndex];
            }

            @Override
            public long estimateSize() {
                if (splSpineIndex == lastSpineIndex) {
                    return lastSpineElementFence - splElementIndex;
                }

                return priorElementCount[lastSpineIndex] + lastSpineElementFence - priorElementCount[splSpineIndex] - splElementIndex;
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Element> consumer) {
                Objects.requireNonNull(consumer);

                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    consumer.accept(splChunk[splElementIndex++]);
                }

                if (splElementIndex == splChunk.length) {
                    splElementIndex = 0;
                    ++splSpineIndex;
                    if (spine != null && splSpineIndex <= lastSpineIndex) {
                        splChunk = spine[splSpineIndex];
                    }
                    return true;
                }

                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super Element> consumer) {

                Objects.requireNonNull(consumer);
                if (splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence)) {
                    int index = this.splElementIndex;
                    for (int start = spineIndex; start < lastSpineIndex; start++) {
                        Element[] elements = spine[start];
                        for (; index < elements.length; index++) {
                            consumer.accept(elements[index]);
                        }
                        index = 0;
                    }

                    Element[] elements = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];
                    int end = this.lastSpineElementFence;
                    for (; index < end; index++) {
                        consumer.accept(elements[index]);
                    }

                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }


            }

            @Override
            public Spliterator<Element> trySplit() {
                if (splSpineIndex < lastSpineIndex) {
                    SpliteratorAdapter adapter = new SpliteratorAdapter(splSpineIndex, lastSpineIndex - 1, splElementIndex, spine[lastSpineIndex - 1].length);

                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[spineIndex];
                    return adapter;
                }
                if (splSpineIndex == lastSpineIndex) {
                    int end = (lastSpineElementFence - splElementIndex) / 2;
                    if (end == 0) {
                        return null;
                    } else {
                        Spliterator<Element> spliterator = Spliterators.spliterator(splChunk, splElementIndex, splElementIndex + end, Spliterator.ORDERED | Spliterator.IMMUTABLE);

                        splElementIndex += end;
                        return spliterator;
                    }
                }

                return null;
            }
        }
        return new SpliteratorAdapter(0, spineIndex, 0, elementIndex);
    }


    public static abstract class OfPrimitive<Primitive, PrimitiveArray, PrimitiveConsumer>
            extends AbstractSpinedBuffer implements Iterable<Primitive> {
        PrimitiveArray current;
        PrimitiveArray[] spine;

        public OfPrimitive(int initialCapacity) {
            super(initialCapacity);
            current = newArray(1 << initialChunkPower);
        }

        public OfPrimitive() {
            super();
            current = newArray(1 << initialChunkPower);
        }

        @Override
        public abstract Iterator<Primitive> iterator();

        @Override
        public abstract Spliterator<Primitive> spliterator();

        @Override
        public abstract void forEach(Consumer<? super Primitive> consumer);

        public abstract PrimitiveArray[] newArrayArray(int size);

        public abstract PrimitiveArray newArray(int size);

        public abstract int arrayLength(PrimitiveArray array);

        public abstract void arrayForEach(PrimitiveArray array, int start, int end, PrimitiveConsumer consumer);

        protected long capacity() {
            return (spineIndex == 0)
                    ? arrayLength(current)
                    : priorElementCount[spineIndex] + arrayLength(spine[spineIndex]);
        }

        private void inflateSpine() {
            if (spine == null) {
                spine = newArrayArray(MIN_SPINE_SIZE);
                priorElementCount = new long[MIN_SPINE_SIZE];
                spine[0] = current;
            }
        }

        protected final void ensureCapacity(long targetSize) {
            long capacity = capacity();

            if (targetSize > capacity) {
                inflateSpine();
                for (int i = spineIndex + 1; targetSize > capacity; i++) {
                    if (i >= spine.length) {
                        int size = spine.length * 2;
                        spine = Arrays.copyOf(spine, size);
                        priorElementCount = Arrays.copyOf(priorElementCount, size);
                    }
                    int chunkSize = chunkSize(i);
                    int preIndex = i - 1;
                    spine[i] = newArray(chunkSize);
                    priorElementCount[i] = priorElementCount[preIndex] + arrayLength(spine[preIndex]);

                    capacity += chunkSize;
                }
            }
        }

        protected void increaseCapacity() {
            ensureCapacity(capacity() + 1);
        }

        protected int chunkFor(long index) {
            if (spineIndex == 0) {
                if (index < elementIndex) {
                    return 0;
                }
                throw new IndexOutOfBoundsException(Long.toString(index));
            }

            if (index >= count()) {
                throw new IndexOutOfBoundsException(Long.toString(index));
            }

            for (int i = 0; i <= spineIndex; i++) {
                if (index < priorElementCount[i] + arrayLength(spine[i])) {
                    return i;
                }
            }

            throw new IndexOutOfBoundsException(Long.toString(index));

        }

        public void copyInto(PrimitiveArray array, int offset) {
            long end = offset + count();
            if (end > arrayLength(array) || end < offset) {
                throw new IndexOutOfBoundsException("does not fit");
            }

            if (spineIndex == 0) {
                System.arraycopy(current, 0, array, offset, elementIndex);
            } else {

                for (int i = 0; i < spineIndex; i++) {
                    PrimitiveArray primitiveArray = spine[i];
                    int length = arrayLength(primitiveArray);
                    System.arraycopy(primitiveArray, 0, array, offset, length);
                    offset += length;
                }
                if (elementIndex > 0) {
                    System.arraycopy(current, 0, array, offset, elementIndex);
                }
            }

        }

        public PrimitiveArray asPrimitiveArray() {
            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            PrimitiveArray result = newArray((int) size);
            copyInto(result, 0);
            return result;
        }

        protected void preAccept() {
            if (elementIndex == arrayLength(current)) {
                inflateSpine();
                if (spineIndex + 1 >= spine.length || spine[spineIndex + 1] == null)
                    increaseCapacity();
                elementIndex = 0;
                ++spineIndex;
                current = spine[spineIndex];
            }
        }

        public void clear() {
            if (spine != null) {
                current = spine[0];
                spine = null;
                priorElementCount = null;
            }
            elementIndex = 0;
            spineIndex = 0;
        }

        public void forEach(PrimitiveConsumer consumer) {
            for (int i = 0; i < spineIndex; i++) {
                arrayForEach(spine[i], 0, arrayLength(spine[i]), consumer);
            }
            arrayForEach(current, 0, elementIndex, consumer);
        }

        public abstract class BaseSpinedBufferSpliterator<Spl extends Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, Spl>>
                implements Spliterator.OfPrimitive<Primitive, PrimitiveConsumer, Spl> {
            // The current spine index
            int splSpineIndex;

            // Last spine index
            final int lastSpineIndex;

            // The current element index into the current spine
            int splElementIndex;

            // Last spine's last element index + 1
            final int lastSpineElementFence;

            // When splSpineIndex >= lastSpineIndex and
            // splElementIndex >= lastSpineElementFence then
            // this spliterator is fully traversed
            // tryAdvance can set splSpineIndex > spineIndex if the last spine is full

            // The current spine array
            PrimitiveArray splChunk;

            public BaseSpinedBufferSpliterator(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpinedIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert spine != null || firstSpinedIndex == 0 && lastSpineIndex == 0;
                splChunk = (spine == null) ? current : spine[firstSpinedIndex];
            }

            abstract Spl newSpliterator(int firstSpineIndex, int lastSpineIndex,
                                        int firstSpineElementIndex, int lastSpineElementFence);

            abstract void arrayForOne(PrimitiveArray array, int index, PrimitiveConsumer consumer);

            abstract Spl arraySpliterator(PrimitiveArray array, int offset, int len);

            @Override
            public long estimateSize() {
                if (splSpineIndex == lastSpineIndex) {
                    return lastSpineElementFence - splElementIndex;
                } else {
                    return priorElementCount[lastSpineIndex] + lastSpineElementFence
                            - priorElementCount[splSpineIndex] - splElementIndex;
                }
            }

            @Override
            public int characteristics() {
                return SPLITERATOR_CHARACTERISTICS;
            }

            protected boolean haveNext() {
                return splSpineIndex < lastSpineIndex
                        || (splSpineIndex == lastSpineIndex && splElementIndex < lastSpineElementFence);
            }

            @Override
            public boolean tryAdvance(PrimitiveConsumer action) {
                Objects.requireNonNull(action);
                if (haveNext()) {
                    arrayForOne(splChunk, splElementIndex++, action);
                    if (splElementIndex == arrayLength(splChunk)) {
                        splElementIndex = 0;
                        ++splSpineIndex;
                        if (spine != null && splSpineIndex <= lastSpineIndex) {
                            splChunk = spine[spineIndex];
                        }
                    }

                    return true;
                }

                return false;
            }

            @Override
            public void forEachRemaining(PrimitiveConsumer action) {
                Objects.requireNonNull(action);
                if (haveNext()) {
                    int index = splElementIndex;
                    for (int i = index; i < lastSpineIndex; i++) {
                        PrimitiveArray array = spine[i];
                        arrayForEach(array, index, arrayLength(array), action);
                        index = 0;
                    }
                    PrimitiveArray array = (splSpineIndex == lastSpineIndex) ? splChunk : spine[lastSpineIndex];

                    arrayForEach(array, index, lastSpineElementFence, action);

                    splSpineIndex = lastSpineIndex;
                    splElementIndex = lastSpineElementFence;
                }
            }

            @Override
            public Spl trySplit() {

                if (splSpineIndex < lastSpineIndex) {
                    Spl spliterator = newSpliterator(splSpineIndex, lastSpineIndex - 1, splElementIndex, arrayLength(spine[lastSpineIndex]));

                    splSpineIndex = lastSpineIndex;
                    splElementIndex = 0;
                    splChunk = spine[splSpineIndex];
                    return spliterator;
                } else if (splSpineIndex == lastSpineIndex) {
                    int size = (lastSpineElementFence - splElementIndex) / 2;
                    if (size == 0) {
                        return null;
                    } else {
                        Spl spliterator = arraySpliterator(splChunk, splElementIndex, size);
                        splElementIndex += size;
                        return spliterator;
                    }
                }

                return null;
            }
        }
    }

    public static class OfByte extends SpinedBuffer.OfPrimitive<Byte, byte[], ByteConsumer> implements ByteConsumer {
        public OfByte(int initialCapacity) {
            super(initialCapacity);
        }

        public OfByte() {
        }

        @Override
        public void forEach(Consumer<? super Byte> consumer) {
            if (consumer instanceof ByteConsumer) {
                forEach((ByteConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public byte[][] newArrayArray(int size) {
            return new byte[size][];
        }

        @Override
        public byte[] newArray(int size) {
            return new byte[size];
        }

        @Override
        public int arrayLength(byte[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(byte[] array, int start, int end, ByteConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(byte value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public byte get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfByte iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfByte spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfByte>
                    implements Spliterator.OfByte {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfByte newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(byte[] array, int index, ByteConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfByte arraySpliterator(byte[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfShort extends SpinedBuffer.OfPrimitive<Short, short[], ShortConsumer> implements ShortConsumer {
        public OfShort(int initialCapacity) {
            super(initialCapacity);
        }

        public OfShort() {
        }

        @Override
        public void forEach(Consumer<? super Short> consumer) {
            if (consumer instanceof ShortConsumer) {
                forEach((ShortConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public short[][] newArrayArray(int size) {
            return new short[size][];
        }

        @Override
        public short[] newArray(int size) {
            return new short[size];
        }

        @Override
        public int arrayLength(short[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(short[] array, int start, int end, ShortConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(short value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public short get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfShort iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfShort spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfShort>
                    implements Spliterator.OfShort {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfShort newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(short[] array, int index, ShortConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfShort arraySpliterator(short[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfChar extends SpinedBuffer.OfPrimitive<Character, char[], CharConsumer> implements CharConsumer {
        public OfChar(int initialCapacity) {
            super(initialCapacity);
        }

        public OfChar() {
        }

        @Override
        public void forEach(Consumer<? super Character> consumer) {
            if (consumer instanceof CharConsumer) {
                forEach((CharConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public char[][] newArrayArray(int size) {
            return new char[size][];
        }

        @Override
        public char[] newArray(int size) {
            return new char[size];
        }

        @Override
        public int arrayLength(char[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(char[] array, int start, int end, CharConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(char value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public char get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfChar iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfChar spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfChar>
                    implements Spliterator.OfChar {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfChar newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(char[] array, int index, CharConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfChar arraySpliterator(char[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfInt extends SpinedBuffer.OfPrimitive<Integer, int[], IntConsumer> implements IntConsumer {
        public OfInt(int initialCapacity) {
            super(initialCapacity);
        }

        public OfInt() {
        }

        @Override
        public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public int[][] newArrayArray(int size) {
            return new int[size][];
        }

        @Override
        public int[] newArray(int size) {
            return new int[size];
        }

        @Override
        public int arrayLength(int[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(int[] array, int start, int end, IntConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(int value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public int get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfInt iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfInt spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfInt>
                    implements Spliterator.OfInt {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfInt newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(int[] array, int index, IntConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfInt arraySpliterator(int[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfLong extends SpinedBuffer.OfPrimitive<Long, long[], LongConsumer> implements LongConsumer {
        public OfLong(int initialCapacity) {
            super(initialCapacity);
        }

        public OfLong() {
        }

        @Override
        public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public long[][] newArrayArray(int size) {
            return new long[size][];
        }

        @Override
        public long[] newArray(int size) {
            return new long[size];
        }

        @Override
        public int arrayLength(long[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(long[] array, int start, int end, LongConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(long value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public long get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfLong iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfLong spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfLong>
                    implements Spliterator.OfLong {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfLong newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(long[] array, int index, LongConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfLong arraySpliterator(long[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfFloat extends SpinedBuffer.OfPrimitive<Float, float[], FloatConsumer> implements FloatConsumer {
        public OfFloat(int initialCapacity) {
            super(initialCapacity);
        }

        public OfFloat() {
        }

        @Override
        public void forEach(Consumer<? super Float> consumer) {
            if (consumer instanceof FloatConsumer) {
                forEach((FloatConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public float[][] newArrayArray(int size) {
            return new float[size][];
        }

        @Override
        public float[] newArray(int size) {
            return new float[size];
        }

        @Override
        public int arrayLength(float[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(float[] array, int start, int end, FloatConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(float value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public float get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfFloat iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfFloat spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfFloat>
                    implements Spliterator.OfFloat {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfFloat newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(float[] array, int index, FloatConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfFloat arraySpliterator(float[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }

    public static class OfDouble extends SpinedBuffer.OfPrimitive<Double, double[], DoubleConsumer> implements DoubleConsumer {
        public OfDouble(int initialCapacity) {
            super(initialCapacity);
        }

        public OfDouble() {
        }

        @Override
        public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        public double[][] newArrayArray(int size) {
            return new double[size][];
        }

        @Override
        public double[] newArray(int size) {
            return new double[size];
        }

        @Override
        public int arrayLength(double[] array) {
            return array.length;
        }

        @Override
        public void arrayForEach(double[] array, int start, int end, DoubleConsumer consumer) {
            for (int i = start; i < end; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(double value) {
            preAccept();
            current[elementIndex++] = value;
        }

        public double get(long index) {
            int chunk = chunkFor(index);
            if (spineIndex == 0 && chunk == 0) {
                return current[(int) index];
            } else {
                return spine[chunk][(int) (index - priorElementCount[chunk])];
            }
        }

        @Override
        public PrimitiveIterator.OfDouble iterator() {
            return Spliterators.iterator(spliterator());
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            class Adapter extends BaseSpinedBufferSpliterator<Spliterator.OfDouble>
                    implements Spliterator.OfDouble {
                public Adapter(int firstSpinedIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpinedIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Spliterator.OfDouble newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Adapter(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(double[] array, int index, DoubleConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfDouble arraySpliterator(double[] array, int offset, int len) {
                    return Spliterators.spliterator(array, offset, len + offset, Spliterator.ORDERED | Spliterator.IMMUTABLE);
                }
            }
            return new Adapter(0, spineIndex, 0, elementIndex);
        }
    }


}
