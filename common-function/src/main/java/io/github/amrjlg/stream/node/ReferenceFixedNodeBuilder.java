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

import java.util.function.IntFunction;

import static io.github.amrjlg.stream.node.Nodes.maxArraySize;
import static io.github.amrjlg.stream.node.Nodes.sameCount;

public final class ReferenceFixedNodeBuilder<T> extends ReferenceArrayNode<T> implements NodeBuilder<T> {

        public ReferenceFixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
            maxArraySize(size);
        }

        @Override
        public Node<T> build() {
            sameCount(currentSize, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length)
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                        size, array.length));
            currentSize = 0;
        }

        @Override
        public void accept(T t) {
            if (currentSize < array.length) {
                array[currentSize++] = t;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(currentSize, array.length);
        }

    }