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

package io.github.amrjlg.util;

import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.CharSupplier;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class OptionalChar {
    private final char value;
    private final boolean present;

    public OptionalChar(char value) {
        this.value = value;
        this.present = true;
    }

    public OptionalChar() {
        this.value = 0;
        this.present = false;
    }

    private static final OptionalChar EMPTY = new OptionalChar();


    public static OptionalChar empty() {
        return EMPTY;
    }

    public static OptionalChar of(char value) {
        return new OptionalChar(value);
    }

    public boolean isPresent() {
        return present;
    }

    public void ifPresent(CharConsumer consumer) {
        if (present) {
            consumer.accept(value);
        }
    }

    public char getAsChar() {
        if (present) {
            return value;
        }
        throw new NoSuchElementException("No value present");
    }

    public char orElse(char value) {
        if (present) {
            return this.value;
        }
        return value;
    }

    public char orElseGet(CharSupplier supplier) {
        if (present) {
            return value;
        }
        return supplier.getAsChar();
    }

    public <E extends Throwable> char orElseThrow(Supplier<E> supplier) throws Throwable {
        if (present) {
            return value;
        }
        throw supplier.get();
    }

}
