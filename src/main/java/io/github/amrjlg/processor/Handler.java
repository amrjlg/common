package io.github.amrjlg.processor;

import java.util.function.Function;

/**
 * 转换接口
 */
public interface Handler<Result, Src> extends Function<Src, Result> {

    Result resolve(Src src);

    @Override
    default Result apply(Src src) {
        return resolve(src);
    }
}
