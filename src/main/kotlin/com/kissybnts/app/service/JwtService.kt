package com.kissybnts.app.service

import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.app.model.UserModel
import com.kissybnts.app.pipeline.objectMapper
import com.kissybnts.exception.BadCredentialException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtService {
    fun generateToken(user: UserModel, type: TokenType): String {
        val expiration = LocalDateTime.now().plusHours(type.hour()).atZone(ZoneId.systemDefault())
        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(JWTUserSubject(user)))
                .setAudience("Ktor-todo")
                .signWith(SignatureAlgorithm.HS512, type.secretKey())
                .setExpiration(Date.from(expiration.toInstant()))
                .setHeaderParam("typ", "JWT")
                .compact()
    }

    fun verifyToken(token: String, type: TokenType): JWTUserSubject {
        val jws = try {
            Jwts.parser().setSigningKey(type.secretKey()).parseClaimsJws(token)
        } catch (ex: Exception) {
            throw BadCredentialException(ex.message?: "Invalid credential.")
        }

        val now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())

        if (jws.body.expiration.before(now)) {
            throw BadCredentialException("Token has already been expired.")
        }

        val subject = jws.body.subject ?: throw BadCredentialException("Invalid token.")

        return try {
            objectMapper.readValue(subject, JWTUserSubject::class.java)
        } catch (ex: Exception) {
            throw BadCredentialException("Invalid token.")
        }
    }
}

enum class TokenType(private val key: String, private val expirationKey: String) {
    ACCESS_TOKEN(EnvironmentVariableKeys.JWT_SECRET_KEY, EnvironmentVariableKeys.JWT_EXPIRE_TIME),
    REFRESH_TOKEN(EnvironmentVariableKeys.JWT_REFRESH_SECRET_KEY, EnvironmentVariableKeys.JWT_REFRESH_EXPIRE_TIME);

    fun secretKey(): String = System.getenv(this.key)
    fun hour(): Long = System.getenv(this.expirationKey).toLong()
}

data class JWTUserSubject(val id: Int, val providerId: Int) {
    constructor(user: UserModel): this(user.id, user.providerId)
}