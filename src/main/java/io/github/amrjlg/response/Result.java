package io.github.amrjlg.response;

/**
 * 返回值
 * <p>
 * 2021/3/4
 *
 * @author jiang
 **/
public interface Result<T> {
    /**
     * judge this result is successful
     *
     * @return true success false failure
     */
    boolean isSuccess();

    /**
     * the message for this result
     *
     * @return message
     */
    String getMsg();

    /**
     * real data for the result
     *
     * @return data
     */
    T getData();
}
