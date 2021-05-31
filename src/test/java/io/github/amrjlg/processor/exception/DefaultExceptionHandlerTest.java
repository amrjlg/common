package io.github.amrjlg.processor.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class DefaultExceptionHandlerTest {

    @Test
    public void handler(){
        DefaultExceptionHandler handler = new DefaultExceptionHandler();

        RuntimeException exception = new RuntimeException("222");

        if (handler.support(exception)){
            System.out.println(Arrays.toString(handler.andThen(s -> s.split("")).apply(exception)));
        }

        ExceptionHandler<Exception> exceptionHandler = new DefaultExceptionHandler();

    }

}