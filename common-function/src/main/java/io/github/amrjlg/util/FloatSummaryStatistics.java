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

import io.github.amrjlg.function.FloatConsumer;

/**
 * @author amrjlg
 **/
public class FloatSummaryStatistics implements FloatConsumer {
    private long count;
    private double sum;
    private float min = Float.MAX_VALUE;
    private float max = Float.MIN_VALUE;


    @Override
    public void accept(float value) {
        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    public void combine(FloatSummaryStatistics other) {
        count += other.count;
        sum += other.sum;
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public final double getAverage() {
        return getCount() > 0 ? (double) getSum() / getCount() : 0.0d;
    }
}
