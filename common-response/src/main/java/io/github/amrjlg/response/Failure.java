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

package io.github.amrjlg.response;

import java.util.function.Supplier;
/**
 * failure result
 * <p>
 * 2021/3/4
 *
 * @author amrjlg
 **/
public interface Failure<T> extends Result<T> {

    @Override
    default int getCode() {
        return ResponseCodeMessage.FAILURE.getCode();
    }

    @Override
    default boolean isSuccess() {
        return false;
    }

    /**
     * default  with no data
     *
     * @return {@link T} with null
     */
    @Override
    default T getData() {
        return null;
    }

    /**
     * default implements
     *
     * @param msg failure message
     * @param <T> directed set null
     * @return failure result with message
     */
    static <T> Failure<T> failure(String msg) {
        return () -> msg;
    }

    /**
     * default failure
     *
     * @param <T> generic type
     * @return simple failure with default message
     */
    static <T> Failure<T> failure() {
        return ResponseCodeMessage.FAILURE::getMessage;
    }

    /**
     * build with supplier
     *
     * @param supplier supplier failure message
     * @param <T>      generic type for response
     * @return failure with message
     */
    static <T> Failure<T> failure(Supplier<String> supplier) {
        return supplier::get;
    }
}
