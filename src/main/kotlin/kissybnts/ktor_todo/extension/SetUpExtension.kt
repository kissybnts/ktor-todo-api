package kissybnts.ktor_todo.extension

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.EnvironmentVariableKeys
import kissybnts.ktor_todo.app.FormatConstants
import kissybnts.ktor_todo.app.response.ErrorResponse
import kissybnts.ktor_todo.exception.InvalidCredentialException
import kissybnts.ktor_todo.exception.ProviderAuthenticationErrorException
import kissybnts.ktor_todo.exception.ResourceNotFoundException
import kissybnts.ktor_todo.getEnv
import io.jsonwebtoken.ExpiredJwtException
import io.ktor.application.call
import io.ktor.config.ApplicationConfig
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.jackson.jackson
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.ktor.util.error
import kissybnts.ktor_todo.exception.UserNotFoundException

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

/**
 * set up the jackson configuration.
 *
 * - Indent output is enable
 * - Naming strategy is snake case
 * - Configure default format for LocalDateTime and LocalDate ( `yyyy/MM/dd HH:mm:ss` )
 */
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

internal fun StatusPages.Configuration.setUp(log: Logger) {

    exception<IllegalStateException> {
        log.error(it)
        call.badRequest(ErrorResponse(it, DefaultMessages.Error.SOMETHING_WRONG))
    }
    exception<IllegalArgumentException> {
        log.error(it)
        call.badRequest(ErrorResponse(it, DefaultMessages.Error.BAD_REQUEST))
    }
    exception<ResourceNotFoundException> {
        log.error(it)
        call.notFound(ErrorResponse(it, DefaultMessages.Error.RESOURCE_NOT_FOUND))
    }
    exception<ProviderAuthenticationErrorException> {
        log.error(it)
        call.badRequest(ErrorResponse(it, DefaultMessages.Error.AUTH_PROCESS_FAILED))
    }
    exception<InvalidCredentialException> {
        log.error(it)
        call.unauthorized(ErrorResponse(it, DefaultMessages.Error.INVALID_CREDENTIAL))
    }
    exception<ExpiredJwtException> {
        log.error(it)
        call.unauthorized(ErrorResponse(it, "Token has already been expired."))
    }
    exception<UserNotFoundException> {
        log.error(it)
        call.notFound(ErrorResponse(it, DefaultMessages.Error.USER_NOT_FOUND))
    }
    exception<Exception> {
        log.error(it)
        call.internalServerError(ErrorResponse(it, DefaultMessages.Error.SOMETHING_WRONG))
    }
}

internal fun CORS.Configuration.setUp() {
    anyHost()
    headers.addAll(listOf(HttpHeaders.Authorization, HttpHeaders.ContentType))
}