/*
 * Copyright (c) 2021-2022 the original author or authors.
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
 * limitations under the License.
 */

package io.github.amrjlg.exception.handler;

import io.github.amrjlg.exception.BaseException;
import io.github.amrjlg.exception.DefaultExceptionCodeMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ExceptionHandlerTest {

    @Test
    void handler() {
        List<ExceptionHandler<Exception>> handlers = Arrays.asList(new BaseExceptionHandler(), new DefaultExceptionHandler());


        List<Exception> exceptions = Arrays.asList(new NullPointerException(), new BaseException(DefaultExceptionCodeMessage.LOGICAL_ERROR) {
        });

        for (ExceptionHandler<Exception> handler : handlers) {
            System.out.println(handler.getClass().getSimpleName());
            for (Exception exception : exceptions) {
                System.out.println(exception.getClass().getSimpleName());
                if (handler.support(exception)) {
                    System.out.println("+");
                }else {
                    System.out.println("-");
                }
            }
        }

    }
}