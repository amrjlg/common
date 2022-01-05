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
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;


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
        NodeBuilder<T> builder = Nodes.builder(size, generator);
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
        default Type[] asArray(IntFunction<Type[]> generator) {
            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            Type[] boxed = generator.apply((int) count());
            copyInto(boxed, 0);
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
            NodeBuilder.OfByte builder = Nodes.byteBuilder(size);
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


    interface OfShort extends OfPrimitive<Short, ShortConsumer, short[], Spliterator.OfShort, OfShort> {
        @Override
        default void forEach(Consumer<? super Short> consumer) {
            if (consumer instanceof ShortConsumer) {
                forEach((ShortConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Short[] boxed, int offset) {
            short[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfShort truncate(long from, long to, IntFunction<Short[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfShort spliterator = spliterator();
            NodeBuilder.OfShort builder = Nodes.shortBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((ShortConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((ShortConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default short[] newArray(int count) {
            return new short[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.SHORT_VALUE;
        }
    }


    interface OfChar extends OfPrimitive<Character, CharConsumer, char[], Spliterator.OfChar, OfChar> {
        @Override
        default void forEach(Consumer<? super Character> consumer) {
            if (consumer instanceof CharConsumer) {
                forEach((CharConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Character[] boxed, int offset) {
            char[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfChar truncate(long from, long to, IntFunction<Character[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfChar spliterator = spliterator();
            NodeBuilder.OfChar builder = Nodes.charBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((CharConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((CharConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default char[] newArray(int count) {
            return new char[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.CHAR_VALUE;
        }
    }

    interface OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, OfInt> {
        @Override
        default void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Integer[] boxed, int offset) {
            int[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfInt truncate(long from, long to, IntFunction<Integer[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfInt spliterator = spliterator();
            NodeBuilder.OfInt builder = Nodes.intBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((IntConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((IntConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default int[] newArray(int count) {
            return new int[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }
    }

    interface OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, OfLong> {
        @Override
        default void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Long[] boxed, int offset) {
            long[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfLong truncate(long from, long to, IntFunction<Long[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfLong spliterator = spliterator();
            NodeBuilder.OfLong builder = Nodes.longBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((LongConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((LongConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default long[] newArray(int count) {
            return new long[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    interface OfFloat extends OfPrimitive<Float, FloatConsumer, float[], Spliterator.OfFloat, OfFloat> {
        @Override
        default void forEach(Consumer<? super Float> consumer) {
            if (consumer instanceof FloatConsumer) {
                forEach((FloatConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Float[] boxed, int offset) {
            float[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfFloat truncate(long from, long to, IntFunction<Float[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfFloat spliterator = spliterator();
            NodeBuilder.OfFloat builder = Nodes.floatBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((FloatConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((FloatConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default float[] newArray(int count) {
            return new float[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.FLOAT_VALUE;
        }
    }

    interface OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, OfDouble> {
        @Override
        default void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
            } else {
                spliterator().forEachRemaining(consumer);
            }
        }

        @Override
        default void copyInto(Double[] boxed, int offset) {
            double[] array = asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default OfDouble truncate(long from, long to, IntFunction<Double[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }

            long size = to - from;

            Spliterator.OfDouble spliterator = spliterator();
            NodeBuilder.OfDouble builder = Nodes.doubleBuilder(size);
            builder.begin(size);
            for (int i = 0; i < from && spliterator.tryAdvance((DoubleConsumer) e -> {
            }); i++) {
            }
            for (int i = 0; i < size && spliterator.tryAdvance((DoubleConsumer) builder); i++) {
            }
            builder.end();
            return builder.build();
        }

        @Override
        default double[] newArray(int count) {
            return new double[count];
        }

        @Override
        default StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }


}
