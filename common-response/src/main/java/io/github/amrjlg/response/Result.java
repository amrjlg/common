/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.response;

import io.github.amrjlg.api.CodeMessage;

/**
 * common result
 * <p>
 * 2021/3/4
 *
 * @author amrjlg
 **/
public interface Result<T> extends CodeMessage {
    /**
     * status code
     *
     * @return <code>int</code>
     */
    default int getCode() {
        return 200;
    }

    /**
     * judge this result is successful
     *
     * @return true success false failure
     */
    boolean isSuccess();

    /**
     * the message for this result
     *
     * @return message
     */
    @Override
    String getMessage();

    /**
     * real data for the result
     *
     * @return data
     */
    T getData();
}
