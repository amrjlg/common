
/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.Streams;
import io.github.amrjlg.util.OptionalByte;
import io.github.amrjlg.util.OptionalChar;
import io.github.amrjlg.util.OptionalFloat;
import io.github.amrjlg.util.OptionalShort;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Random;
import java.util.function.Function;

class ReferencePipelineTest {

    private String [] arrays(int number){
        Random random = new Random();
        number = Math.max(number, 0);
        String[] arr = new String[number];
        for (int i = 0; i < number; i++) {
            arr[i] = String.valueOf(random.nextInt(100));
        }
        return arr;
    }

    @Test
    void mapToByte() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToByte");
        Stream.of(arr)
                .mapToByte(s -> (byte) Integer.parseInt(s))
                .forEach(System.out::println);
        OptionalByte max = Stream.of(arr)
                .mapToByte(s -> (byte) Integer.parseInt(s))
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToShort() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToShort");
        Stream.of(arr)
                .mapToShort(Short::parseShort)
                .forEach(System.out::println);
        OptionalShort max = Stream.of(arr)
                .mapToShort(Short::parseShort)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToChar() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToChar");
        Stream.of(arr)
                .mapToChar(s -> s.charAt(0))
                .forEach(System.out::println);
        OptionalChar max = Stream.of(arr)
                .mapToChar(s -> s.charAt(0))
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToInt() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToInt");
        Stream.of(arr)
                .mapToInt(Integer::parseInt)
                .forEach(System.out::println);
        OptionalInt max = Stream.of(arr)
                .mapToInt(Integer::parseInt)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToLong() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToLong");
        Stream.of(arr)
                .mapToLong(Long::parseLong)
                .forEach(System.out::println);
        OptionalLong max = Stream.of(arr)
                .mapToLong(Long::parseLong)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToFloat() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToFloat");
        Stream.of(arr)
                .mapToFloat(Float::parseFloat)
                .forEach(System.out::println);
        OptionalFloat max = Stream.of(arr)
                .mapToFloat(Float::parseFloat)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void mapToDouble() {
        String[] arr = arrays(5);
        System.out.println("Stream.mapToDouble");
        Stream.of(arr)
                .mapToDouble(Double::parseDouble)
                .forEach(System.out::println);
        OptionalDouble max = Stream.of(arr)
                .mapToDouble(Double::parseDouble)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMap() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMap");
        Optional<String> max = Stream.of("3", "2", "1", "4")
                .map(Function.identity())
                .max(String::compareTo);
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToByte() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToByte");
        Stream.of(arr)
                .flatMapToByte(s -> Streams.stream(s.getBytes(StandardCharsets.UTF_8)))
                .forEach(System.out::println);
        OptionalByte max = Stream.of(arr)
                .flatMapToByte(s -> Streams.stream(s.getBytes(StandardCharsets.UTF_8)))
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToShort() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToShort");
        Stream.of(arr)
                .flatMapToShort(s-> Streams.stream(new short[]{Short.parseShort(s)}))
                .forEach(System.out::println);
        OptionalShort max = Stream.of(arr)
                .flatMapToShort(s-> Streams.stream(new short[]{Short.parseShort(s)}))
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToChar() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToChar");
        Stream.of(arr)
                .mapToChar(s -> s.charAt(0))
                .forEach(System.out::println);
        OptionalChar max = Stream.of(arr)
                .mapToChar(s -> s.charAt(0))
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToInt() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToInt");
        Stream.of(arr)
                .mapToInt(Integer::parseInt)
                .forEach(System.out::println);
        OptionalInt max = Stream.of(arr)
                .mapToInt(Integer::parseInt)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToLong() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToLong");
        Stream.of(arr)
                .mapToLong(Long::parseLong)
                .forEach(System.out::println);
        OptionalLong max = Stream.of(arr)
                .mapToLong(Long::parseLong)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToFloat() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToFloat");
        Stream.of(arr)
                .mapToFloat(Float::parseFloat)
                .forEach(System.out::println);
        OptionalFloat max = Stream.of(arr)
                .mapToFloat(Float::parseFloat)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }

    @Test
    void flatMapToDouble() {
        String[] arr = arrays(5);
        System.out.println("Stream.flatMapToDouble");
        Stream.of(arr)
                .mapToDouble(Double::parseDouble)
                .forEach(System.out::println);
        OptionalDouble max = Stream.of(arr)
                .mapToDouble(Double::parseDouble)
                .max();
        System.out.println(max.isPresent());
        max.ifPresent(System.out::println);
    }
}