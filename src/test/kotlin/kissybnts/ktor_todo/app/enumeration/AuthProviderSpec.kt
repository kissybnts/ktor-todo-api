package kissybnts.ktor_todo.app.enumeration

import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object AuthProviderSpec : Spek({

    given("AuthProvider") {
        on("fromName()") {
            val correct = "github"
            val wrong = "unknown"

            it("should return GitHub in case of '$correct' is passed as argument") {
                AuthProvider.fromName(correct) shouldEqual AuthProvider.GitHub
            }

            it("should throw IllegalArgumentException in case of Unsupported string is passed") {
                { AuthProvider.fromName(wrong) } shouldThrow IllegalArgumentException::class
            }
        }

    }
})