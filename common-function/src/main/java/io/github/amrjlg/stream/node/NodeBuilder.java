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

import io.github.amrjlg.stream.Sink;

public interface NodeBuilder<T> extends Sink<T> {

    Node<T> build();

    interface OfByte extends NodeBuilder<Byte>, Sink.OfByte {
        @Override
        Node.OfByte build();
    }

    interface OfChar extends NodeBuilder<Character>, Sink.OfChar {
        @Override
        Node.OfChar build();
    }

    interface OfShort extends NodeBuilder<Short>, Sink.OfShort {
        @Override
        Node.OfShort build();
    }

    interface OfInt extends NodeBuilder<Integer>, Sink.OfInt {
        @Override
        Node.OfInt build();
    }

    interface OfLong extends NodeBuilder<Long>, Sink.OfLong {
        @Override
        Node.OfLong build();
    }

    interface OfFloat extends NodeBuilder<Float>, Sink.OfFloat {
        @Override
        Node.OfFloat build();
    }

    interface OfDouble extends NodeBuilder<Double>, Sink.OfDouble {
        @Override
        Node.OfDouble build();
    }

}