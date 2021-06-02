package io.github.amrjlg.utils;

import static io.github.amrjlg.utils.ByteUtil.higherHalf;
import static io.github.amrjlg.utils.ByteUtil.lowerHalf;

public class HexUtil {

    public static final char[] HEX_VALUES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String parse(byte b) {
        return parse(ArrayUtil.array(b));
    }

    public static String parse(int v) {
        return parse(v, " ");
    }

    public static String parse(int v, CharSequence separator) {
        byte[] bytes = ByteUtil.toBytes(v);
        return parse(bytes, separator);
    }

    public static String parse(long v) {
        return parse(v, " ");
    }

    public static String parse(long v, CharSequence separator) {
        byte[] bytes = ByteUtil.toBytes(v);
        return parse(bytes);
    }

    public static String parse(byte... values) {
        return parse(values, " ");
    }
    /**
     * The byte array is parsed into hexadecimal strings, each separated by a specific sequence of characters
     *
     * @param values       fellow byte
     * @param separator the specific separate char sequence
     * @return hex string
     */
    public static String parse(byte[] values, CharSequence separator) {
        StringBuilder builder = new StringBuilder(values.length);
        for (byte value : values) {
            builder.append(HEX_VALUES[byteHigherIndex(value)]).append(HEX_VALUES[lowerHalf(value)]);
            if (separator != null) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static long toLong(String hex) {
        long number = 0L;

        int length = hex.length();
        for (int i = length - 1; i >= 0; i--) {
//        for (int i = 0; i < length; i++) {
            int value = toInt(hex.charAt(i));
            if (value == -1) {
                throw new IllegalArgumentException("not one hex number string");
            }
            long pow = 1L;
            int pow_time = length - i;
            for (int j = 1; j < pow_time; j++) {
                pow *= 16;
            }
            number = number + value * pow;
            // number *= 16;
            // number -= value;

        }
        return number;

    }

    public static int toInt(char hex) {
        for (int i = 0; i < HEX_VALUES.length; i++) {
            if (hex == HEX_VALUES[i]) {
                return i;
            }
        }
        return -1;
    }


    private static int byteHigherIndex(byte b) {
        return higherHalf(b) >> 4;
    }


}
