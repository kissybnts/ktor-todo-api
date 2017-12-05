package kissybnts.ktor_todo.extension

import java.util.*
import javax.crypto.Cipher

fun Cipher.passwordEncrypt(value: String): String {
    return String(Base64.getEncoder().encode(doFinal(value.toByteArray())))
}

fun Cipher.isCorrectPassword(requestPassword: String, encryptedPassword: String): Boolean {
    return String(doFinal(Base64.getDecoder().decode(encryptedPassword.toByteArray()))) == requestPassword
}