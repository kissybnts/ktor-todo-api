package kissybnts.ktor_todo.extension

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.time.LocalDate
import java.time.LocalDateTime

object JodaDateTimeExtensionSpec: Spek({
    given("JodaDateTimeExtension") {
        val jodaDateTime = DateTime(2018, 1, 1, 0, 0, 0, 0, DateTimeZone.getDefault())
        val javaLocalDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0)
        val javaLocalDate = LocalDate.of(2018, 1, 1)

        on("toJavaLocalDateTime()") {
            it("should return same local date time") {
                jodaDateTime.toJavaLocalDateTime() shouldEqual javaLocalDateTime
            }
        }
        on("toJavaLocalDate()") {
            it("should return same local date") {
                jodaDateTime.toJavaLocalDate() shouldEqual javaLocalDate
            }
        }
    }
})