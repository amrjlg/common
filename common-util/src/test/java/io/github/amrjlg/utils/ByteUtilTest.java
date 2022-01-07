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

import org.junit.jupiter.api.Test;

/**
 * @author amrjlg
 **/
class ByteUtilTest {

    @Test
    void toIntArray() {

        byte[] bytes = new byte[]{
                0x06, 0x06, 0x06, 0x06,
                0x06, 0x06,
        };
        int[] values = ByteUtil.toIntArray(bytes);
        for (int value : values) {
            System.out.println(value);
        }
        System.out.println(0x06060606);
        System.out.println(0x0606);

    }

    @Test
    void toLongArray() {
        byte[] bytes = new byte[]{
                (byte) 0x0F, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF,
                0x06, (byte) 0XFF, 0x06, (byte) 0XFF, (byte) 0xff
        };
        long[] values = ByteUtil.toLongArray(bytes);
        for (long value : values) {
            System.out.println(value);
        }
        System.out.println(0x0FFFFFFFFFFFFFFFL);
        System.out.println(0X06FF06FFFFL);
    }
}