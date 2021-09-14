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

public final class ByteFixedNodeBuilder extends ByteArrayNode implements NodeBuilder.OfByte {

    public ByteFixedNodeBuilder(long size) {
        super(size);
    }

    @Override
    public Node.OfByte build() {
        Nodes.sameCount(index, array.length);
        return this;
    }

    @Override
    public void begin(long size) {
        Nodes.sameCount((int) size, array.length);
        index = 0;
    }

    @Override
    public void accept(byte value) {
        if (index < array.length) {
            array[index++] = value;
        } else {
            throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                    array.length));
        }
    }

    @Override
    public void end() {
        Nodes.sameCount(index, array.length);
    }
}
