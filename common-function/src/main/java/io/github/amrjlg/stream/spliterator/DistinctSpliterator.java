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

package io.github.amrjlg.stream.spliterator;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DistinctSpliterator<T> implements Spliterator<T>, Consumer<T> {

    // The value to represent null in the ConcurrentHashMap
    private static final Object NULL_VALUE = new Object();

    // The underlying spliterator
    private final Spliterator<T> s;

    // ConcurrentHashMap holding distinct elements as keys
    private final ConcurrentHashMap<T, Boolean> seen;

    // Temporary element, only used with tryAdvance
    private T tmpSlot;

    public DistinctSpliterator(Spliterator<T> s) {
        this(s, new ConcurrentHashMap<>());
    }

    public DistinctSpliterator(Spliterator<T> s, ConcurrentHashMap<T, Boolean> seen) {
        this.s = s;
        this.seen = seen;
    }

    @Override
    public void accept(T t) {
        this.tmpSlot = t;
    }

    @SuppressWarnings("unchecked")
    private T mapNull(T t) {
        return t != null ? t : (T) NULL_VALUE;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        while (s.tryAdvance(this)) {
            if (seen.putIfAbsent(mapNull(tmpSlot), Boolean.TRUE) == null) {
                action.accept(tmpSlot);
                tmpSlot = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        s.forEachRemaining(t -> {
            if (seen.putIfAbsent(mapNull(t), Boolean.TRUE) == null) {
                action.accept(t);
            }
        });
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<T> split = s.trySplit();
        return (split != null) ? new DistinctSpliterator<>(split, seen) : null;
    }

    @Override
    public long estimateSize() {
        return s.estimateSize();
    }

    @Override
    public int characteristics() {
        return (s.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED |
                Spliterator.SORTED | Spliterator.ORDERED))
                | Spliterator.DISTINCT;
    }

    @Override
    public Comparator<? super T> getComparator() {
        return s.getComparator();
    }
}