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

package io.github.amrjlg.utils;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * string util
 *
 * @author amrjlg
 */
public class StringUtil {

    public static String SPACE = " ";

    /**
     * 将字符按照字符拆分
     *
     * @param value 字符串
     * @return 单个字符作为字符串的数组
     */
    public static String[] toCharStringArray(String value) {
        if (Objects.isNull(value)) {
            return new String[0];
        }
        return value.chars().mapToObj(v -> String.valueOf((char) v)).toArray(String[]::new);
    }

    /**
     * 字符串分割
     * 字符串会进行trim截取掉两端空白字符
     * 将连续分隔符当作分隔符进行字符串分割
     * <p>
     * {@link #split(String, String, boolean)}
     *
     * @param value     被分割字字符字符串
     * @param separator 分隔符
     * @return 分割后的数组
     */
    public static String[] split(String value, String separator) {
        return split(value, separator, false);
    }

    /**
     * 字符串分割 字符串会进行trim截取掉两端空白字符
     *
     * @param value            要分割的字符串
     * @param separator        分隔符
     * @param separatorAsValue 是否保留分割符为分割后的值
     * @return 分割后的数组
     * value是null或者长度不足1 将返回空长度为0的数组,separator是null或者空白字符串时将value为值长度为1的字符串数组<p>
     * example : value = "asssdf"; separator = "s" separatorAsValue=false;<br >
     * 分割结果是 ["a","df"]
     * </p>
     * <p>
     * example : value = "asssdf"; separator = "s" separatorAsValue=true;<br >
     * 分割结果是 ["a","s","s","s","df"]
     * </p>
     */
    public static String[] split(String value, String separator, boolean separatorAsValue) {
        value = text(value);
        if (Objects.isNull(value)) {
            return new String[0];
        }
        if (Objects.isNull(separator) || separator.length() < 1) {
            return new String[]{value};
        }
        StringTokenizer tokenizer = new StringTokenizer(value, separator, separatorAsValue);
        int tokens = tokenizer.countTokens();
        String[] values = new String[tokens];
        for (int i = 0; i < tokens; i++) {
            values[i] = tokenizer.nextToken();
        }
        return values;
    }

    /**
     * check string is null or empty string or whole white-space char-sequence
     *
     * @param value test string
     * @return only white-space-char or null or length is 0 will return true otherwise will be false
     */
    public static boolean isBlank(String value) {
        if (Objects.nonNull(value)) {
            for (int i = 0; i < value.length(); i++) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * check string is null or length is 0
     *
     * @param value test string
     * @return empty will return true otherwise will be false
     */
    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * remove head and tail white-space char
     *
     * @param value string
     * @return the text of value if value all of white-space will return null
     */
    public static String text(String value) {
        if (isBlank(value)) {
            return null;
        } else {
            return value.trim();
        }
    }
}
