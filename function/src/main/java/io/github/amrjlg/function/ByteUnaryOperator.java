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
 * @author amrjlg
 **/
public interface ByteUnaryOperator {

    byte applyAsByte(byte b);


    default ByteUnaryOperator compose(ByteUnaryOperator before) {
        Objects.requireNonNull(before);
        return v -> applyAsByte(before.applyAsByte(v));
    }


    default ByteUnaryOperator andThen(ByteUnaryOperator after) {
        Objects.requireNonNull(after);
        return t -> after.applyAsByte(applyAsByte(t));
    }

    static ByteUnaryOperator identity() {
        return t -> t;
    }
}
