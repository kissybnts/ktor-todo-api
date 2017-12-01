package com.kissybnts.extension

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.app.FormatConstants
import com.kissybnts.getEnv
import io.ktor.config.ApplicationConfig
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import org.jetbrains.exposed.sql.Database
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * set up database config with `ApplicationConfig` and environment variables.
 */
internal fun Database.Companion.setUp(databaseConfig: ApplicationConfig) {
    apply {
        val host = getEnv(EnvironmentVariableKeys.Gradle.DB_HOST, "127.0.0.1")
        val port = getEnv(EnvironmentVariableKeys.Gradle.DB_PORT, "3306")
        val name = getEnv(EnvironmentVariableKeys.Gradle.DB_NAME, "ktor-todo")
        val user = getEnv(EnvironmentVariableKeys.Gradle.DB_USER, "root")
        val password = getEnv(EnvironmentVariableKeys.Gradle.DB_PASS, "")
        val driver = databaseConfig.property("driver").getString()
        connect("jdbc:mysql://$host:$port/$name?useSSL=false", driver, user, password)
    }
}

internal fun ContentNegotiation.Configuration.jacksonSetUp() {
    jackson {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        registerModule(JavaTimeModule().setUp())
    }
}

private fun JavaTimeModule.setUp(): JavaTimeModule {
    addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT)))
    addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT)))
    addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern(FormatConstants.DATE_FORMAT)))
    addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatter.ofPattern(FormatConstants.DATE_FORMAT)))
    return this
}