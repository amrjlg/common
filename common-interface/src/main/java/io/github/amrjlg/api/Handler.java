/*
 *  Copyright (c) 2021-2021 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.amrjlg.api;

import java.util.function.Function;

/**
 * parse one <code>Src</code> to <code>Result</code>
 *
 * @param <Result> output type
 * @param <Src>    source type
 * @author amrjlg
 */
@FunctionalInterface
public interface Handler<Result, Src> extends Function<Src, Result> {
    /**
     * parse one <code>Src</code> to <code>Result</code>
     *
     * @param src param
     * @return result
     */
    Result resolve(Src src);

    /**
     * parse one <code>Src</code> to <code>Result</code>
     *
     * @param src param
     * @return result
     */
    @Override
    default Result apply(Src src) {
        return resolve(src);
    }
}
