package io.github.amrjlg.exception;

public class DefaultExceptionHandler<T extends Exception> implements ExceptionHandler<T> {

    @Override
    public String handler(Exception exception) {
        return exception.getMessage();
    }

    @Override
    public boolean support(Class<? extends Exception> tClass) {
        return RuntimeException.class.isAssignableFrom(tClass);
    }
}
