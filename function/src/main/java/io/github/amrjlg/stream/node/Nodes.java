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
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

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
    private static final Node.OfShort EMPTY_SHORT_NODE = new Nodes.EmptyNode.OfShort();
    private static final Node.OfChar EMPTY_CHAR_NODE = new Nodes.EmptyNode.OfChar();
    private static final Node.OfInt EMPTY_INT_NODE = new Nodes.EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new Nodes.EmptyNode.OfLong();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new Nodes.EmptyNode.OfDouble();

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

        private static final class OfShort extends EmptyNode<Short, short[], ShortConsumer>
                implements Node.OfShort {

            @Override
            public Spliterator.OfShort spliterator() {
                return null;
            }

            @Override
            public short[] asPrimitiveArray() {
                return new short[0];
            }

        }

        private static final class OfChar extends EmptyNode<Character, char[], CharConsumer>
                implements Node.OfChar {

            @Override
            public Spliterator.OfChar spliterator() {
                return null;
            }

            @Override
            public char[] asPrimitiveArray() {
                return new char[0];
            }

        }

        private static final class OfInt extends EmptyNode<Integer, int[], IntConsumer>
                implements Node.OfInt {

            @Override
            public Spliterator.OfInt spliterator() {
                return null;
            }

            @Override
            public int[] asPrimitiveArray() {
                return new int[0];
            }

        }

        private static final class OfLong extends EmptyNode<Long, long[], LongConsumer>
                implements Node.OfLong {

            @Override
            public Spliterator.OfLong spliterator() {
                return null;
            }

            @Override
            public long[] asPrimitiveArray() {
                return new long[0];
            }

        }
        private static final class OfFloat extends EmptyNode<Float, float[], FloatConsumer>
                implements Node.OfFloat {

            @Override
            public Spliterator.OfFloat spliterator() {
                return null;
            }

            @Override
            public float[] asPrimitiveArray() {
                return new float[0];
            }

        }
        private static final class OfDouble extends EmptyNode<Double, double[], DoubleConsumer>
                implements Node.OfDouble {

            @Override
            public Spliterator.OfDouble spliterator() {
                return null;
            }

            @Override
            public double[] asPrimitiveArray() {
                return new double[0];
            }

        }

    }
}
