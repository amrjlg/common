package io.github.amrjlg.utils;

import org.junit.jupiter.api.Test;

class StringUtilTest {
    String value = "hello word";
    String separator = "l";

    @Test
    void toCharStringArray() {
        System.out.println(String.join(",", StringUtil.toCharStringArray(value)));
        System.out.println(String.join(",", StringUtil.toCharStringArray("我爱你")));
    }

    @Test
    void split() {
        String values = String.join(",", StringUtil.split(value, separator));
        System.out.println(values);
        values = String.join(",", StringUtil.split(value, ""));
        System.out.println(values);
        values = String.join(",", StringUtil.split(value, "\\*", true));
        System.out.println(values);
        values = String.join(",", StringUtil.split(value, null));
        System.out.println(values);

        values = String.join(",", StringUtil.split(value, separator, true));
        System.out.println(values);
        values = String.join(",", StringUtil.split(value, "", true));
        System.out.println(values);
        values = String.join(",", value.split(""));
        System.out.println(values);

    }

    @Test
    void isBlank() {
        System.out.println(StringUtil.isBlank(value));
        System.out.println(StringUtil.isBlank(""));
        System.out.println(StringUtil.isBlank("   "));
        System.out.println(StringUtil.isBlank(" a  "));
        System.out.println(StringUtil.isBlank(null));
    }

    @Test
    void text() {

        // It is '\t', U+0009 HORIZONTAL TABULATION.
        // It is '\n', U+000A LINE FEED.
        // It is '\u000B', U+000B VERTICAL TABULATION.
        // It is '\f', U+000C FORM FEED.
        // It is '\r', U+000D CARRIAGE RETURN.
        // It is '\u001C', U+001C FILE SEPARATOR.
        // It is '\u001D', U+001D GROUP SEPARATOR.
        // It is '\u001E', U+001E RECORD SEPARATOR.
        // It is '\u001F', U+001F UNIT SEPARATOR.
        System.out.println(StringUtil.text(value));
        System.out.println(StringUtil.text("\t\t"));
        System.out.println(StringUtil.text("\n\n"));
        System.out.println(StringUtil.text("\u000B\u000B"));
        System.out.println(StringUtil.text("\f   \f"));
        System.out.println(StringUtil.text("\r   \r"));
        System.out.println(StringUtil.text("\u001C   \u001C"));
        System.out.println(StringUtil.text("\u001D a  \u001D"));
        System.out.println(StringUtil.text("\u001E a  \u001E"));
        System.out.println(StringUtil.text("\u001F a  \u001F"));
        System.out.println(StringUtil.text(null));
    }


}