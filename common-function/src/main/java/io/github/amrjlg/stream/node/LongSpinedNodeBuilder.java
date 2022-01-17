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

package io.github.amrjlg.stream.node;


import io.github.amrjlg.stream.buffer.SpinedBuffer;
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.function.LongConsumer;


/**
 * @author amrjlg
 **/
public class LongSpinedNodeBuilder
        extends SpinedBuffer.OfLong
        implements NodeBuilder.OfLong, Node.OfLong {

    private boolean building = false;

    @Override
    public Spliterator.OfLong spliterator() {
        assert !building : "during building";
        return super.spliterator();
    }

    @Override
    public void forEach(LongConsumer consumer) {
        assert !building : "during building";
        super.forEach(consumer);
    }

    //
    @Override
    public void begin(long size) {
        assert !building : "was already building";
        building = true;
        clear();
        ensureCapacity(size);
    }

    @Override
    public void accept(long value) {
        assert building : "not building";
        super.accept(value);
    }


    @Override
    public void end() {
        assert building : "was not building";
        building = false;
        // @@@ check begin(size) and size
    }

    @Override
    public void copyInto(long[] array, int offset) throws IndexOutOfBoundsException {
        assert !building : "during building";
        super.copyInto(array, offset);
    }

    @Override
    public long[] asPrimitiveArray() {
        assert !building : "during building";
        return super.asPrimitiveArray();
    }

    @Override
    public Node.OfLong build() {
        assert !building : "during building";
        return this;
    }
}
