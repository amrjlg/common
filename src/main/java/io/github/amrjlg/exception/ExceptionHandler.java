package io.github.amrjlg.exception;

/**
 * exception handler
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface ExceptionHandler<T extends Exception> {


    String handler(T e);

    boolean support(Class<? extends Exception> tClass);

}
