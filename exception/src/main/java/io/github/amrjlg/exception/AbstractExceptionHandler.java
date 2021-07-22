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

package io.github.amrjlg.exception;

import io.github.amrjlg.api.Support;

/**
 * 异常处理抽象类
 * <br>
 * 针对不同异常进行响应实现
 *
 * @param <T> type of exception
 * @author amrjlg
 */
public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandler<T> {

    private final Support<Class<? extends Exception>> predicate;

    public AbstractExceptionHandler(Support<Class<? extends Exception>> predicate) {
        this.predicate = predicate;
    }

    public AbstractExceptionHandler(Class<? extends Exception> tClass) {
        this.predicate = tClass::isAssignableFrom;
    }

    @Override
    public final boolean support(Exception e) {
        if (e == null) {
            return false;
        }
        return predicate.support(e.getClass());
    }
}