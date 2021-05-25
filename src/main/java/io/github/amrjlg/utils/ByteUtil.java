package io.github.amrjlg.utils;

/**
 * the byte utils
 * <p>
 * 2021/3/2
 *
 * @author jiang
 **/
public class ByteUtil {
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
        if (bit < 1 || bit > 8) {
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

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            if (hv.length() > 2) {
                hv = hv.substring(hv.length() - 2);
            }
            stringBuilder.append(hv);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }


    public static int higherHalf(byte src) {
        // 0XF0=11110000
        return src & 0xF0;
    }

    public static int higherHalf(int src) {
        return src & 0xFFFF0000;
    }

    public static int lowerHalf(byte src) {
        // 0X0F=00001111
        return src & 0x0F;
    }

    public static int lowerHalf(int src) {
        // 0X0F=00001111
        return src & 0x0000FFFF;
    }

    public static byte toByte(int src) {
         return (byte) (0XFF & src);
    }

    public static int toInt(byte src) {
        return src & 0XFF;
    }

}
