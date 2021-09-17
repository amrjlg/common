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

import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;

import java.util.function.Consumer;
import java.util.function.IntFunction;

import static io.github.amrjlg.stream.node.Nodes.maxArraySize;

public class ReferenceArrayNode<T> implements Node<T> {

        protected final T[] array;
        protected int currentSize;

        public ReferenceArrayNode(long size, IntFunction<T[]> generator) {
            maxArraySize(size);
            this.currentSize = 0;
            this.array = generator.apply((int) size);
        }

        public ReferenceArrayNode(T[] array) {
            this.array = array;
            this.currentSize = array.length;
        }

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(array, 0, currentSize, java.util.Spliterator.ORDERED | java.util.Spliterator.IMMUTABLE);
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < currentSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            if (array.length == currentSize) {
                return array;
            }
            throw new IllegalStateException();
        }

        @Override
        public void copyInto(T[] boxed, int offset) {
            System.arraycopy(array, 0, boxed, offset, currentSize);
        }

        @Override
        public long count() {
            return currentSize;
        }
    }