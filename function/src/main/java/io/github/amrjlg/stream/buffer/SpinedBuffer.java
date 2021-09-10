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

package io.github.amrjlg.stream.buffer;

import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;
import io.github.amrjlg.stream.node.Nodes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import java.util.function.Consumer;
import java.util.function.IntFunction;

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
                        Spliterator<Element> spliterator = Spliterators.spliterator(splChunk, splElementIndex, splElementIndex + end, java.util.Spliterator.ORDERED | java.util.Spliterator.IMMUTABLE);

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
        public abstract void forEach(Consumer<? super Primitive> action);

        protected abstract PrimitiveArray[] newArrayArray(int size);

        protected abstract PrimitiveArray newArray(int size);

        protected abstract int arrayLength(PrimitiveArray array);

        protected abstract void arrayForEach(PrimitiveArray array, int start, int end, PrimitiveConsumer consumer);

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
                for (int i = spineIndex + 1; targetSize > capacity ;i++ ){
                    if (i >= spine.length){
                        int size = spine.length * 2;
                        spine = Arrays.copyOf(spine,size);
                        priorElementCount = Arrays.copyOf(priorElementCount,size);
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

        protected  int chunkFor(long index){
            if (spineIndex == 0){
                if (index < elementIndex){
                    return 0;
                }
                throw new IndexOutOfBoundsException(Long.toString(index));
            }

            if (index >= count()){
                throw new IndexOutOfBoundsException(Long.toString(index));
            }

            for (int i = 0; i <= spineIndex; i++) {
                if (index < priorElementCount[i] + arrayLength(spine[i])){
                    return i;
                }
            }

            throw new IndexOutOfBoundsException(Long.toString(index));

        }
        public void copyInto(PrimitiveArray array,int offset){
            long end = offset + count();
            if (end > arrayLength(array) || end < offset){
                throw new IndexOutOfBoundsException("does not fit");
            }

            if (spineIndex == 0){
                System.arraycopy(current,0,array,offset,elementIndex);
            }else {

                for (int i = 0; i < spineIndex; i++) {
                    PrimitiveArray primitiveArray = spine[i];
                    int length = arrayLength(primitiveArray);
                    System.arraycopy(primitiveArray,0,array,offset, length);
                    offset += length;
                }
                if (elementIndex >0){
                    System.arraycopy(current,0,array,offset,elementIndex);
                }
            }

        }

        public PrimitiveArray asPrimitiveArray(){
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
                if (spineIndex+1 >= spine.length || spine[spineIndex+1] == null)
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

        public void forEach(PrimitiveConsumer consumer){
            for (int i = 0; i < spineIndex; i++) {
                arrayForEach(spine[i],0,arrayLength(spine[i]),consumer);
            }
            arrayForEach(current,0,elementIndex,consumer);
        }
    }

}
