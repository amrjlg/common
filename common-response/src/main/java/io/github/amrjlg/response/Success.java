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


import java.util.Collection;
import java.util.function.Supplier;

/**
 * Success result
 * <p>
 * 2021/3/4
 *
 * @author amrjlg
 * @see Result
 **/
public interface Success<T> extends Result<T> {

    @Override
    default int getCode() {
        return ResponseCodeMessage.SUCCESS.getCode();
    }

    @Override
    default boolean isSuccess() {
        return true;
    }

    @Override
    default String getMessage() {
        return ResponseCodeMessage.SUCCESS.getMessage();
    }

    /**
     * Result with no data
     *
     * @param <T> dat type
     * @return {@link Result}
     */
    static <T> Success<T> ok() {
        return () -> null;
    }

    /**
     * Single Result
     *
     * @param data response data could be null
     * @param <T>  type
     * @return success with single data
     */
    static <T> Success<T> ok(T data) {
        if (data == null) {
            return ok();
        }
        return () -> data;
    }

    static <T> Success<T> ok(Supplier<T> supplier) {
        return supplier::get;
    }
}
