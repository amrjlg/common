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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public abstract class InternalNodeSpliterator<T, S extends Spliterator<T>,
        N extends Node<T>> implements Spliterator<T> {
    // Node we are pointing to
    // null if full traversal has occurred
    protected N currentNode;

    // next child of curNode to consume
    protected int curChildIndex;

    // The spliterator of the curNode if that node is last and has no children.
    // This spliterator will be delegated to for splitting and traversing.
    // null if curNode has children
    protected S lastNodeSpliterator;

    // spliterator used while traversing with tryAdvance
    // null if no partial traversal has occurred
    protected S tryAdvanceSpliterator;

    // node stack used when traversing to search and find leaf nodes
    // null if no partial traversal has occurred
    protected Deque<N> tryAdvanceStack;

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
        N n;
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

    public static final class OfRef<T> extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {

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

    public static abstract class OfPrimitive<Type, TypeConsumer, TypeArray,
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

    public static final class OfByte
            extends OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, Node.OfByte>
            implements Spliterator.OfByte {
        public OfByte(Node.OfByte node) {
            super(node);
        }
    }

    public static final class OfShort
            extends OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, Node.OfShort>
            implements Spliterator.OfShort {
        public OfShort(Node.OfShort node) {
            super(node);
        }
    }

    public static final class OfChar
            extends OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, Node.OfChar>
            implements Spliterator.OfChar {
        public OfChar(Node.OfChar node) {
            super(node);
        }
    }

    public static final class OfInt
            extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
            implements Spliterator.OfInt {
        public OfInt(Node.OfInt node) {
            super(node);
        }
    }

    public static final class OfLong
            extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
            implements Spliterator.OfLong {
        public OfLong(Node.OfLong node) {
            super(node);
        }
    }

    public static final class OfFloat
            extends OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, Node.OfFloat>
            implements Spliterator.OfFloat {
        public OfFloat(Node.OfFloat node) {
            super(node);
        }
    }

    public static final class OfDouble
            extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
            implements Spliterator.OfDouble {
        public OfDouble(Node.OfDouble node) {
            super(node);
        }
    }
}