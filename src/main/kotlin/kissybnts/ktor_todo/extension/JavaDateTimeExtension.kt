package kissybnts.ktor_todo.extension

import org.joda.time.DateTime
import java.time.LocalDate
import java.time.LocalDateTime

internal fun LocalDate.toJodaDate(): DateTime {
    return DateTime(year, monthValue, dayOfMonth, 0, 0, 0)
}

internal fun LocalDateTime.toJodaDateTime(): DateTime {
    return DateTime(year, monthValue, dayOfMonth, hour, minute, second)
}