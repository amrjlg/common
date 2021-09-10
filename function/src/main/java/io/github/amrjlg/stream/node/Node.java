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
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.iterator.Spliterator;
import io.github.amrjlg.stream.StreamShape;

import java.util.function.Consumer;
import java.util.function.IntFunction;


/**
 * @author amrjlg
 **/
public interface Node<T> {
    Spliterator<T> spliterator();

    void forEach(Consumer<? super T> consumer);

    default int getChildCount() {
        return 0;
    }

    default Node<T> getChild(int i) {
        throw new IndexOutOfBoundsException();
    }

    default Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
        if (from == 0 && to == count())
            return this;
        Spliterator<T> spliterator = spliterator();
        long size = to - from;
        Node.Builder<T> builder = Nodes.builder(size, generator);
        builder.begin(size);
        for (int i = 0; i < from && spliterator.tryAdvance(e -> {
        }); i++) {
        }
        for (int i = 0; (i < size) && spliterator.tryAdvance(builder); i++) {
        }
        builder.end();
        return builder.build();
    }

    T[] asArray(IntFunction<T[]> generator);

    void copyInto(T[] boxed, int offset);

    default StreamShape getShape() {
        return StreamShape.REFERENCE;
    }


    long count();

    interface Builder<T> extends Sink<T> {

        Node<T> build();

        interface OfByte extends Builder<Byte>, SKinOfByte {
            @Override
            Node.OfByte build();
        }

        interface OfChar extends Builder<Character>, SKinOfChar {
            @Override
            Node<Character> build();
        }

        interface OfShort extends Builder<Short>, SKinOfShort {
            @Override
            Node<Short> build();
        }

        interface OfInt extends Builder<Integer>, SKinOfInt {
            @Override
            Node<Integer> build();
        }

        interface OfLong extends Builder<Long>, SKinOfLong {
            @Override
            Node<Long> build();
        }

        interface OfFloat extends Builder<Float>, SKinOfFloat {
            @Override
            Node<Float> build();
        }

        interface OfDouble extends Builder<Double>, SKinOfDouble {
            @Override
            Node<Double> build();
        }

    }

    interface OfPrimitive<Type, TypeConsumer, TypeArray,
            TypeSpliterator extends Spliterator.OfPrimitive<Type, TypeConsumer, TypeSpliterator>,
            TypeNode extends OfPrimitive<Type, TypeConsumer, TypeArray, TypeSpliterator, TypeNode>>
            extends Node<Type> {

        @Override
        TypeSpliterator spliterator();

        void forEach(TypeConsumer action);

        @Override
        default TypeNode getChild(int i) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        TypeNode truncate(long from, long to, IntFunction<Type[]> generator);

        @Override
       default Type[] asArray(IntFunction<Type[]> generator){
            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE){
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            Type[] boxed = generator.apply((int) count());
            copyInto(boxed,0);
            return boxed;
        }

        TypeArray asPrimitiveArray();

        TypeArray newArray(int count);

        void copyInto(TypeArray array, int offset);
    }

    interface OfByte extends OfPrimitive<Byte, ByteConsumer, byte[], Spliterator.OfByte, OfByte> {
        @Override
        default void forEach(Consumer<? super Byte> consumer) {
            if (consumer instanceof ByteConsumer) {
                forEach((ByteConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Byte[] boxed, int offset) {
            byte[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfByte truncate(long from, long to, IntFunction<Byte[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfByte spliterator = spliterator();
            Node.Builder.OfByte builder = Nodes.byteBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((ByteConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((ByteConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default byte[] newArray(int count) {
            return new byte[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.BYTE_VALUE;
        }
    }

}
