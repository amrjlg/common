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

import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.function.ShortSupplier;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class OptionalShort {

    private final boolean present;
    private final short value;

    public OptionalShort() {
        present = false;
        value = 0;
    }

    private static final OptionalShort EMPTY = new OptionalShort();

    public OptionalShort(short value) {
        this.present = true;
        this.value = value;
    }

    public short getAsShort() {
        if (present) {
            return value;
        }
        throw new NoSuchElementException("No value present");
    }

    public static OptionalShort of(short value) {
        return new OptionalShort(value);
    }

    public static OptionalShort empty() {
        return EMPTY;
    }

    public boolean isPresent() {
        return present;
    }

    public void ifPresent(ShortConsumer consumer) {
        if (present)
            consumer.accept(value);
    }

    public short orElse(short other) {
        return present ? value : other;
    }

    public short orElseGet(ShortSupplier other) {
        return present ? value : other.getAsShort();
    }


    public <X extends Throwable> short orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (present) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }
}
