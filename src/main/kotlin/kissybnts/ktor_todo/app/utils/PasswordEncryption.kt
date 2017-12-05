package kissybnts.ktor_todo.app.utils

import kissybnts.ktor_todo.app.EnvironmentVariableKeys
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object PasswordEncryption {
    private val key = System.getenv(EnvironmentVariableKeys.PASSWORD_CRYPT_KEY)
    private val cipherForEncrypt = Cipher.getInstance("AES").apply {
        init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(),"AES"))
    }
    private val cipherForDecrypt = Cipher.getInstance("AES").apply {
        init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"))
    }

    fun passwordEncrypt(value: String): String {
        return String(Base64.getEncoder().encode(cipherForEncrypt.doFinal(value.toByteArray())))
    }

    fun isCorrectPassword(requestPassword: String, encryptedPassword: String): Boolean {
        return String(cipherForDecrypt.doFinal(Base64.getDecoder().decode(encryptedPassword.toByteArray()))) == requestPassword
    }
}