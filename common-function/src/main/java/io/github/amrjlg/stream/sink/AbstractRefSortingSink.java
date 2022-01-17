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

import java.util.Comparator;

/**
 * @author amrjlg
 **/
public abstract class AbstractRefSortingSink<T> extends Sink.ChainedReference<T,T> {

    protected final Comparator<? super T> comparator;
    protected boolean cancellationRequested;


    public AbstractRefSortingSink(Sink<? super T> downstream,Comparator<? super T> comparator) {
        super(downstream);
        this.comparator=comparator;
    }

    @Override
    public boolean cancellationRequested() {
        cancellationRequested = true;
        return false;
    }
}
