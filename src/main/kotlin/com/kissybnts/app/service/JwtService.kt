package com.kissybnts.app.service

import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.app.model.UserModel
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtService {
    fun generateToken(user: UserModel, type: TokenType): String {
        val expiration = LocalDateTime.now().plusHours(type.hour()).atZone(ZoneId.systemDefault())
        return Jwts.builder()
                .setSubject(user.id.toString())
                .setAudience("Ktor-todo")
                .signWith(SignatureAlgorithm.HS512, type.secretKey())
                .setExpiration(Date.from(expiration.toInstant()))
                .setHeaderParam("typ", "JWT")
                .compact()
    }
}

enum class TokenType(private val key: String, private val expirationKey: String) {
    ACCESS_TOKEN(EnvironmentVariableKeys.JWT_SECRET_KEY, EnvironmentVariableKeys.JWT_EXPIRE_TIME),
    REFRESH_TOKEN(EnvironmentVariableKeys.JWT_REFRESH_SECRET_KEY, EnvironmentVariableKeys.JWT_REFRESH_EXPIRE_TIME);

    fun secretKey(): String = System.getenv(this.key)
    fun hour(): Long = System.getenv(this.expirationKey).toLong()
}