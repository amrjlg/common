package io.github.amrjlg.utils;

import org.junit.jupiter.api.Test;

class ByteUtilTest {

    @Test
    void toHex() {
        //  1111111 -> 00000000000100001111010001000111

        // -1111111 -> 11111111111011110000101110111001
//        stringBuilder.append(HEX_VALUES[higherHalf(b)]);
//        stringBuilder.append(HEX_VALUES[lowerHalf(b)]);
        byte a = 0x1;
        byte b = (byte) 0xff;
        byte c = (byte) 0xFa;
        String hex = ByteUtil.toHex(a, b, c);
        System.out.println(hex);

        System.out.printf("%s%s ", HexUtil.HEX_VALUES[ByteUtil.higherHalf(a) >> 4], HexUtil.HEX_VALUES[ByteUtil.lowerHalf(a)]);
        System.out.printf("%s%s ", HexUtil.HEX_VALUES[ByteUtil.higherHalf(b) >> 4], HexUtil.HEX_VALUES[ByteUtil.lowerHalf(b)]);
        System.out.printf("%s%s", HexUtil.HEX_VALUES[ByteUtil.higherHalf(c) >> 4], HexUtil.HEX_VALUES[ByteUtil.lowerHalf(c)]);

    }
}