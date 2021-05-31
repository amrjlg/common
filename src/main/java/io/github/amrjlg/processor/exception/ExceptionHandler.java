package io.github.amrjlg.processor.exception;

import io.github.amrjlg.processor.Handler;

/**
 * exception handler
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface ExceptionHandler<T extends Exception> extends Handler<String, T> {
    /**
     * 处理异常
     *
     * @param e 异常
     * @return 异常处理结果
     */
    @Override
    String resolve(T e);

}
