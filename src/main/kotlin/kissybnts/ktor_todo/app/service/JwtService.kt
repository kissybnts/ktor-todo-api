package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.EnvironmentVariableKeys
import kissybnts.ktor_todo.app.JwtConstants
import kissybnts.ktor_todo.app.model.UserModel
import kissybnts.ktor_todo.app.pipeline.objectMapper
import kissybnts.ktor_todo.exception.InvalidCredentialException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.ktor.auth.Principal
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtService {

    fun generateToken(user: UserModel, type: TokenType): String {
        val expiration = LocalDateTime.now().plusHours(type.hour()).atZone(ZoneId.systemDefault())
        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(JwtUserSubject(user)))
                .setAudience(JwtConstants.Body.AUDIENCE)
                .signWith(SignatureAlgorithm.HS512, type.secretKey())
                .setExpiration(Date.from(expiration.toInstant()))
                .setHeaderParam(JwtConstants.Header.TYPE_KEY, JwtConstants.Header.TYPE)
                .compact()
    }

    fun verifyToken(token: String, type: TokenType): JwtUserSubject {
        val jws = try {
            Jwts.parser().setSigningKey(type.secretKey()).parseClaimsJws(token)
        } catch (ex: ExpiredJwtException) {
            throw ex
        } catch (ex: Exception) {
            throw InvalidCredentialException(ex.message ?: DefaultMessages.Error.INVALID_CREDENTIAL)
        }

        if (jws.body.audience != JwtConstants.Body.AUDIENCE) {
            throw InvalidCredentialException()
        }

        val subject = jws.body.subject ?: throw InvalidCredentialException()

        return try {
            objectMapper.readValue(subject, JwtUserSubject::class.java)
        } catch (ex: Exception) {
            throw InvalidCredentialException()
        }
    }
}

enum class TokenType(private val key: String, private val expirationKey: String) {
    ACCESS_TOKEN(EnvironmentVariableKeys.JWT_SECRET_KEY, EnvironmentVariableKeys.JWT_EXPIRE_TIME),
    REFRESH_TOKEN(EnvironmentVariableKeys.JWT_REFRESH_SECRET_KEY, EnvironmentVariableKeys.JWT_REFRESH_EXPIRE_TIME);

    fun secretKey(): String = System.getenv(this.key)
    fun hour(): Long = System.getenv(this.expirationKey).toLong()
}

data class JwtUserSubject(val id: Int, val providerId: Int): Principal {
    constructor(user: UserModel): this(user.id, user.providerId)
}