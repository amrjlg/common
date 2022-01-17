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

import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;

import java.util.Arrays;

public class CharArrayNode implements Node.OfChar {

        final char[] array;
        int index;

        public CharArrayNode(char[] array) {
            this.array = array;
            index = array.length;
        }

        public CharArrayNode(long size) {
           Nodes.maxArraySize(size);
            this.array = new char[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfChar spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public char[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(char[] chars, int offset) {
            System.arraycopy(array, 0, chars, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(CharConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }
