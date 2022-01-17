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
 * limitations under the License.
 *
 */

package io.github.amrjlg.util;

import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.FloatSupplier;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class OptionalFloat {

    private final float value;
    private final boolean present;

    public OptionalFloat(float value) {
        this.value = value;
        this.present = true;
    }

    public OptionalFloat() {
        this.value = 0;
        this.present = false;
    }

    private static final OptionalFloat EMPTY = new OptionalFloat();


    public static OptionalFloat empty() {
        return EMPTY;
    }

    public static OptionalFloat of(float value) {
        return new OptionalFloat(value);
    }

    public boolean isPresent() {
        return present;
    }

    public void ifPresent(FloatConsumer consumer) {
        if (present) {
            consumer.accept(value);
        }
    }

    public float getAsFloat() {
        if (present) {
            return value;
        }
        throw new NoSuchElementException("No value present");
    }
    public float orElse(float value) {
        if (present) {
            return this.value;
        }
        return value;
    }

    public float orElseGet(FloatSupplier supplier) {
        if (present) {
            return value;
        }
        return supplier.getAsFloat();
    }

    public <E extends Throwable> float orElseThrow(Supplier<E> supplier) throws Throwable {
        if (present) {
            return value;
        }
        throw supplier.get();
    }
}
