/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.response;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class ResultTest {

    @Test
    void result() {
        Failure<String> failure = Failure.failure();
        System.out.println(failure.getMessage());
        System.out.println(Failure.failure("failure message").getMessage());

        System.out.println(Success.ok(Collections::emptyList).getData());
        System.out.println(Success.ok(failure::getCode).getData());
        System.out.println(Success.ok(Arrays::asList).getData());

        System.out.println(Success.ok(Collections.emptyList()).getData());
        System.out.println(Arrays.toString(Success.ok(new int[]{1}).getData()));
    }
}