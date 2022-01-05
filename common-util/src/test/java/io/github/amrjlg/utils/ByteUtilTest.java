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
    void toInts() {

        byte[] bytes = new byte[]{

                0x06,0x06,
        };
        int[] ints = ByteUtil.toInts(bytes);
        for (int i = 0; i < ints.length; i++) {
            System.out.println(ints[i]);
        }
    }
}