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

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.ByteSupplier;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @author amrjlg
 **/
public class OptionalByte {
    private final byte value;
    private final boolean present;

    public OptionalByte(byte value) {
        this.value = value;
        this.present = true;
    }

    public OptionalByte() {
        this.value = 0;
        this.present = false;
    }

    private static final OptionalByte EMPTY = new OptionalByte();


    public static OptionalByte empty() {
        return EMPTY;
    }

    public static OptionalByte of(byte value) {
        return new OptionalByte(value);
    }

    public boolean isPresent() {
        return present;
    }

    public void ifPresent(ByteConsumer consumer) {
        if (present) {
            consumer.accept(value);
        }

    }

    public byte getAsByte() {
        if (present) {
            return value;
        }
        throw new NoSuchElementException("No value present");
    }

    public byte orElse(byte value) {
        if (present) {
            return this.value;
        }
        return value;
    }

    public byte orElseGet(ByteSupplier supplier) {
        if (present) {
            return value;
        }
        return supplier.getAsByte();
    }

    public <E extends Throwable> byte orElseThrow(Supplier<E> supplier) throws Throwable {
        if (present) {
            return value;
        }
        throw supplier.get();
    }

}
