/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 *
 * @author amrjlg
 **/
public class DateUtil {

    public static java.util.regex.Pattern DATE_PATTERN = java.util.regex.Pattern.compile(Pattern.DEFAULT_DATE_PATTERN);
    public static java.util.regex.Pattern TIME_PATTERN = java.util.regex.Pattern.compile(Pattern.DEFAULT_TIME_PATTERN);
    public static java.util.regex.Pattern DATA_TIME_PATTERN = java.util.regex.Pattern.compile(Pattern.DEFAULT_DATE_TIME_PATTERN);

    public interface Pattern {
        String YEAR = "yyyy";
        String MONTH = "MM";
        String DAY = "dd";

        String DATE_CONCAT = "-";

        String HOUR = "HH";
        String MINUTE = "mm";
        String SECOND = "ss";

        String MILLISECONDS = "SSS";

        String TIME_CONCAT = ":";

        String DEFAULT_DATE_PATTERN = YEAR + DATE_CONCAT + MONTH + DATE_CONCAT + DAY;

        String DEFAULT_TIME_PATTERN = HOUR + TIME_CONCAT + MINUTE + TIME_CONCAT + SECOND;
        String DEFAULT_TIME_12_PATTERN = HOUR.toLowerCase(Locale.ENGLISH) + TIME_CONCAT + MINUTE + TIME_CONCAT + SECOND;

        String DEFAULT_DATE_TIME_PATTERN = DEFAULT_DATE_PATTERN + StringUtil.SPACE + DEFAULT_TIME_PATTERN;
        String DEFAULT_DATE_TIME_12_PATTERN = DEFAULT_DATE_PATTERN + StringUtil.SPACE + DEFAULT_TIME_12_PATTERN;

        String DEFAULT_DATE_TIME_MILLISECONDS = DEFAULT_DATE_TIME_PATTERN + "." + MILLISECONDS;
        String DEFAULT_DATE_TIME_MILLISECONDS_NO_SEPARATOR = YEAR + MONTH + DAY + HOUR + MINUTE + SECOND + MILLISECONDS;

        String YEAR_MONTH_DAY = YEAR + MONTH + DAY + HOUR + MINUTE + SECOND;
        String HOUR_MINUTE_SECOND = HOUR + MINUTE + SECOND;
        String YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = YEAR_MONTH_DAY + HOUR_MINUTE_SECOND;
    }

    public static class Formatter {
        public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(Pattern.DEFAULT_TIME_PATTERN);
        public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = builder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral(Pattern.DATE_CONCAT)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral(Pattern.DATE_CONCAT)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter();
        public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = builder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral(Pattern.DATE_CONCAT)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral(Pattern.DATE_CONCAT)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral(StringUtil.SPACE)
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(Pattern.TIME_CONCAT)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(Pattern.TIME_CONCAT)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();

        public static final DateTimeFormatter DEFAULT_DATE_TIME_MILLISECONDS_FORMATTER = DateTimeFormatter.ofPattern(Pattern.DEFAULT_DATE_TIME_MILLISECONDS);

        public static final DateTimeFormatter DEFAULT_DATE_TIME_12_FORMATTER = DateTimeFormatter.ofPattern(Pattern.DEFAULT_DATE_TIME_12_PATTERN);

        public static final DateTimeFormatter YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_FORMATTER = builder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();


        public static final DateTimeFormatter YEAR_MONTH_DAY_FORMATTER = builder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter();

        public static final DateTimeFormatter HOUR_MINUTE_SECOND_FORMATTER = builder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();

        public static DateTimeFormatterBuilder builder() {
            return new DateTimeFormatterBuilder();
        }
    }

    /**
     * 是否是时间 HH:mm:ss
     *
     * @param time 时间字符串
     * @return boolean
     */
    public static boolean isTime(String time) {
        return TIME_PATTERN.matcher(time).matches();
    }

    public static boolean isTime(String time, String pattern) {
        return isTime(time, java.util.regex.Pattern.compile(pattern));
    }

    public static boolean isTime(String time, java.util.regex.Pattern pattern) {
        return pattern.matcher(time).matches();
    }

    /**
     * 是否是时间 yyyy-MM-dd
     *
     * @param date 日期字符串
     * @return boolean
     */
    public static boolean isDate(String date) {
        return DATE_PATTERN.matcher(date).matches();
    }

    public static boolean isDate(String date, String pattern) {
        return isDate(date, java.util.regex.Pattern.compile(pattern));
    }

    public static boolean isDate(String date, java.util.regex.Pattern pattern) {
        return pattern.matcher(date).matches();
    }

    /**
     * 是否是时间 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 日期时间字符串
     * @return boolean
     */
    public static boolean isDateTime(String dateTime) {
        return DATA_TIME_PATTERN.matcher(dateTime).matches();
    }

    public static boolean isDateTime(String dateTime, String pattern) {
        return isDateTime(dateTime, java.util.regex.Pattern.compile(pattern));
    }

    public static boolean isDateTime(String dateTime, java.util.regex.Pattern pattern) {
        return pattern.matcher(dateTime).matches();
    }


    public static Date toDate(LocalDateTime dateTime) {
        Instant instant = ZonedDateTime.of(dateTime, ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date toDate(LocalDate date) {
        return toDate(LocalDateTime.of(date, LocalTime.MIN));
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate parseLocalDate(String date) {
        return LocalDate.parse(date, Formatter.DEFAULT_DATE_FORMATTER);
    }

    public static LocalDateTime parseLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, Formatter.DEFAULT_DATE_TIME_FORMATTER);
    }


    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime firstTimeOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    public static LocalDateTime firstTimeOfDay(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN);
    }

    public static LocalDateTime firstTimeOfDay() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    public static LocalDateTime lastTimeOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX);
    }

    public static LocalDateTime lastTimeOfDay(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX);
    }

    public static LocalDateTime lastTimeOfDay() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }


    public static LocalDate firstDayOfMonth() {
        return firstDayOfMonth(LocalDate.now());
    }

    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate firstDayOfMonth(LocalDate date, int month) {
        if (!checkMonth(month)) {
            throw new IllegalArgumentException("月份不正确");
        }
        return date.with(ChronoField.MONTH_OF_YEAR, month).with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate firstDayOfMonth(int year, int month) {
        if (!checkYear(year)) {
            throw new IllegalArgumentException("年份不正确");
        }
        if (!checkMonth(month)) {
            throw new IllegalArgumentException("月份不正确");
        }
        return LocalDate.of(year, month, 1);
    }

    public static boolean checkMonth(int month) {
        return ChronoField.MONTH_OF_YEAR.range().isValidValue(month);
    }

    public static boolean checkYear(int year) {
        return ChronoField.YEAR.range().isValidValue(year);
    }

    public static boolean checkDay(int day) {
        return ChronoField.DAY_OF_MONTH.range().isValidValue(day);
    }

    public static String time(LocalTime time) {
        return Formatter.DEFAULT_TIME_FORMATTER.format(time);
    }

    public static String time(LocalDate date) {
        return "00:00:00";
    }

    public static String time(LocalDateTime dateTime) {
        return time(dateTime.toLocalTime());
    }

    public static String date(LocalDate date) {
        return Formatter.DEFAULT_DATE_FORMATTER.format(date);
    }

    public static String date(LocalDateTime dateTime) {
        return date(dateTime.toLocalDate());
    }

    public static String dateTimeMilliseconds(LocalDate date) {
        return dateTimeMilliseconds(LocalDateTime.of(date, LocalTime.MIN));
    }

    public static String dateTimeMilliseconds(LocalDateTime dateTime) {
        return Formatter.DEFAULT_DATE_TIME_MILLISECONDS_FORMATTER.format(dateTime);
    }


}
