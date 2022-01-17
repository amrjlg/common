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

/**
 * @author amrjlg
 * @see java.util.stream.AbstractSpinedBuffer
 **/
public abstract class AbstractSpinedBuffer {

    /**
     * Minimum power-of-two for the first chunk.
     */
    public static final int MIN_CHUNK_POWER = 4;

    /**
     * Minimum size for the first chunk.
     */
    public static final int MIN_CHUNK_SIZE = 1 << MIN_CHUNK_POWER;

    /**
     * Max power-of-two for chunks.
     */
    public static final int MAX_CHUNK_POWER = 30;

    /**
     * Minimum array size for array-of-chunks.
     */
    public static final int MIN_SPINE_SIZE = 8;


    /**
     * log2 of the size of the first chunk.
     */
    protected final int initialChunkPower;

    /**
     * Index of the *next* element to write; may point into, or just outside of,
     * the current chunk.
     */
    protected int elementIndex;

    /**
     * Index of the *current* chunk in the spine array, if the spine array is
     * non-null.
     */
    protected int spineIndex;

    /**
     * Count of elements in all prior chunks.
     */
    protected long[] priorElementCount;

    protected AbstractSpinedBuffer() {
        this.initialChunkPower = MIN_CHUNK_POWER;
    }

    protected AbstractSpinedBuffer(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
        int power = Integer.numberOfLeadingZeros(initialCapacity - 1);
        this.initialChunkPower = Math.max(MIN_CHUNK_POWER, Integer.SIZE - power);
    }

    public boolean isEmpty() {
        return (spineIndex == 0) && (elementIndex == 0);
    }

    /**
     * How many elements are currently in the buffer?
     */
    public long count() {
        return (spineIndex == 0)
                ? elementIndex
                : priorElementCount[spineIndex] + elementIndex;
    }

    /**
     * How big should the nth chunk be?
     */
    protected int chunkSize(int n) {
        int power = (n == 0 || n == 1)
                ? initialChunkPower
                : Math.min(initialChunkPower + n - 1, AbstractSpinedBuffer.MAX_CHUNK_POWER);
        return 1 << power;
    }

    /**
     * Remove all data from the buffer
     */
    public abstract void clear();
}
