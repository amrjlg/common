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

import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;

import java.util.Arrays;

public class ShortArrayNode implements Node.OfShort {

        final short[] array;
        int index;

        public ShortArrayNode(short[] array) {
            this.array = array;
            index = array.length;
        }

        public ShortArrayNode(long size) {
            Nodes.maxArraySize(size);
            this.array = new short[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfShort spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public short[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(short[] shorts, int offset) {
            System.arraycopy(array, 0, shorts, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(ShortConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }