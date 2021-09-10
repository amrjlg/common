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

package io.github.amrjlg.util;

import io.github.amrjlg.function.CharConsumer;

/**
 * @author amrjlg
 **/
public class CharSummaryStatistics implements CharConsumer {
    private long count;
    private long sum;
    private char min = Character.MAX_VALUE;
    private char max = Character.MIN_VALUE;


    @Override
    public void accept(char value) {
        ++count;
        sum += value;
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }

    public void combine(CharSummaryStatistics other) {
        count += other.count;
        sum += other.sum;
        if (other.min < min) {
            min = other.min;
        }
        if (other.max > max) {
            max = other.max;
        }
    }

    public final long getCount() {
        return count;
    }

    public final long getSum() {
        return sum;
    }

    public final char getMin() {
        return min;
    }

    public final char getMax() {
        return max;
    }

    public final double getAverage() {
        return getCount() > 0 ? (double) getSum() / getCount() : 0.0d;
    }
}
