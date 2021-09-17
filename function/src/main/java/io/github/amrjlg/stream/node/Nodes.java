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
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.pipeline.PipelineHelper;
import io.github.amrjlg.stream.task.CollectorTask;
import io.github.amrjlg.stream.task.SizedCollectorTask;
import io.github.amrjlg.stream.task.ToArrayTask;

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

    public static <T> Node.OfByte node(byte[] array) {
        return new ByteArrayNode(array);
    }

    public static <T> Node.OfChar node(char[] array) {
        return new CharArrayNode(array);
    }

    public static <T> Node.OfShort node(short[] array) {
        return new ShortArrayNode(array);
    }

    public static <T> Node.OfInt node(int[] array) {
        return new IntArrayNode(array);
    }

    public static <T> Node.OfLong node(long[] array) {
        return new LongArrayNode(array);
    }

    public static <T> Node.OfFloat node(float[] array) {
        return new FloatArrayNode(array);
    }

    public static <T> Node.OfDouble node(double[] array) {
        return new DoubleArrayNode(array);
    }

    public static <T> Node<T> node(Collection<T> collection) {
        return new CollectionNode<>(collection);
    }

    public static <T, R> Node<R> collect(PipelineHelper<R> helper, Spliterator<T> spliterator, boolean flatten, IntFunction<R[]> generator) {

        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            R[] array = generator.apply((int) size);
            new SizedCollectorTask.OfRef<>(spliterator, helper, array).invoke();

            return node(array);
        }

        Node<R> node = new CollectorTask.OfRef<>(helper, generator, spliterator).invoke();
        if (flatten) {
            return flatten(node, generator);
        }
        return node;
    }

    public static <Input> Node.OfByte collectByte(PipelineHelper<Byte> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            byte[] array = new byte[(int) size];
            new SizedCollectorTask.OfByte<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfByte node = new CollectorTask.OfByte<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenByte(node);
        }
        return node;
    }

    public static <Input> Node.OfShort collectShort(PipelineHelper<Short> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            short[] array = new short[(int) size];
            new SizedCollectorTask.OfShort<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfShort node = new CollectorTask.OfShort<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenShort(node);
        }
        return node;
    }

    public static <Input> Node.OfChar collectChar(PipelineHelper<Character> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            char[] array = new char[(int) size];
            new SizedCollectorTask.OfChar<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfChar node = new CollectorTask.OfChar<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenChar(node);
        }
        return node;
    }

    public static <Input> Node.OfInt collectInt(PipelineHelper<Integer> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            int[] array = new int[(int) size];
            new SizedCollectorTask.OfInt<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfInt node = new CollectorTask.OfInt<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenInt(node);
        }
        return node;
    }

    public static <Input> Node.OfLong collectLong(PipelineHelper<Long> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            long[] array = new long[(int) size];
            new SizedCollectorTask.OfLong<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfLong node = new CollectorTask.OfLong<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenLong(node);
        }
        return node;
    }

    public static <Input> Node.OfFloat collectFloat(PipelineHelper<Float> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            float[] array = new float[(int) size];
            new SizedCollectorTask.OfFloat<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfFloat node = new CollectorTask.OfFloat<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenFloat(node);
        }
        return node;
    }

    public static <Input> Node.OfDouble collectDouble(PipelineHelper<Double> helper, Spliterator<Input> spliterator, boolean flatten) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0 && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
            maxArraySize(size);
            double[] array = new double[(int) size];
            new SizedCollectorTask.OfDouble<>(spliterator, helper, array).invoke();
            return node(array);
        }
        Node.OfDouble node = new CollectorTask.OfDouble<>(helper, spliterator).invoke();
        if (flatten) {
            return flattenDouble(node);
        }
        return node;
    }


    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() > 0) {
            long count = node.count();
            maxArraySize(count);
            T[] array = generator.apply((int) count);
            new ToArrayTask.OfRef<>(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfByte flattenByte(Node.OfByte node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            byte[] array = new byte[(int) size];
            new ToArrayTask.OfByte(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfChar flattenChar(Node.OfChar node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            char[] array = new char[(int) size];
            new ToArrayTask.OfChar(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfShort flattenShort(Node.OfShort node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            short[] array = new short[(int) size];
            new ToArrayTask.OfShort(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfInt flattenInt(Node.OfInt node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            int[] array = new int[(int) size];
            new ToArrayTask.OfInt(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfLong flattenLong(Node.OfLong node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            long[] array = new long[(int) size];
            new ToArrayTask.OfLong(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfFloat flattenFloat(Node.OfFloat node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            float[] array = new float[(int) size];
            new ToArrayTask.OfFloat(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
    }

    public static Node.OfDouble flattenDouble(Node.OfDouble node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= MAX_ARRAY_SIZE)
                throw new IllegalArgumentException(BAD_SIZE);
            double[] array = new double[(int) size];
            new ToArrayTask.OfDouble(node, array, 0).invoke();
            return node(array);
        } else {
            return node;
        }
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
