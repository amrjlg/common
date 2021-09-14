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

import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * @author amrjlg
 **/
public class CollectionNode<T> implements Node<T> {
    private final Collection<T> collection;

    public CollectionNode(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public Spliterator<T> spliterator() {
        return new Spliterators.IteratorSpliterator<T>(collection, 0);
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
        collection.forEach(consumer);
    }

    @Override
    public T[] asArray(IntFunction<T[]> generator) {
        return collection.toArray(generator.apply(collection.size()));
    }

    @Override
    public void copyInto(T[] array, int offset) {
        for (T ele : collection) {
            array[offset++] = ele;
        }
    }

    @Override
    public long count() {
        return collection.size();
    }
}
