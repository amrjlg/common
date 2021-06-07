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

package io.github.amrjlg.processor.exception;

import io.github.amrjlg.processor.Handler;

/**
 * exception handler
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface ExceptionHandler<T extends Exception> extends Handler<String, T> {
    /**
     * 处理异常
     *
     * @param e 异常
     * @return 异常处理结果
     */
    @Override
    String resolve(T e);

}
