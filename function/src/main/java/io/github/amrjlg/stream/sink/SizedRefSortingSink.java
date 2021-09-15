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

package io.github.amrjlg.stream.sink;

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.node.Nodes;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author amrjlg
 **/
public class SizedRefSortingSink<T> extends AbstractRefSortingSink<T> {

    private T[] array;
    private int offset;


    public SizedRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
        super(downstream, comparator);
    }

    @Override
    public void begin(long size) {
        Nodes.maxArraySize(size);
        array = (T[]) new Object[(int) size];
        offset = 0;
    }

    @Override
    public void end() {
        Arrays.sort(array, 0, offset, comparator);
        downstream.begin(offset);
        if (!cancellationRequested) {
            for (int i = 0; i < offset; i++) {
                downstream.accept(array[i]);
            }
        } else {
            for (int i = 0; i < offset && !downstream.cancellationRequested(); i++) {
                downstream.accept(array[i]);
            }
        }
        downstream.end();
        array = null;
    }

    @Override
    public void accept(T t) {
        array[offset++] = t;
    }
}
