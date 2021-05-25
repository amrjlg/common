package io.github.amrjlg.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Pattern;

public class DateTimeUtil {

    public static final Pattern DATA_TIME_PATTERN = Pattern.compile("^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");
    public static final Pattern TIME_PATTERN = Pattern.compile("^([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");
    public static final Pattern DATE_PATTERN = Pattern.compile("^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))$");

    public static final String DATE_CONTACT = "-";
    public static final String TIME_CONTACT = ":";

    public static final String DATE_TIME_CONTACT = " ";


    public static final String YEAR = "yyyy";
    public static final String MONTH = "MM";
    public static final String DAY = "dd";
    public static final String YEAR_MONTH = YEAR + DATE_CONTACT + MONTH;
    public static final String MONTH_DAY = MONTH + DATE_CONTACT + DAY;
    public static final String HOUR = "HH";
    public static final String MINUTE = "mm";
    public static final String SECOND = "ss";
    public static final String MILLISECONDS = "SSS";
    public static final String HOUR_MINUTE = HOUR + TIME_CONTACT + MINUTE;
    public static final String MINUTE_SECOND = MINUTE + TIME_CONTACT + SECOND;

    public static final String DATE = YEAR + DATE_CONTACT + MONTH + DATE_CONTACT + DAY;
    public static final String TIME = HOUR + TIME_CONTACT + MINUTE + TIME_CONTACT + SECOND;
    public static final String DATE_TIME = DATE + DATE_TIME_CONTACT + TIME;
    public static final String DATE_TIME_MILLISECONDS = DATE_TIME + "." + MILLISECONDS;

    public static final String DATE_TIME_MILLISECONDS_NO_SEPARATOR = YEAR + MONTH + DAY + HOUR + MINUTE + SECOND + MILLISECONDS;

    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME);
    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE);
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME);
    public static DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(YEAR_MONTH);
    public static DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern(YEAR);

    public static DateTimeFormatter DATE_TIME_MILLISECONDS_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_MILLISECONDS);

    /**
     * 是否是时间 HH:mm:ss
     *
     * @param time 时间字符串
     * @return boolean
     */
    public static boolean isTime(String time) {
        return TIME_PATTERN.matcher(time).matches();
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

    /**
     * 是否是时间 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 日期时间字符串
     * @return boolean
     */
    public static boolean isDateTime(String dateTime) {
        return DATA_TIME_PATTERN.matcher(dateTime).matches();
    }

    public static String date() {
        return date(LocalDate.now());
    }

    public static String date(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    public static String date(LocalDateTime dateTime) {
        return DATE_FORMATTER.format(dateTime.toLocalDate());
    }

    public static String dateTime() {
        return dateTime(LocalDateTime.now());
    }

    public static String dateTime(LocalDate date) {
        return dateTime(date, LocalTime.now());
    }

    private static String dateTime(LocalDate date, LocalTime time) {
        return dateTime(LocalDateTime.of(date, time));
    }

    public static String dateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String dateTimeMilliseconds(LocalDateTime dateTime) {
        return DATE_TIME_MILLISECONDS_FORMATTER.format(dateTime);
    }

    public static LocalDate toDate(String date) {
        if (isDate(date)) {
            return LocalDate.parse(date, DATE_FORMATTER);
        }
        return null;
    }

    public static LocalDateTime toDateTime(String dateTime) {
        if (isDateTime(dateTime)) {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        }
        return null;
    }

    public static LocalTime toTime(String time) {
        if (isTime(time)) {
            return LocalTime.parse(time, TIME_FORMATTER);
        }
        return null;
    }

    public static LocalDate firstDayOfMonth() {
        return firstDayOfMonth(LocalDate.now());
    }

    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate firstDayOfMonth(LocalDateTime dateTime) {
        return firstDayOfMonth(dateTime.toLocalDate());
    }

    public static LocalDate lastDayOfMonth() {
        return lastDayOfMonth(LocalDate.now());
    }

    public static LocalDate lastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    public static LocalDate lastDayOfMonth(LocalDateTime dateTime) {
        return lastDayOfMonth(dateTime.toLocalDate());
    }

    public static LocalDate nextWeek(LocalDate date) {
        return date.plusWeeks(1);
    }

    public static LocalDate nextWeek() {
        return nextWeek(LocalDate.now());
    }

    public static LocalDate nextWeek(LocalDateTime dateTime) {
        return nextWeek(dateTime.toLocalDate());
    }

}
