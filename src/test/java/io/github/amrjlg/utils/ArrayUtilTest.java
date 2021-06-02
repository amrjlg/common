package io.github.amrjlg.utils;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class ArrayUtilTest {

    @Test
    void arrays() {
        Number[] strings = ArrayUtil.arrays(128,new BigInteger("1"));
        ArrayUtil.consumer(strings,v->{
            System.out.println(v.getClass().getName());
        });
    }

    @Test
    public void forEach() {
        List<Byte> arr = new ArrayList<>(100000000 / 2);
        for (int i = 0; i < 100000000 / 2; i++) {
            arr.add((byte) 0x1);
        }
        int length = arr.size();
        System.out.println(length);
        String hex = "FFFFFFF";
        StopWatch stopwatch = StopWatch.createStarted();
        for (int i = 0; i < length; i++) {
            HexUtil.toLong(hex);
        }

        stopwatch.stop();
        System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS));
        stopwatch = StopWatch.createStarted();
        for (int i = length - 1; i >= 0; i--) {
            HexUtil.toLong(hex);
        }
        System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS));

        stopwatch = StopWatch.createStarted();
        for (byte b : arr) {
            HexUtil.toLong(hex);
        }
        System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS));

    }

    @Test
    public void map() {

        Integer[] ints = ArrayUtil.arrays(1, 2, 3, 4, 5, 6);

        String[] strings = ArrayUtil.map(ints, String::valueOf, ()->new String[5]);

        ArrayUtil.consumer(strings, System.out::println);

    }


}