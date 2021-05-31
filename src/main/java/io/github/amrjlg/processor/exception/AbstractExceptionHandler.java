package io.github.amrjlg.processor.exception;

import io.github.amrjlg.processor.Support;

/**
 * 异常处理抽象类
 * <p></p>
 * 针对不同异常进行响应实现
 */
public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandler<T>, Support<T> {

    private final Support<Class<? extends Exception>> predicate;

    public AbstractExceptionHandler(Support<Class<? extends Exception>> predicate) {
        this.predicate = predicate;
    }


    @Override
    public final boolean support(T t) {
        if (t == null) {
            return false;
        }
        return predicate.support(t.getClass());
    }
}
// 另一种接口方式
//public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandler<T>, Support<T> {
//
//    private final Class<?> supportedClass;
//
//    protected AbstractExceptionHandler(Class<?> supportedClass) {
//        this.supportedClass = supportedClass;
//    }
//
//    @Override
//    public final boolean support(T t) {
//        if (t == null) {
//            return false;
//        }
//        return this.supportedClass.isAssignableFrom(t.getClass());
//    }
//}
