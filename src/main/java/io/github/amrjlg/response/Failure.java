package io.github.amrjlg.response;

/**
 * failure
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface Failure<T> extends Result<T> {

    @Override
    default int getCode() {
//        return Result.super.getCode();
        return 400;
    }

    @Override
    default boolean isSuccess() {
        return false;
    }

    @Override
    default T getData() {
        return null;
    }

    @Override
    String getMsg();

    /**
     * simple implements
     *
     * @param msg failure message
     * @param <T> directed set null
     * @return failure result with message
     */
    static <T> Failure<T> msg(String msg) {
        return () -> msg;
    }
}
