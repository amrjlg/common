package io.github.amrjlg.utils;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class HexUtilTest {

    @Test
    void parse() {

        System.out.println(HexUtil.parse(Integer.MAX_VALUE / 100));
        System.out.println(HexUtil.parse(Integer.MAX_VALUE / 200));
        System.out.println(HexUtil.parse(Integer.MAX_VALUE / 300));
        System.out.println(HexUtil.parse(Integer.MAX_VALUE / 400));


        System.out.println();
        System.out.println(HexUtil.parse(Long.MAX_VALUE / 10));
        System.out.println(HexUtil.parse(Long.MAX_VALUE / 20));
        System.out.println(HexUtil.parse(Long.MAX_VALUE / 30));
        System.out.println(HexUtil.parse(Long.MAX_VALUE / 40));

    }

    @Test
    void toLong() {
        int i = Integer.MAX_VALUE;
        int times = 100000000;
        String hex = HexUtil.parse(i, "");
        System.out.println(i);
        System.out.println(hex);
        System.out.println(HexUtil.toLong(hex));
        StopWatch stopwatch = StopWatch.createStarted();
        for (int j = 0; j < times; j++) {
            HexUtil.toLong(hex);
        }
        stopwatch.stop();
        System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS));
        stopwatch.reset();
        stopwatch.start();
        for (int j = 0; j < times; j++) {
            Integer.parseInt(hex, 16);
        }
        stopwatch.stop();
        System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS));
    }
}