package com.kissybnts.extension

import org.joda.time.DateTime
import java.time.LocalDateTime

fun DateTime.toJavaLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute)
}