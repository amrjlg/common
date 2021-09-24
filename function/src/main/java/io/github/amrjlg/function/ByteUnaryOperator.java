/*
 * Copyright (c) 2021-2021 the original author or authors.
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
 *  limitations under the License.
 *
 */

package io.github.amrjlg.function;

import java.util.Objects;

/**
 * byte 一元运算
 *
 * @author amrjlg
 **/
public interface ByteUnaryOperator {
    /**
     * 运算操作
     *
     * @param value 操作数
     * @return 运算结果
     */
    byte applyAsByte(byte value);

    /**
     * 前置运算
     *
     * @param before 前置运算
     * @return ByteUnaryOperator
     */
    default ByteUnaryOperator compose(ByteUnaryOperator before) {
        Objects.requireNonNull(before);
        return v -> applyAsByte(before.applyAsByte(v));
    }

    /**
     * 后置运算
     *
     * @param after 后置运算
     * @return ByteUnaryOperator
     */
    default ByteUnaryOperator andThen(ByteUnaryOperator after) {
        Objects.requireNonNull(after);
        return t -> after.applyAsByte(applyAsByte(t));
    }

    /**
     * 原样输出
     *
     * @return ByteUnaryOperator
     */
    static ByteUnaryOperator identity() {
        return t -> t;
    }
}
