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

package io.github.amrjlg.stream.task;

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.node.Node;

import java.util.concurrent.CountedCompleter;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * @author amrjlg
 **/
public abstract class ToArrayTask<
        Type,
        TypeNode extends Node<Type>,
        K extends ToArrayTask<Type, TypeNode, K>>
        extends CountedCompleter<Void> {
    protected final TypeNode node;
    protected final int offset;

    public ToArrayTask(TypeNode node, int offset) {
        this.node = node;
        this.offset = offset;
    }

    public ToArrayTask(K parent, TypeNode node, int offset) {
        super(parent);
        this.node = node;
        this.offset = offset;
    }

    abstract void copyNodeToArray();

    abstract K makeChild(int childIndex, int offset);

    @Override
    public void compute() {
        ToArrayTask<Type, TypeNode, K> task = this;

        while (true) {
            if (task.node.getChildCount() == 0) {
                task.copyNodeToArray();
                task.propagateCompletion();
                break;
            } else {
                task.setPendingCount(task.node.getChildCount() - 1);
                int size = 0, i = 0;
                for (; i < task.node.getChildCount() - 1; i++) {
                    K child = task.makeChild(i, task.offset + size);
                    size += child.node.count();
                    child.fork();
                }
                task = task.makeChild(i, task.offset + size);
            }
        }
    }

    public static final class OfRef<T> extends ToArrayTask<T, Node<T>, OfRef<T>> {

        private final T[] array;

        public OfRef(Node<T> node, T[] array, int offset) {
            super(node, offset);
            this.array = array;
        }

        public OfRef(OfRef<T> parent, Node<T> node, int offset) {
            super(parent, node, offset);
            this.array = parent.array;
        }

        @Override
        void copyNodeToArray() {
            node.copyInto(array, offset);
        }

        @Override
        OfRef<T> makeChild(int childIndex, int offset) {
            return new OfRef<>(this, node.getChild(childIndex), offset);
        }
    }


    public static class OfPrimitive<Type, TypeConsumer, TypeArray,
            TypeSpliterator extends Spliterator.OfPrimitive<Type, TypeConsumer, TypeSpliterator>,
            TypeNode extends Node.OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode>>
            extends ToArrayTask<Type, TypeNode, OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode>> {

        private final TypeArray array;

        public OfPrimitive(TypeNode node, TypeArray array, int offset) {
            super(node, offset);
            this.array = array;
        }

        public OfPrimitive(OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode> parent, TypeNode node, int offset) {
            super(parent, node, offset);
            this.array = parent.array;
        }

        @Override
        void copyNodeToArray() {
            node.copyInto(array,offset);
        }

        @Override
        OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode> makeChild(int childIndex, int offset) {
            return new OfPrimitive<>(this, node.getChild(childIndex), offset);
        }
    }

    public static final class OfByte extends OfPrimitive<Byte, ByteConsumer,byte[],Spliterator.OfByte,Node.OfByte>{

        public OfByte(Node.OfByte node, byte[] array, int offset) {
            super(node, array, offset);
        }

        public OfByte(OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, Node.OfByte> parent, Node.OfByte node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfChar extends OfPrimitive<Character, CharConsumer,char[],Spliterator.OfChar,Node.OfChar>{

        public OfChar(Node.OfChar node, char[] array, int offset) {
            super(node, array, offset);
        }

        public OfChar(OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, Node.OfChar> parent, Node.OfChar node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfShort extends OfPrimitive<Short, ShortConsumer,short[],Spliterator.OfShort,Node.OfShort>{

        public OfShort(Node.OfShort node, short[] array, int offset) {
            super(node, array, offset);
        }

        public OfShort(OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, Node.OfShort> parent, Node.OfShort node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfInt extends OfPrimitive<Integer, IntConsumer,int[],Spliterator.OfInt,Node.OfInt>{

        public OfInt(Node.OfInt node, int[] array, int offset) {
            super(node, array, offset);
        }

        public OfInt(OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> parent, Node.OfInt node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfLong extends OfPrimitive<Long, LongConsumer,long[],Spliterator.OfLong,Node.OfLong>{

        public OfLong(Node.OfLong node, long[] array, int offset) {
            super(node, array, offset);
        }

        public OfLong(OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> parent, Node.OfLong node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfFloat extends OfPrimitive<Float, FloatConsumer,float[],Spliterator.OfFloat,Node.OfFloat>{

        public OfFloat(Node.OfFloat node, float[] array, int offset) {
            super(node, array, offset);
        }

        public OfFloat(OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, Node.OfFloat> parent, Node.OfFloat node, int offset) {
            super(parent, node, offset);
        }
    }

    public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer,double[],Spliterator.OfDouble,Node.OfDouble>{

        public OfDouble(Node.OfDouble node, double[] array, int offset) {
            super(node, array, offset);
        }

        public OfDouble(OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> parent, Node.OfDouble node, int offset) {
            super(parent, node, offset);
        }
    }

}
