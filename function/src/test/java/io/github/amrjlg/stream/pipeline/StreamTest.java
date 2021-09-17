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

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.stream.Stream;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.pipeline.ReferencePipeline;
import io.github.amrjlg.stream.spliterator.Spliterator;
import io.github.amrjlg.stream.spliterator.Spliterators;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author amrjlg
 **/
public class StreamTest {


    @Test
    public void stream() {

        String[] array = array("a", "b", "c", "d", "e", "f");

        Spliterator<String> spliterator = Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);

        Stream<String> stream = new ReferencePipeline.Head<>(spliterator, StreamOpFlag.fromCharacteristics(spliterator), false);
        Comparator<String> comparator = String::compareTo;
        // TODO forEach
        stream.map(String::toUpperCase).sorted(comparator.reversed()).forEach(System.out::println);


    }

    public static <T> T[] array(T... t) {
        return t;
    }
}
