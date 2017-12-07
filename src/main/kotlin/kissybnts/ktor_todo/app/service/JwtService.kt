package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.JwtConstants
import kissybnts.ktor_todo.app.model.UserModel
import kissybnts.ktor_todo.app.objectMapper
import kissybnts.ktor_todo.exception.InvalidCredentialException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.ktor.auth.Principal
import kissybnts.ktor_todo.app.enumeration.TokenType
import kotlinx.coroutines.experimental.async
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtService {
    data class TokenPair(val accessToken: String, val refreshToken: String)

    suspend fun generateTokenPair(user: UserModel): JwtService.TokenPair {
        val token = async { generateToken(user, TokenType.ACCESS_TOKEN) }
        val refresh = async { generateToken(user, TokenType.REFRESH_TOKEN) }
        return TokenPair(token.await(), refresh.await())
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

        val user = try {
            objectMapper.readValue(subject, JwtUserSubject::class.java)
        } catch (ex: Exception) {
            throw InvalidCredentialException()
        }

        if (user.type == type) {
            return user
        } else {
            throw InvalidCredentialException()
        }
    }

    private fun generateToken(user: UserModel, type: TokenType): String {
        val expiration = LocalDateTime.now().plusHours(type.hour()).atZone(ZoneId.systemDefault())
        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(JwtUserSubject(user, type)))
                .setAudience(JwtConstants.Body.AUDIENCE)
                .signWith(SignatureAlgorithm.HS512, type.secretKey())
                .setExpiration(Date.from(expiration.toInstant()))
                .setHeaderParam(JwtConstants.Header.TYPE_KEY, JwtConstants.Header.TYPE)
                .compact()
    }
}

data class JwtUserSubject(val id: Int, val type: TokenType): Principal {
    constructor(user: UserModel, type: TokenType): this(user.id, type)
}