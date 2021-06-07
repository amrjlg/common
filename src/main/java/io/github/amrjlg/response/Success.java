package io.github.amrjlg.response;


/**
 * Success result
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface Success<T> extends Result<T> {
    @Override
    default boolean isSuccess() {
        return true;
    }

    @Override
    default String getMsg() {
        return "SUCCESS";
    }

    /**
     * simple implements
     *
     * @param data response data could be null
     * @param <T> type
     * @return success
     */
    static <T> Success<T> ok(T data) {
        return () -> data;
    }

    /**
     * the result signed success
     * @param <T> dat type
     * @return only sign success
     */
    static <T> Success<T> ok() {
        return () -> null;
    }

}
