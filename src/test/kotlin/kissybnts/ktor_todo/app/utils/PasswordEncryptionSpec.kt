package kissybnts.ktor_todo.app.utils

import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object PasswordEncryptionSpec: Spek({
    given("PasswordEncryptionSpec") {
        val passwordLiteral = "test"
        val wrongPassword = "other_password"
        on("passwordEncrypt()") {
            it("should return not same the value passed as argument") {
                PasswordEncryption.passwordEncrypt(passwordLiteral) shouldNotEqual passwordLiteral
            }

            it("should return same value is given even if it's executed more than once") {
                PasswordEncryption.passwordEncrypt(passwordLiteral) shouldEqual PasswordEncryption.passwordEncrypt(passwordLiteral)
            }

            it("should not return same value in case of different value is given") {
                PasswordEncryption.passwordEncrypt(passwordLiteral) shouldNotEqual PasswordEncryption.passwordEncrypt(wrongPassword)
            }
        }

        on("isCorrectPassword()") {
            val encryptedPassword = PasswordEncryption.passwordEncrypt(passwordLiteral)
            it("should return true in case of correct password is requested") {
                PasswordEncryption.isCorrectPassword(passwordLiteral, encryptedPassword) shouldEqual true
            }

            it("should return false in case of wrong password is requested") {
                PasswordEncryption.isCorrectPassword(wrongPassword, encryptedPassword) shouldEqual false
            }
        }
    }
})