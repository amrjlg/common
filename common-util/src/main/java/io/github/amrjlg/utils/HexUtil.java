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
 *   limitations under the License.
 */

package io.github.amrjlg.utils;

/**
 * hex util
 *
 * @author amrjlg
 */
public class HexUtil {

    public static final char[] HEX_VALUES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final String[] FORMATTED_HEX_VALUES = new String[]{
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
            "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
            "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
            "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
            "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
            "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
            "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
    };

    public static String parse(byte b) {
        return parse(ArrayUtil.array(b));
    }

    public static String parse(int v) {
        return parse(v, " ");
    }

    public static String parse(int v, CharSequence separator) {
        byte[] bytes = ByteUtil.toByteArray(v);
        return parse(bytes, separator);
    }

    public static String parse(long v) {
        return parse(v, " ");
    }

    public static String parse(long v, CharSequence separator) {
        byte[] bytes = ByteUtil.toByteArray(v);
        return parse(bytes, separator);
    }

    public static String parse(CharSequence separator, byte... values) {
        return parse(values, separator);
    }

    public static String parse(byte... values) {
        return parse(values, " ");
    }

    /**
     * The byte array is parsed into hexadecimal strings, each separated by a specific sequence of characters
     *
     * @param values    fellow byte
     * @param separator the specific separate char sequence
     * @return hex string
     */
    public static String parse(byte[] values, CharSequence separator) {
        StringBuilder builder = new StringBuilder(values.length);
        for (byte value : values) {
            builder.append(FORMATTED_HEX_VALUES[ByteUtil.toInt(value)]);
            if (separator != null) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    /**
     * 将无符号16进制字符串转为long
     *
     * @param hex 16进制无符号 无间隔字符串
     * @return long value
     */
    public static long toLong(String hex, Character separator) {
        long number = 0L;

        int length = hex.length();
        for (int i = 0; i < length; i++) {
            char c = hex.charAt(i);
            if (separator == null || c == separator) {
                continue;
            }
            int value = toInt(c);
            if (value == -1) {
                throw new IllegalArgumentException("not one hex number string");
            }
            number = number * 16 - value;
        }
        return -number;
    }

    public static long toLong(String hex, CharSequence separator) {
        return toLong(hex.replace(separator, ""));
    }

    public static long toLong(String hex) {
        return toLong(hex, ' ');
    }

    public static int toInt(char hex) {
        int value = -1;
        for (int i = 0; i < HEX_VALUES.length; i++) {
            if (hex == HEX_VALUES[i]) {
                value = i;
                break;
            }
        }
        return value;
    }


}
