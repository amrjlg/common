/*
 *  Copyright (c) 2021-2021 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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
    public static final int MAX_BIT = 8;
    public static final int MIN_BIT = 1;

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


    public static int higherHalf(byte src) {
        // 0XF0=11110000
        return src & 0xF0;
    }


    public static int lowerHalf(byte src) {
        // 0X0F=00001111
        return src & 0x0F;
    }


    public static byte toByte(int src) {
        return (byte) (0XFF & src);
    }

    public static int toInt(byte src) {
        return src & 0XFF;
    }

    public static int[] toInts(byte[] bytes) {
        if (ArrayUtil.empty(bytes)) {
            return new int[0];
        }
        int count = bytes.length / 4 + 1;
        int[] numbers = new int[count];
        for (int i = 0; i < numbers.length; i++) {
            int j = i * 4;
            if (j >= bytes.length) {
                break;
            }

            int v = 0;
            v = v | toInt(bytes[j]);
            if (j + 1 >= bytes.length) {
                numbers[i] = v;
                break;
            }
            v = v << 8 | toInt(bytes[j + 1]);
            if (j + 2 >= bytes.length) {
                numbers[i] = v;
                break;
            }
            v = v << 8 | toInt(bytes[j + 2]);
            if (j + 3 >= bytes.length) {
                numbers[i] = v;
                break;
            }
            v = v << 8 | toInt(bytes[j + 3]);
            numbers[i] = v;
        }
        return numbers;
    }

    public static byte[] toBytes(int v) {
        byte a = (byte) ((v & 0xff000000) >> 24);
        byte b = (byte) ((v & 0xff0000) >> 16);
        byte c = (byte) ((v & 0xff00) >> 8);
        byte d = (byte) (v & 0xff);
        return ArrayUtil.array(a, b, c, d);
    }

    public static byte[] toBytes(long v) {
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

}
