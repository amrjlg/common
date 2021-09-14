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
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.buffer.SpinedBuffer;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.iterator.Spliterators;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
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

    public static void maxArraySize(long size){
        if (size>=MAX_ARRAY_SIZE){
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Node EMPTY_NODE = new Nodes.EmptyNode.OfRef();
    private static final Node.OfByte EMPTY_BYTE_NODE = new Nodes.EmptyNode.OfByte();
    private static final Node.OfShort EMPTY_SHORT_NODE = new Nodes.EmptyNode.OfShort();
    private static final Node.OfChar EMPTY_CHAR_NODE = new Nodes.EmptyNode.OfChar();
    private static final Node.OfInt EMPTY_INT_NODE = new Nodes.EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new Nodes.EmptyNode.OfLong();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new Nodes.EmptyNode.OfDouble();

    @SuppressWarnings("unchecked")
    public static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        Node<T> node;
        switch (shape) {
            case REFERENCE:
                node= new ConcatNode<>(left, right);
                break;
            case BYTE_VALUE:
                node= (Node<T>) new ConcatNode.OfByte((Node.OfByte) left, (Node.OfByte) right);
                break;
            case CHAR_VALUE:
                node= (Node<T>) new ConcatNode.OfChar((Node.OfChar) left, (Node.OfChar) right);
                break;
            case SHORT_VALUE:
                node= (Node<T>) new ConcatNode.OfShort((Node.OfShort) left, (Node.OfShort) right);
                break;
            case INT_VALUE:
                node= (Node<T>) new ConcatNode.OfInt((Node.OfInt) left, (Node.OfInt) right);
                break;
            case LONG_VALUE:
                node= (Node<T>) new ConcatNode.OfLong((Node.OfLong) left, (Node.OfLong) right);
                break;
            case FLOAT_VALUE:
                node= (Node<T>) new ConcatNode.OfFloat((Node.OfFloat) left, (Node.OfFloat) right);
                break;
            case DOUBLE_VALUE:
                node= (Node<T>) new ConcatNode.OfDouble((Node.OfDouble) left, (Node.OfDouble) right);
                break;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }

        return node;
    }

    static <T> NodeBuilder<T> builder() {
        return new Nodes.SpinedNodeBuilder<>();
    }

    public static <T> NodeBuilder<T> builder(long size, IntFunction<T[]> generator) {
        if (size >= 0 && size < MAX_ARRAY_SIZE) {
            return new FixedNodeBuilder(size, generator);
        } else {
            return builder();
        }
    }

    public static NodeBuilder.OfByte byteBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfShort shortBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfChar charBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfInt intBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfLong longBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfFloat floatBuilder(long size) {
        return null;
    }

    public static NodeBuilder.OfDouble doubleBuilder(long size) {
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

    private static class ArrayNode<T> implements Node<T> {

        protected final T[] array;
        protected int currentSize;

        public ArrayNode(long size, IntFunction<T[]> generator) {
            maxArraySize(size);
            this.currentSize = 0;
            this.array = generator.apply((int) size);
        }

        public ArrayNode(T[] array) {
            this.array = array;
            this.currentSize = array.length;
        }

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(array, 0, currentSize, java.util.Spliterator.ORDERED | java.util.Spliterator.IMMUTABLE);
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < currentSize; i++) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            if (array.length == currentSize) {
                return array;
            }
            throw new IllegalStateException();
        }

        @Override
        public void copyInto(T[] boxed, int offset) {
            System.arraycopy(array, 0, boxed, offset, currentSize);
        }

        @Override
        public long count() {
            return currentSize;
        }
    }

    private static abstract class AbstractConcatNode<Type, TypeNode extends Node<Type>> implements Node<Type> {
        protected final TypeNode left;
        protected final TypeNode right;
        private final long size;

        public AbstractConcatNode(TypeNode left, TypeNode right) {
            this.left = left;
            this.right = right;
            this.size = left.count() + right.count();
        }

        @Override
        public int getChildCount() {
            return 2;
        }

        @Override
        public TypeNode getChild(int i) {
            if (i == 0) return left;
            if (i == 1) return right;
            throw new IndexOutOfBoundsException();
        }

        @Override
        public long count() {
            return size;
        }
    }

    private static final class ConcatNode<T> extends AbstractConcatNode<T, Node<T>>
            implements Node<T> {
        public ConcatNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        @Override
        public Spliterator<T> spliterator() {
            return new InternalNodeSpliterator.OfRef<>(this);
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            left.forEach(consumer);
            right.forEach(consumer);
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            long size = count();
            maxArraySize(size);
            T[] apply = generator.apply((int) size);
            copyInto(apply, 0);
            return apply;
        }

        @Override
        public void copyInto(T[] boxed, int offset) {
            Objects.requireNonNull(boxed);
            left.copyInto(boxed, offset);
            right.copyInto(boxed, offset);
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }
            long leftCount = left.count();
            if (from >= leftCount) {
                return right.truncate(from - leftCount, to - leftCount, generator);
            } else if (to <= leftCount) {
                return left.truncate(from, to, generator);
            } else {
                return Nodes.conc(getShape(), left.truncate(from, leftCount, generator), right.truncate(0, to - leftCount, generator));
            }


        }


        private static abstract class OfPrimitive<Type, TypeConsumer, TypeArray,
                TypeSpliterator extends Spliterator.OfPrimitive<Type, TypeConsumer, TypeSpliterator>,
                TypeNode extends Node.OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode>>
                extends AbstractConcatNode<Type, TypeNode>
                implements Node.OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode> {

            public OfPrimitive(TypeNode left, TypeNode right) {
                super(left, right);
            }

            @Override
            public void forEach(TypeConsumer action) {
                left.forEach(action);
                right.forEach(action);
            }

            @Override
            public void copyInto(TypeArray array, int offset) {
                left.copyInto(array, offset);
                right.copyInto(array, offset);
            }

            @Override
            public TypeArray asPrimitiveArray() {
                long size = count();
                maxArraySize(size);

                TypeArray array = newArray((int) size);
                copyInto(array, 0);
                return array;
            }

        }


        private static final class OfByte extends OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, Node.OfByte>
                implements Node.OfByte {
            public OfByte(Node.OfByte left, Node.OfByte right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfByte spliterator() {
                return new InternalNodeSpliterator.OfByte(this);
            }
        }

        private static final class OfShort extends OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, Node.OfShort>
                implements Node.OfShort {
            public OfShort(Node.OfShort left, Node.OfShort right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfShort spliterator() {
                return new InternalNodeSpliterator.OfShort(this);
            }
        }

        private static final class OfChar extends OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, Node.OfChar>
                implements Node.OfChar {
            public OfChar(Node.OfChar left, Node.OfChar right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfChar spliterator() {
                return new InternalNodeSpliterator.OfChar(this);
            }
        }

        private static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Node.OfInt {
            public OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }
        }

        private static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Node.OfLong {
            public OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }
        }

        private static final class OfFloat extends OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, Node.OfFloat>
                implements Node.OfFloat {
            public OfFloat(Node.OfFloat left, Node.OfFloat right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfFloat spliterator() {
                return new InternalNodeSpliterator.OfFloat(this);
            }
        }

        private static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Node.OfDouble {
            public OfDouble(Node.OfDouble left, Node.OfDouble right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return new InternalNodeSpliterator.OfDouble(this);
            }
        }

    }

    private static abstract class InternalNodeSpliterator<T, S extends Spliterator<T>,
            N extends Node<T>> implements Spliterator<T> {
        // Node we are pointing to
        // null if full traversal has occurred
        N currentNode;

        // next child of curNode to consume
        int curChildIndex;

        // The spliterator of the curNode if that node is last and has no children.
        // This spliterator will be delegated to for splitting and traversing.
        // null if curNode has children
        S lastNodeSpliterator;

        // spliterator used while traversing with tryAdvance
        // null if no partial traversal has occurred
        S tryAdvanceSpliterator;

        // node stack used when traversing to search and find leaf nodes
        // null if no partial traversal has occurred
        Deque<N> tryAdvanceStack;

        InternalNodeSpliterator(N currentNode) {
            this.currentNode = currentNode;
        }

        @SuppressWarnings("unchecked")
        protected Deque<N> init() {
            int childCount = currentNode.getChildCount();

            Deque<N> stack = new ArrayDeque<>(8);
            for (int i = childCount - 1; i >= curChildIndex; i--) {
                stack.addFirst((N) currentNode.getChild(i));
            }
            return stack;
        }

        @SuppressWarnings("unchecked")
        protected final N findNextLeafNode(Deque<N> stack) {
            N n = null;
            while ((n = stack.pollFirst()) != null) {
                if (n.getChildCount() == 0) {
                    if (n.count() > 0) {
                        return n;
                    }
                } else {
                    for (int i = 0; i < n.getChildCount(); i++) {
                        stack.addFirst((N) n.getChild(i));
                    }
                }
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        protected final boolean initTryAdvance() {
            if (currentNode == null) {
                return false;
            }

            if (tryAdvanceSpliterator == null) {
                if (lastNodeSpliterator == null) {
                    tryAdvanceStack = init();
                    N leafNode = findNextLeafNode(tryAdvanceStack);
                    if (leafNode != null) {
                        tryAdvanceSpliterator = (S) leafNode.spliterator();
                    } else {
                        currentNode = null;
                        return false;
                    }

                } else {
                    tryAdvanceSpliterator = lastNodeSpliterator;
                }


            }

            return true;

        }

        @Override
        @SuppressWarnings("unchecked")
        public S trySplit() {
            if (currentNode == null || tryAdvanceSpliterator == null) {
                return null;
            } else if (lastNodeSpliterator != null) {
                return (S) lastNodeSpliterator.trySplit();
            } else if (curChildIndex < currentNode.getChildCount() - 1) {
                return (S) currentNode.getChild(curChildIndex++).spliterator();
            } else {
                currentNode = (N) currentNode.getChild(curChildIndex);
                if (currentNode.getChildCount() == 0) {
                    lastNodeSpliterator = (S) currentNode.spliterator();
                    return (S) lastNodeSpliterator.trySplit();
                } else {
                    curChildIndex = 0;
                    return (S) currentNode.getChild(curChildIndex++).spliterator();
                }
            }
        }

        @Override
        public final long estimateSize() {
            if (currentNode == null) {
                return 0;
            }
            if (lastNodeSpliterator != null) {
                return lastNodeSpliterator.estimateSize();
            } else {
                long size = 0;
                for (int i = curChildIndex; i < currentNode.getChildCount(); i++) {
                    size += currentNode.getChild(i).count();
                }
                return size;
            }
        }

        @Override
        public final int characteristics() {
            return Spliterator.SIZED;
        }

        private static final class OfRef<T> extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {

            public OfRef(Node<T> currentNode) {
                super(currentNode);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (!initTryAdvance()) {
                    return false;
                }
                boolean hasNext = tryAdvanceSpliterator.tryAdvance(consumer);

                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        Node<T> leaf = findNextLeafNode(tryAdvanceStack);
                        if (leaf != null) {
                            tryAdvanceSpliterator = leaf.spliterator();
                            return tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                }

                return hasNext;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                if (currentNode != null) {
                    if (tryAdvanceSpliterator == null) {
                        if (lastNodeSpliterator == null) {
                            Deque<Node<T>> nodes = init();
                            Node<T> node;
                            while ((node = findNextLeafNode(nodes)) != null) {
                                node.forEach(consumer);
                            }
                            currentNode = null;
                        } else {
                            lastNodeSpliterator.forEachRemaining(consumer);
                        }
                    } else {
                        while (tryAdvance(consumer)) {
                        }
                        ;
                    }
                }
            }
        }

        private static abstract class OfPrimitive<Type, TypeConsumer, TypeArray,
                TypeSpliterator extends Spliterator.OfPrimitive<Type, TypeConsumer, TypeSpliterator>,
                N extends Node.OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, N>>
                extends InternalNodeSpliterator<Type, TypeSpliterator, N>
                implements Spliterator.OfPrimitive<Type, TypeConsumer, TypeSpliterator> {

            public OfPrimitive(N node) {
                super(node);
            }

            @Override
            public boolean tryAdvance(TypeConsumer action) {
                if (!initTryAdvance()) {
                    return false;
                }
                boolean hasNext = tryAdvanceSpliterator.tryAdvance(action);
                if (!hasNext) {
                    if (lastNodeSpliterator == null) {
                        N node = findNextLeafNode(tryAdvanceStack);
                        if (node != null) {
                            tryAdvanceSpliterator = node.spliterator();
                            return tryAdvanceSpliterator.tryAdvance(action);
                        }
                    }
                    currentNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(TypeConsumer action) {
                if (currentNode == null)
                    return;

                if (tryAdvanceSpliterator == null) {
                    if (lastNodeSpliterator == null) {
                        Deque<N> stack = init();
                        N leaf;
                        while ((leaf = findNextLeafNode(stack)) != null) {
                            leaf.forEach(action);
                        }
                        currentNode = null;
                    } else
                        lastNodeSpliterator.forEachRemaining(action);
                } else
                    while (tryAdvance(action)) {
                    }
            }
        }

        private static final class OfByte
                extends OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, Node.OfByte>
                implements Spliterator.OfByte {
            public OfByte(Node.OfByte node) {
                super(node);
            }
        }

        private static final class OfShort
                extends OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, Node.OfShort>
                implements Spliterator.OfShort {
            public OfShort(Node.OfShort node) {
                super(node);
            }
        }

        private static final class OfChar
                extends OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, Node.OfChar>
                implements Spliterator.OfChar {
            public OfChar(Node.OfChar node) {
                super(node);
            }
        }

        private static final class OfInt
                extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Spliterator.OfInt {
            public OfInt(Node.OfInt node) {
                super(node);
            }
        }

        private static final class OfLong
                extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Spliterator.OfLong {
            public OfLong(Node.OfLong node) {
                super(node);
            }
        }

        private static final class OfFloat
                extends OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, Node.OfFloat>
                implements Spliterator.OfFloat {
            public OfFloat(Node.OfFloat node) {
                super(node);
            }
        }

        private static final class OfDouble
                extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
                implements Spliterator.OfDouble {
            public OfDouble(Node.OfDouble node) {
                super(node);
            }
        }
    }


    private static final class FixedNodeBuilder<T> extends ArrayNode<T> implements NodeBuilder<T> {

        public FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
            maxArraySize(size);
        }

        @Override
        public Node<T> build() {
            sameCount(currentSize, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != array.length)
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d",
                        size, array.length));
            currentSize = 0;
        }

        @Override
        public void accept(T t) {
            if (currentSize < array.length) {
                array[currentSize++] = t;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(currentSize, array.length);
        }

    }

    private static final class SpinedNodeBuilder<T> extends SpinedBuffer<T>
            implements Node<T>, NodeBuilder<T> {
        private boolean building;

        @Override
        public Spliterator<T> spliterator() {
            assert !building : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            assert !building : "during building";
            super.forEach(action);
        }

        @Override
        public void begin(long size) {
            assert !building : "was already building";
            building = true;
            clear();
            ensureCapacity(size);
        }

        @Override
        public void accept(T t) {
            assert building : "not building";
            super.accept(t);
        }

        @Override
        public void end() {
            assert building : "was not building";
            building = false;
        }

        @Override
        public void copyInto(T[] array, int offset) {
            assert !building : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            assert !building : "during building";
            return super.asArray(generator);
        }

        @Override
        public Node<T> build() {
            assert !building : "during building";
            return this;
        }
    }


    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private static final short[] EMPTY_SHORT_ARRAY = new short[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    private static class ByteArrayNodeBuilder implements Node.OfByte {

        final byte[] array;
        int index;

        public ByteArrayNodeBuilder(byte[] array) {
            this.array = array;
            index = array.length;
        }

        public ByteArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new byte[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfByte spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public byte[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(byte[] bytes, int offset) {
            System.arraycopy(array, 0, bytes, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(ByteConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class ByteFixedNodeBuilder extends ByteArrayNodeBuilder implements Node.Builder.OfByte {

        public ByteFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfByte build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(byte value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class ShortArrayNodeBuilder implements Node.OfShort {

        final short[] array;
        int index;

        public ShortArrayNodeBuilder(short[] array) {
            this.array = array;
            index = array.length;
        }

        public ShortArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new short[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfShort spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public short[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(short[] shorts, int offset) {
            System.arraycopy(array, 0, shorts, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(ShortConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class ShortFixedNodeBuilder extends ShortArrayNodeBuilder implements Node.Builder.OfShort {

        public ShortFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfShort build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(short value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class CharArrayNodeBuilder implements Node.OfChar {

        final char[] array;
        int index;

        public CharArrayNodeBuilder(char[] array) {
            this.array = array;
            index = array.length;
        }

        public CharArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new char[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfChar spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public char[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(char[] chars, int offset) {
            System.arraycopy(array, 0, chars, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(CharConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class CharFixedNodeBuilder extends CharArrayNodeBuilder implements Node.Builder.OfChar {

        public CharFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfChar build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(char value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class IntArrayNodeBuilder implements Node.OfInt {

        final int[] array;
        int index;

        public IntArrayNodeBuilder(int[] array) {
            this.array = array;
            index = array.length;
        }

        public IntArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new int[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfInt spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public int[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(int[] ints, int offset) {
            System.arraycopy(array, 0, ints, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(IntConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class IntFixedNodeBuilder extends IntArrayNodeBuilder implements Node.Builder.OfInt {

        public IntFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfInt build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(int value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class LongArrayNodeBuilder implements Node.OfLong {

        final long[] array;
        int index;

        public LongArrayNodeBuilder(long[] array) {
            this.array = array;
            index = array.length;
        }

        public LongArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new long[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public long[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(long[] longs, int offset) {
            System.arraycopy(array, 0, longs, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(LongConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class LongFixedNodeBuilder extends LongArrayNodeBuilder implements Node.Builder.OfLong {

        public LongFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfLong build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(long value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class FloatArrayNodeBuilder implements Node.OfFloat {

        final float[] array;
        int index;

        public FloatArrayNodeBuilder(float[] array) {
            this.array = array;
            index = array.length;
        }

        public FloatArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new float[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfFloat spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public float[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(float[] floats, int offset) {
            System.arraycopy(array, 0, floats, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(FloatConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }

    private static final class FloatFixedNodeBuilder extends FloatArrayNodeBuilder implements Node.Builder.OfFloat {

        public FloatFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfFloat build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(float value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }

    private static class DoubleArrayNodeBuilder implements Node.OfDouble {

        final double[] array;
        int index;

        public DoubleArrayNodeBuilder(double[] array) {
            this.array = array;
            index = array.length;
        }

        public DoubleArrayNodeBuilder(long size) {
            maxArraySize(size);
            this.array = new double[(int) size];
            this.index = 0;
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            return Spliterators.spliterator(array, 0, index, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }

        @Override
        public double[] asPrimitiveArray() {
            if (array.length == index) {
                return array;
            } else {
                return Arrays.copyOf(array, index);
            }
        }

        @Override
        public void copyInto(double[] doubles, int offset) {
            System.arraycopy(array, 0, doubles, offset, index);
        }

        @Override
        public long count() {
            return index;
        }

        @Override
        public void forEach(DoubleConsumer action) {
            for (int i = 0; i < index; i++) {
                action.accept(array[i]);
            }
        }

    }


    private static final class DoubleFixedNodeBuilder extends DoubleArrayNodeBuilder implements Node.Builder.OfDouble {

        public DoubleFixedNodeBuilder(long size) {
            super(size);
        }

        @Override
        public Node.OfDouble build() {
            sameCount(index, array.length);
            return this;
        }

        @Override
        public void begin(long size) {
            sameCount((int) size, array.length);
            index = 0;
        }

        @Override
        public void accept(double value) {
            if (index < array.length) {
                array[index++] = value;
            } else {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d",
                        array.length));
            }
        }

        @Override
        public void end() {
            sameCount(index, array.length);
        }
    }


    public static void sameCount(int count, int length) {
        if (count < length) {
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d",
                    count, length));
        }
    }

}
