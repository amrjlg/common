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

package io.github.amrjlg.stream.node;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.stream.buffer.SpinedBuffer;
import io.github.amrjlg.stream.iterator.Spliterator;


/**
 * @author amrjlg
 **/
public class CharSpinedNodeBuilder
        extends SpinedBuffer.OfChar
        implements NodeBuilder.OfChar, Node.OfChar {

    private boolean building = false;

    @Override
    public Spliterator.OfChar spliterator() {
        assert !building : "during building";
        return super.spliterator();
    }

    @Override
    public void forEach(CharConsumer consumer) {
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
    public void accept(char value) {
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
    public void copyInto(char[] array, int offset) throws IndexOutOfBoundsException {
        assert !building : "during building";
        super.copyInto(array, offset);
    }

    @Override
    public char[] asPrimitiveArray() {
        assert !building : "during building";
        return super.asPrimitiveArray();
    }

    @Override
    public Node.OfChar build() {
        assert !building : "during building";
        return this;
    }
}
