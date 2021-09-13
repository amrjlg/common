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

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * @author amrjlg
 **/
public class Nodes {
    private Nodes() {
        throw new Error("no instances");
    }

    /**
     * The maximum size of an array that can be allocated.
     */
    public static final long MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    // IllegalArgumentException messages
    public static final String BAD_SIZE = "Stream size exceeds max array size";

    @SuppressWarnings("rawtypes")
    private static final Node EMPTY_NODE = new Nodes.EmptyNode.OfRef();
    private static final Node.OfByte EMPTY_BYTE_NODE = new Nodes.EmptyNode.OfByte();
//    private static final Node.OfInt EMPTY_INT_NODE = new Nodes.EmptyNode.OfInt();
//    private static final Node.OfInt EMPTY_INT_NODE = new Nodes.EmptyNode.OfInt();
//    private static final Node.OfInt EMPTY_INT_NODE = new Nodes.EmptyNode.OfInt();
//    private static final Node.OfLong EMPTY_LONG_NODE = new Nodes.EmptyNode.OfLong();
//    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new Nodes.EmptyNode.OfDouble();

    public static <T> Node.Builder<T> builder(long size, IntFunction<T[]> generator) {
        return null;
    }

    public static Node.Builder.OfByte byteBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfShort shortBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfChar charBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfInt intBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfLong longBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfFloat floatBuilder(long size) {
        return null;
    }

    public static Node.Builder.OfDouble doubleBuilder(long size) {
        return null;
    }

    private static abstract class EmptyNode<Type, TypeArray, TypeConsumer> implements Node<Type> {

        public EmptyNode() {
        }

        public void forEach(TypeConsumer consumer) {

        }

        @Override
        public Type[] asArray(IntFunction<Type[]> generator) {
            return generator.apply(0);
        }


        public void copyInto(TypeArray array, int offset) {

        }

        @Override
        public long count() {
            return 0;
        }

        private static final class OfRef<T> extends EmptyNode<T, T[], Consumer<? super T>> {

            @Override
            public Spliterator<T> spliterator() {
                return Spliterators.emptySpliterator();
            }
        }

        private static final class OfByte extends EmptyNode<Byte, byte[], ByteConsumer>
                implements Node.OfByte {

            @Override
            public Spliterator.OfByte spliterator() {
                return null;
            }

            @Override
            public byte[] asPrimitiveArray() {
                return new byte[0];
            }

        }
    }
}
