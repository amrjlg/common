package io.github.amrjlg.processor;

import java.util.function.Predicate;

/**
 * 支持接口
 *
 * @param <T>
 */
public interface Support<T> extends Predicate<T> {
    boolean support(T t);

    @Override
    default boolean test(T t) {
        return support(t);
    }
}
