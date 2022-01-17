/*
 *  Copyright (c) 2021-2022 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.amrjlg.utils;


/**
 * the byte utils
 * <p>
 * 2021/3/2
 *
 * @author amrjlg
 **/
public class ByteUtil {
    public static final int MIN_BIT = 1;
    public static final int MAX_BIT = 8;

    public static final int BYTE_BIT_WIDTH = 8;

    public static final int INTEGER_BYTE_WIDTH = 4;
    public static final int INTEGER_BIT_WIDTH = BYTE_BIT_WIDTH * INTEGER_BYTE_WIDTH;
    public static final int LONG_BYTE_WIDTH = 8;
    public static final int LONG_BIT_WIDTH = BYTE_BIT_WIDTH * LONG_BYTE_WIDTH;

    /**
     * Gets the specified location(range 1 to 8) of the byte
     *
     * <pre>{@code
     *  // src: 1 0 0 0 0 0 0 0
     *  // pos: 8 7 6 5 4 3 2 1
     *
     *     src = 1<<8
     *     bit(src,8) -> 1
     *     bit(src,1) -> 0
     * }</pre>
     *
     * @param src the byte
     * @param bit the bit location range [1,8]
     * @return int  bit value 1 or 0
     */
    public static int bit(byte src, int bit) {
        if (bit < MIN_BIT || bit > MAX_BIT) {
            return 0;
        }
        return (src >> (bit - 1)) & 1;
    }

    /**
     * get first bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_1(byte src) {
        // 0x1  = 00000001
        // 1
        return src & 1;
    }

    /**
     * get second bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_2(byte src) {
        // 0x2  = 00000010
        // 2
        return (src & 2) >> 1;
    }

    /**
     * get third bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_3(byte src) {
        // 0x4  = 00000100
        // 4
        return (src & 4) >> 2;
    }

    /**
     * get fourth bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_4(byte src) {
        // 0x8  = 00001000
        // 8
        return (src & 8) >> 3;
    }

    /**
     * get fifth bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_5(byte src) {
        // 0x10 = 00010000
        // 16
        return (src & 16) >> 4;
    }

    /**
     * get sixth bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_6(byte src) {
        // 0x20 = 00100000
        // 32
        return (src & 32) >> 5;
    }

    /**
     * get seventh bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_7(byte src) {
        // 0x40 = 01000000
        // 64
        return (src & 64) >> 6;
    }

    /**
     * get eighth bit value of the byte
     *
     * @param src the byte
     * @return int byte value (0 or 1)
     */
    public static int getBit_8(byte src) {
        // 0x80 = 10000000
        // 128
        return (src & 128) >> 7;
    }

    /**
     * Parses the byte array into a hexadecimal string, with each hexadecimal separated by a space character
     *
     * @param src byte array
     * @return hex string
     */
    public static String toHex(byte... src) {
        if (ArrayUtil.empty(src)) {
            return "";
        }
        return toHex(src, " ");
    }

    /**
     * The byte array is parsed into hexadecimal strings, each separated by a specific sequence of characters
     *
     * @param src       fellow byte
     * @param separator the specific separate char sequence
     * @return hex string
     */
    public static String toHex(byte[] src, CharSequence separator) {
        return HexUtil.parse(src, separator);
    }

    /**
     * get byte high 4 bit as int value
     *
     * @param src byte
     * @return high 4 bit as int value
     */
    public static int lowerHalf(byte src) {
        // 0X0F=00001111
        return src & 0x0F;
    }

    /**
     * get byte low 4 bit as int value
     *
     * @param src byte
     * @return low 4 bit as int value
     */
    public static int higherHalf(byte src) {
        // 0XF0=11110000
        return src & 0xF0;
    }

    /**
     * parse byte to int
     *
     * @param src byte
     * @return the byte plain as int
     */
    public static int toInt(byte src) {
        return src & 0XFF;
    }

    /**
     * parse byte array to int <br />
     * ex. {@code  0x06 0xFF -> 0x06<< 8 | 0xFF -> 0x06FF}
     *
     * @param bytes array
     * @param start start index
     * @param end   end position
     * @return the byte array form start to end plain as int
     * @throws IndexOutOfBoundsException if start less than 0 or start bigger equal end or end bigger equal array length
     * @throws IllegalArgumentException  if end - start > {@value INTEGER_BYTE_WIDTH}
     */
    public static int toInt(byte[] bytes, int start, int end) {
        ArrayUtil.rangeCheck(bytes.length, start, end);
        if (end - start > INTEGER_BYTE_WIDTH) {
            throw new IllegalArgumentException("integer must be less or equal 4 byte");
        }
        int num = 0;
        for (int i = start; i < end; i++) {
            num = (num << BYTE_BIT_WIDTH) | toInt(bytes[i]);
        }
        return num;
    }

    /**
     * parse byte array to int array
     *
     * @param bytes byte array
     * @return int array
     */
    public static int[] toIntArray(byte[] bytes) {
        if (ArrayUtil.empty(bytes)) {
            return new int[0];
        }
        int count = computeCount(bytes.length, INTEGER_BYTE_WIDTH);
        int[] numbers = new int[count];
        for (int i = 0; i < numbers.length; i++) {
            int start = i * INTEGER_BYTE_WIDTH;
            numbers[i] = toInt(bytes, start, Math.min(start + INTEGER_BYTE_WIDTH, bytes.length));
        }
        return numbers;
    }

    /**
     * parse byte to int
     *
     * @param bytes byte array
     * @return the byte plain as long
     */
    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0, bytes.length);
    }

    /**
     * parse byte array to int <br />
     * ex. {@code  0x06 0xFF -> 0x06<< 8 | 0xFF -> 0x06FF}
     *
     * @param bytes array
     * @param start start index
     * @param end   end position
     * @return the byte array form start to end plain as long
     * @throws IndexOutOfBoundsException if start less than 0 or start bigger equal end or end bigger equal array length
     * @throws IllegalArgumentException  if end - start > {@value LONG_BYTE_WIDTH}
     */
    public static long toLong(byte[] bytes, int start, int end) {
        ArrayUtil.rangeCheck(bytes.length, start, end);
        if (end - start > LONG_BYTE_WIDTH) {
            throw new IllegalArgumentException("integer must be less or equal 8 byte");
        }
        long num = 0;
        for (int i = start; i < end; i++) {
            num = (num << BYTE_BIT_WIDTH) | toInt(bytes[i]);
        }
        return num;
    }

    public static long[] toLongArray(byte[] bytes) {
        int count = computeCount(bytes.length, LONG_BYTE_WIDTH);

        long[] numbers = new long[count];
        for (int i = 0; i < numbers.length; i++) {
            int start = i * LONG_BYTE_WIDTH;
            numbers[i] = toLong(bytes, start, Math.min(start + LONG_BYTE_WIDTH, bytes.length));
        }

        return numbers;
    }


    /**
     * parse int to byte
     *
     * @param src int
     * @return the int value lower {@value BYTE_BIT_WIDTH} bit to byte
     */
    public static byte toByte(int src) {
        return (byte) (0XFF & src);
    }

    /**
     * parse int to byte
     *
     * @param src long
     * @return the int value lower {@value BYTE_BIT_WIDTH} bit to byte
     */
    public static byte toByte(long src) {
        return (byte) (0XFF & src);
    }

    /**
     * parse int each {@value BYTE_BIT_WIDTH} bit to one byte and build to array
     *
     * @param v int value
     * @return the array of byte
     */
    public static byte[] toByteArray(int v) {
        byte a = (byte) ((v & 0xff000000) >> 24);
        byte b = (byte) ((v & 0xff0000) >> 16);
        byte c = (byte) ((v & 0xff00) >> 8);
        byte d = (byte) (v & 0xff);
        return ArrayUtil.array(a, b, c, d);
    }

    /**
     * parse int each {@value BYTE_BIT_WIDTH} bit to one byte and build to array
     *
     * @param v long value
     * @return the array of byte
     */
    public static byte[] toByteArray(long v) {
        byte a = (byte) ((v & 0xff00000000000000L) >> 56);
        byte b = (byte) ((v & 0xff000000000000L) >> 48);
        byte c = (byte) ((v & 0xff0000000000L) >> 40);
        byte d = (byte) ((v & 0xff00000000L) >> 32);
        byte e = (byte) ((v & 0xff000000) >> 24);
        byte f = (byte) ((v & 0xff0000) >> 16);
        byte g = (byte) ((v & 0xff00) >> 8);
        byte h = (byte) ((v & 0xff));
        return ArrayUtil.array(a, b, c, d, e, f, g, h);
    }

    /**
     * compute count
     *
     * @param length total
     * @param width  member width
     * @return the count of length separate by width
     */
    public static int computeCount(int length, int width) {
        int count = length / width;
        if (count * width < length) {
            count++;
        }
        return count;
    }
}
