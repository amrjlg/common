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
import io.github.amrjlg.stream.buffer.SpinedBuffer;
import io.github.amrjlg.stream.node.Nodes;

import java.util.Arrays;

/**
 * @author amrjlg
 **/
public class ShortSortingSink extends AbstractShortSortingSink {

    private SpinedBuffer.OfShort buffer;

    public ShortSortingSink(Sink<? super Short> downstream) {
        super(downstream);
    }

    @Override
    public void begin(long size) {
        Nodes.maxArraySize(size);
        buffer = (size >= 0) ? new SpinedBuffer.OfShort((int) size) : new SpinedBuffer.OfShort();
    }

    @Override
    public void end() {
        short[] array = buffer.asPrimitiveArray();
        Arrays.sort(array);
        downstream.begin(array.length);
        if (!cancellationRequested){
            for (short value : array) {
                downstream.accept(value);
            }
        }else {
            for (short value : array) {
                if (downstream.cancellationRequested()){
                    break;
                }
                downstream.accept(value);
            }
        }
        downstream.end();
        buffer = null;
    }

    @Override
    public void accept(short value) {
        buffer.accept(value);
    }
}
