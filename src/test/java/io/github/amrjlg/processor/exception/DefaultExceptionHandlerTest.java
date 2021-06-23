package io.github.amrjlg.processor.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class DefaultExceptionHandlerTest {

    @Test
    public void handler() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();

        RuntimeException exception = new RuntimeException("222");

        if (handler.support(exception)) {
            System.out.println(Arrays.toString(handler.andThen(s -> s.split("")).apply(exception)));
        }

        ExceptionHandler<Exception> exceptionHandler = new DefaultExceptionHandler();

    }


    @Test
    public void sync() throws InterruptedException {

        ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>(10);
        String[] keys = new String[]{"loop1", "loop2", "loop3", "loop4"};
        int loop = 100010;
        for (int i = 0; i < loop; i++) {
            Thread thread = new Thread(() -> {
                for (String key : keys) {

                    AtomicLong absent = counts.putIfAbsent(key, new AtomicLong(0));
                    if (absent != null) {
                        absent.incrementAndGet();
                    }
                }
            });
            thread.join();
            thread.start();

        }


        counts.forEach((k, v) -> {
            System.out.println(k + "-" + v.intValue());
        });


    }

}