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

import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.EnumMap;
import java.util.Map;


/**
 * @see java.util.stream.StreamOpFlag
 */
public enum StreamOpFlag {


    // 0, 0x00000001
    // Matches Spliterator.DISTINCT
    DISTINCT(0,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),


    // 1, 0x00000004
    // Matches Spliterator.SORTED
    SORTED(1,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),


    // 2, 0x00000010
    // Matches Spliterator.ORDERED
    ORDERED(2,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP).clear(Type.TERMINAL_OP)
                    .clear(Type.UPSTREAM_TERMINAL_OP)),


    // 3, 0x00000040
    // Matches Spliterator.SIZED
    SIZED(3,
            set(Type.SPLITERATOR).set(Type.STREAM).clear(Type.OP)),


    // 12, 0x01000000
    SHORT_CIRCUIT(12,
            set(Type.OP).set(Type.TERMINAL_OP));

    // The following 2 flags are currently undefined and a free for any further
    // stream flags if/when required
    //
    // 13, 0x04000000
    // 14, 0x10000000
    // 15, 0x40000000

    /**
     * Type of a flag
     */
    public enum Type {
        /**
         * The flag is associated with spliterator characteristics.
         */
        SPLITERATOR,

        /**
         * The flag is associated with stream flags.
         */
        STREAM,

        /**
         * The flag is associated with intermediate operation flags.
         */
        OP,

        /**
         * The flag is associated with terminal operation flags.
         */
        TERMINAL_OP,

        /**
         * The flag is associated with terminal operation flags that are
         * propagated upstream across the last stateful operation boundary
         */
        UPSTREAM_TERMINAL_OP
    }

    /**
     * The bit pattern for setting/injecting a flag.
     */
    public static final int SET_BITS = 0b01;

    /**
     * The bit pattern for clearing a flag.
     */
    public static final int CLEAR_BITS = 0b10;

    /**
     * The bit pattern for preserving a flag.
     */
    public static final int PRESERVE_BITS = 0b11;

    public static MaskBuilder set(Type t) {
        return new MaskBuilder(new EnumMap<>(Type.class)).set(t);
    }

    public static class MaskBuilder {
        final Map<Type, Integer> map;

        MaskBuilder(Map<Type, Integer> map) {
            this.map = map;
        }

        MaskBuilder mask(Type t, Integer i) {
            map.put(t, i);
            return this;
        }

        MaskBuilder set(Type t) {
            return mask(t, SET_BITS);
        }

        MaskBuilder clear(Type t) {
            return mask(t, CLEAR_BITS);
        }

        MaskBuilder setAndClear(Type t) {
            return mask(t, PRESERVE_BITS);
        }

        Map<Type, Integer> build() {
            for (Type t : Type.values()) {
                map.putIfAbsent(t, 0b00);
            }
            return map;
        }
    }

    /**
     * The mask table for a flag, this is used to determine if a flag
     * corresponds to a certain flag type and for creating mask constants.
     */
    public final Map<Type, Integer> maskTable;

    /**
     * The bit position in the bit mask.
     */
    public final int bitPosition;

    /**
     * The set 2 bit set offset at the bit position.
     */
    public final int set;

    /**
     * The clear 2 bit set offset at the bit position.
     */
    public final int clear;

    /**
     * The preserve 2 bit set offset at the bit position.
     */
    public final int preserve;

    StreamOpFlag(int position, MaskBuilder maskBuilder) {
        this.maskTable = maskBuilder.build();
        // Two bits per flag
        position *= 2;
        this.bitPosition = position;
        this.set = SET_BITS << position;
        this.clear = CLEAR_BITS << position;
        this.preserve = PRESERVE_BITS << position;
    }

    /**
     * Gets the bitmap associated with setting this characteristic.
     *
     * @return the bitmap for setting this characteristic
     */
    public int set() {
        return set;
    }

    /**
     * Gets the bitmap associated with clearing this characteristic.
     *
     * @return the bitmap for clearing this characteristic
     */
    public int clear() {
        return clear;
    }

    /**
     * Determines if this flag is a stream-based flag.
     *
     * @return true if a stream-based flag, otherwise false.
     */
    public boolean isStreamFlag() {
        return maskTable.get(Type.STREAM) > 0;
    }

    /**
     * Checks if this flag is set on stream flags, injected on operation flags,
     * and injected on combined stream and operation flags.
     *
     * @param flags the stream flags, operation flags, or combined stream and
     *              operation flags
     * @return true if this flag is known, otherwise false.
     */
    public boolean isKnown(int flags) {
        return (flags & preserve) == set;
    }

    /**
     * Checks if this flag is cleared on operation flags or combined stream and
     * operation flags.
     *
     * @param flags the operation flags or combined stream and operations flags.
     * @return true if this flag is preserved, otherwise false.
     */
    public boolean isCleared(int flags) {
        return (flags & preserve) == clear;
    }

    /**
     * Checks if this flag is preserved on combined stream and operation flags.
     *
     * @param flags the combined stream and operations flags.
     * @return true if this flag is preserved, otherwise false.
     */
    public boolean isPreserved(int flags) {
        return (flags & preserve) == preserve;
    }

    /**
     * Determines if this flag can be set for a flag type.
     *
     * @param t the flag type.
     * @return true if this flag can be set for the flag type, otherwise false.
     */
    public boolean canSet(Type t) {
        return (maskTable.get(t) & SET_BITS) > 0;
    }

    /**
     * The bit mask for spliterator characteristics
     */
    public static final int SPLITERATOR_CHARACTERISTICS_MASK = createMask(Type.SPLITERATOR);

    /**
     * The bit mask for source stream flags.
     */
    public static final int STREAM_MASK = createMask(Type.STREAM);

    /**
     * The bit mask for intermediate operation flags.
     */
    public static final int OP_MASK = createMask(Type.OP);

    /**
     * The bit mask for terminal operation flags.
     */
    public static final int TERMINAL_OP_MASK = createMask(Type.TERMINAL_OP);

    /**
     * The bit mask for upstream terminal operation flags.
     */
    public static final int UPSTREAM_TERMINAL_OP_MASK = createMask(Type.UPSTREAM_TERMINAL_OP);

    public static int createMask(Type t) {
        int mask = 0;
        for (StreamOpFlag flag : StreamOpFlag.values()) {
            mask |= flag.maskTable.get(t) << flag.bitPosition;
        }
        return mask;
    }

    /**
     * Complete flag mask.
     */
    public static final int FLAG_MASK = createFlagMask();

    public static int createFlagMask() {
        int mask = 0;
        for (StreamOpFlag flag : StreamOpFlag.values()) {
            mask |= flag.preserve;
        }
        return mask;
    }

    /**
     * Flag mask for stream flags that are set.
     */
    public static final int FLAG_MASK_IS = STREAM_MASK;

    /**
     * Flag mask for stream flags that are cleared.
     */
    public static final int FLAG_MASK_NOT = STREAM_MASK << 1;

    /**
     * The initial value to be combined with the stream flags of the first
     * stream in the pipeline.
     */
    public static final int INITIAL_OPS_VALUE = FLAG_MASK_IS | FLAG_MASK_NOT;

    /**
     * The bit value to set or inject {@link #DISTINCT}.
     */
    public static final int IS_DISTINCT = DISTINCT.set;

    /**
     * The bit value to clear {@link #DISTINCT}.
     */
    public static final int NOT_DISTINCT = DISTINCT.clear;

    /**
     * The bit value to set or inject {@link #SORTED}.
     */
    public static final int IS_SORTED = SORTED.set;

    /**
     * The bit value to clear {@link #SORTED}.
     */
    public static final int NOT_SORTED = SORTED.clear;

    /**
     * The bit value to set or inject {@link #ORDERED}.
     */
    public static final int IS_ORDERED = ORDERED.set;

    /**
     * The bit value to clear {@link #ORDERED}.
     */
    public static final int NOT_ORDERED = ORDERED.clear;

    /**
     * The bit value to set {@link #SIZED}.
     */
    public static final int IS_SIZED = SIZED.set;

    /**
     * The bit value to clear {@link #SIZED}.
     */
    public static final int NOT_SIZED = SIZED.clear;

    /**
     * The bit value to inject {@link #SHORT_CIRCUIT}.
     */
    public static final int IS_SHORT_CIRCUIT = SHORT_CIRCUIT.set;

    public static int getMask(int flags) {
        return (flags == 0)
                ? FLAG_MASK
                : ~(flags | ((FLAG_MASK_IS & flags) << 1) | ((FLAG_MASK_NOT & flags) >> 1));
    }

    public static int combineOpFlags(int newStreamOrOpFlags, int prevCombOpFlags) {
        // 0x01 or 0x10 nibbles are transformed to 0x11
        // 0x00 nibbles remain unchanged
        // Then all the bits are flipped
        // Then the result is logically or'ed with the operation flags.
        return (prevCombOpFlags & StreamOpFlag.getMask(newStreamOrOpFlags)) | newStreamOrOpFlags;
    }

    public static int toStreamFlags(int combOpFlags) {
        // By flipping the nibbles 0x11 become 0x00 and 0x01 become 0x10
        // Shift left 1 to restore set flags and mask off anything other than the set flags
        return ((~combOpFlags) >> 1) & FLAG_MASK_IS & combOpFlags;
    }

    public static int toCharacteristics(int streamFlags) {
        return streamFlags & SPLITERATOR_CHARACTERISTICS_MASK;
    }

    public static int fromCharacteristics(Spliterator<?> spliterator) {
        int characteristics = spliterator.characteristics();
        if ((characteristics & Spliterator.SORTED) != 0 && spliterator.getComparator() != null) {
            // Do not propagate the SORTED characteristic if it does not correspond
            // to a natural sort order
            return characteristics & SPLITERATOR_CHARACTERISTICS_MASK & ~Spliterator.SORTED;
        } else {
            return characteristics & SPLITERATOR_CHARACTERISTICS_MASK;
        }
    }

    public static int fromCharacteristics(int characteristics) {
        return characteristics & SPLITERATOR_CHARACTERISTICS_MASK;
    }
}
