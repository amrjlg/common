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

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author amrjlg
 **/
class DateUtilTest {

    @Test
    void toDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtil.Pattern.DEFAULT_DATE_TIME_PATTERN);
        LocalDateTime now = LocalDateTime.now();
        String format = DateUtil.Formatter.DEFAULT_DATE_TIME_FORMATTER.format(now);
        System.out.println(format);
        long currentTimeMillis = System.currentTimeMillis();
        Date date = DateUtil.toDate(now);
        System.out.println(date.getTime() + "-" + currentTimeMillis);
        String format1 = dateFormat.format(date);
        System.out.println(format1);
        date = DateUtil.toDate(now.toLocalDate());
        System.out.println(date.getTime() + "-" + currentTimeMillis);
        format1 = dateFormat.format(date);
        System.out.println(format1);
    }


    @Test
    void toLocalDate() {
        Date d = new Date();
        LocalDate date = DateUtil.toLocalDate(d);
        LocalDate now = LocalDate.now();
        print(date);
        print(now);


    }

    @Test
    void toLocalDateTime() {
        Date date = new Date();
        LocalDateTime dateTime = DateUtil.toLocalDateTime(date);
        LocalDateTime now = LocalDateTime.now();

        print(dateTime);
        print(now);
    }

    @Test
    void firstTimeOfDay() {

        print(DateUtil.firstTimeOfDay());
        print(DateUtil.firstTimeOfDay(LocalDate.now()));
        print(DateUtil.firstTimeOfDay(LocalDateTime.now()));

    }

    @Test
    void firstDayOfMonth() {
        print(DateUtil.firstDayOfMonth());
        print(DateUtil.firstDayOfMonth(LocalDate.now()));

        print(DateUtil.firstDayOfMonth(LocalDate.now(), 3));
        print(DateUtil.firstDayOfMonth(1990, 3));

    }

    @Test
    void lastTimeOfDay() {
        print(DateUtil.lastTimeOfDay());
        print(DateUtil.lastTimeOfDay(LocalDate.of(1990,1,2)));
        print(DateUtil.lastTimeOfDay(LocalDateTime.now()));

    }



    void print(LocalDateTime dateTime) {
        System.out.println(DateUtil.Formatter.DEFAULT_DATE_TIME_FORMATTER.format(dateTime));
    }

    void print(LocalDate date) {
        System.out.println(DateUtil.Formatter.DEFAULT_DATE_FORMATTER.format(date));
    }


}