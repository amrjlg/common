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

import io.github.amrjlg.stream.StreamShape;

import java.util.Collection;
import java.util.Objects;
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

    public static void maxArraySize(long size) {
        if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Node EMPTY_NODE = new EmptyNode.OfRef();
    private static final Node.OfByte EMPTY_BYTE_NODE = new EmptyNode.OfByte();
    private static final Node.OfShort EMPTY_SHORT_NODE = new EmptyNode.OfShort();
    private static final Node.OfChar EMPTY_CHAR_NODE = new EmptyNode.OfChar();
    private static final Node.OfInt EMPTY_INT_NODE = new EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new EmptyNode.OfLong();
    private static final Node.OfFloat EMPTY_FLOAT_NODE = new EmptyNode.OfFloat();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new EmptyNode.OfDouble();


    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private static final short[] EMPTY_SHORT_ARRAY = new short[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    @SuppressWarnings("unchecked")
    public static <T> Node<T> empty(StreamShape shape) {
        Objects.requireNonNull(shape);
        Node<T> node;
        switch (shape) {
            case REFERENCE:
                node = EMPTY_NODE;
                break;
            case BYTE_VALUE:
                node = (Node<T>) EMPTY_BYTE_NODE;
                break;
            case CHAR_VALUE:
                node = (Node<T>) EMPTY_CHAR_NODE;
                break;
            case SHORT_VALUE:
                node = (Node<T>) EMPTY_SHORT_NODE;
                break;
            case INT_VALUE:
                node = (Node<T>) EMPTY_INT_NODE;
                break;
            case LONG_VALUE:
                node = (Node<T>) EMPTY_LONG_NODE;
                break;
            case FLOAT_VALUE:
                node = (Node<T>) EMPTY_FLOAT_NODE;
                break;
            case DOUBLE_VALUE:
                node = (Node<T>) EMPTY_DOUBLE_NODE;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return node;
    }

    public static <T> Node<T> node(T[] array) {
        return new ReferenceArrayNode<>(array);
    }

    public static <T> Node<T> node(Collection<T> collection) {
        return new CollectionNode<>(collection);
    }


    @SuppressWarnings("unchecked")
    public static <T> Node<T> concat(StreamShape shape, Node<T> left, Node<T> right) {
        Node<T> node;
        switch (shape) {
            case REFERENCE:
                node = new ConcatNode<>(left, right);
                break;
            case BYTE_VALUE:
                node = (Node<T>) new ConcatNode.OfByte((Node.OfByte) left, (Node.OfByte) right);
                break;
            case CHAR_VALUE:
                node = (Node<T>) new ConcatNode.OfChar((Node.OfChar) left, (Node.OfChar) right);
                break;
            case SHORT_VALUE:
                node = (Node<T>) new ConcatNode.OfShort((Node.OfShort) left, (Node.OfShort) right);
                break;
            case INT_VALUE:
                node = (Node<T>) new ConcatNode.OfInt((Node.OfInt) left, (Node.OfInt) right);
                break;
            case LONG_VALUE:
                node = (Node<T>) new ConcatNode.OfLong((Node.OfLong) left, (Node.OfLong) right);
                break;
            case FLOAT_VALUE:
                node = (Node<T>) new ConcatNode.OfFloat((Node.OfFloat) left, (Node.OfFloat) right);
                break;
            case DOUBLE_VALUE:
                node = (Node<T>) new ConcatNode.OfDouble((Node.OfDouble) left, (Node.OfDouble) right);
                break;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }

        return node;
    }

    static <T> NodeBuilder<T> builder() {
        return new SpinedNodeBuilder<>();
    }

    public static <T> NodeBuilder<T> builder(long size, IntFunction<T[]> generator) {
        if (size >= 0 && size < MAX_ARRAY_SIZE) {
            return new ReferenceFixedNodeBuilder<>(size, generator);
        } else {
            return builder();
        }
    }

    private static boolean arraySize(long size) {
        return size >= 0 && size < MAX_ARRAY_SIZE;
    }

    public static NodeBuilder.OfByte byteBuilder(long size) {
        if (arraySize(size)) {
            return new ByteFixedNodeBuilder(size);
        }
        return byteBuilder();
    }

    private static NodeBuilder.OfByte byteBuilder() {
        return new ByteSpinedNodeBuilder();
    }

    public static NodeBuilder.OfShort shortBuilder(long size) {
        if (arraySize(size)) {
            return new ShortFixedNodeBuilder(size);
        }
        return shortBuilder();
    }

    private static NodeBuilder.OfShort shortBuilder() {
        return new ShortSpinedNodeBuilder();
    }

    public static NodeBuilder.OfChar charBuilder(long size) {
        if (arraySize(size)) {
            return new CharFixedNodeBuilder(size);
        }
        return charBuilder();
    }

    private static NodeBuilder.OfChar charBuilder() {
        return new CharSpinedNodeBuilder();
    }

    public static NodeBuilder.OfInt intBuilder(long size) {
        if (arraySize(size)) {
            return new IntFixedNodeBuilder(size);
        } else {
            return intBuilder();
        }
    }

    private static NodeBuilder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    public static NodeBuilder.OfLong longBuilder(long size) {
        if (arraySize(size)) {
            return new LongFixedNodeBuilder(size);
        }
        return longBuilder();
    }

    private static NodeBuilder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    public static NodeBuilder.OfFloat floatBuilder(long size) {
        if (arraySize(size)) {
            return new FloatFixedNodeBuilder(size);
        }
        return floatBuilder();
    }

    private static NodeBuilder.OfFloat floatBuilder() {
        return new FloatSpinedNodeBuilder();
    }

    public static NodeBuilder.OfDouble doubleBuilder(long size) {
        if (arraySize(size)) {
            return new DoubleFixedNodeBuilder(size);
        }
        return doubleBuilder();
    }

    private static NodeBuilder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    public static void sameCount(int count, int length) {
        if (count < length) {
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                    count, length));
        }
    }


}
