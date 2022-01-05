/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.amrjlg.utils;

import java.time.*;
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

    public static java.util.regex.Pattern date_pattern = java.util.regex.Pattern.compile(Pattern.DEFAULT_DATE_PATTERN);

    public interface Pattern {
        String YEAR = "yyyy";
        String MONTH = "MM";
        String DAY = "dd";

        String DATE_CONCAT = "-";

        String HOUR = "HH";
        String MINUTE = "mm";
        String SECOND = "ss";

        String TIME_CONCAT = ":";

        String DEFAULT_DATE_PATTERN = YEAR + DATE_CONCAT + MONTH + DATE_CONCAT + DAY;

        String DEFAULT_TIME_PATTERN = HOUR + TIME_CONCAT + MINUTE + TIME_CONCAT + SECOND;
        String DEFAULT_TIME_12_PATTERN = HOUR.toLowerCase(Locale.ENGLISH) + TIME_CONCAT + MINUTE + TIME_CONCAT + SECOND;

        String DEFAULT_DATE_TIME_PATTERN = DEFAULT_DATE_PATTERN + StringUtil.SPACE + DEFAULT_TIME_PATTERN;
        String DEFAULT_DATE_TIME_12_PATTERN = DEFAULT_DATE_PATTERN + StringUtil.SPACE + DEFAULT_TIME_12_PATTERN;

        String YEAR_MONTH_DAY = YEAR + MONTH + DAY + HOUR + MINUTE + SECOND;
        String HOUR_MINUTE_SECOND = HOUR + MINUTE + SECOND;
        String YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = YEAR_MONTH_DAY + HOUR_MINUTE_SECOND;
    }

    public static class Formatter {
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

}
