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

package io.github.amrjlg.stream.sink;

import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.node.Nodes;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author amrjlg
 **/
public class RefSortingSink<T> extends AbstractRefSortingSink<T> {

    private ArrayList<T> list;

    public RefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
        super(downstream, comparator);
    }

    @Override
    public void begin(long size) {
        Nodes.maxArraySize(size);
        list = (size >= 0) ? new ArrayList<>((int) size) : new ArrayList<>();
    }

    @Override
    public void end() {
        list.sort(comparator);
        downstream.begin(list.size());
        if (!cancellationRequested) {
            list.forEach(downstream);
        } else {
            for (T t : list) {
                if (downstream.cancellationRequested()) {
                    break;
                }
                downstream.accept(t);
            }
        }
        downstream.end();
        list = null;
    }

    @Override
    public void accept(T t) {
        list.add(t);
    }
}
