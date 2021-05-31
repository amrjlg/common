package io.github.amrjlg.steam;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ArrayStreamTest {

    @Test
    public void stream(){
        int sum = Arrays.stream(new double[]{1, 2, 3, 4}).mapToInt(d -> (int) d).sum();

    }
}
