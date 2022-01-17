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

import io.github.amrjlg.function.ByteConsumer;
import io.github.amrjlg.function.CharConsumer;
import io.github.amrjlg.function.FloatConsumer;
import io.github.amrjlg.function.ShortConsumer;
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

public final class ConcatNode<T> extends AbstractConcatNode<T, Node<T>>
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
            Nodes.maxArraySize(size);
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
                return Nodes.concat(getShape(), left.truncate(from, leftCount, generator), right.truncate(0, to - leftCount, generator));
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
                Nodes.maxArraySize(size);

                TypeArray array = newArray((int) size);
                copyInto(array, 0);
                return array;
            }

        }


        public static final class OfByte extends OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, Node.OfByte>
                implements Node.OfByte {
            public OfByte(Node.OfByte left, Node.OfByte right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfByte spliterator() {
                return new InternalNodeSpliterator.OfByte(this);
            }
        }

        public static final class OfShort extends OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, Node.OfShort>
                implements Node.OfShort {
            public OfShort(Node.OfShort left, Node.OfShort right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfShort spliterator() {
                return new InternalNodeSpliterator.OfShort(this);
            }
        }

        public static final class OfChar extends OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, Node.OfChar>
                implements Node.OfChar {
            public OfChar(Node.OfChar left, Node.OfChar right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfChar spliterator() {
                return new InternalNodeSpliterator.OfChar(this);
            }
        }

        public static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
                implements Node.OfInt {
            public OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }
        }

        public static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
                implements Node.OfLong {
            public OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }
        }

        public static final class OfFloat extends OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, Node.OfFloat>
                implements Node.OfFloat {
            public OfFloat(Node.OfFloat left, Node.OfFloat right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfFloat spliterator() {
                return new InternalNodeSpliterator.OfFloat(this);
            }
        }

        public static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
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