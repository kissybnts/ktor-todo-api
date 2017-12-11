package kissybnts.ktor_todo.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.auth.OAuthServerSettings

val objectMapper: ObjectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

object EnvironmentVariableKeys {
    // DB
    const val DB_HOST: String = "DB_HOST"
    const val DB_PORT: String = "DB_PORT"
    const val DB_NAME: String = "DB_NAME"
    const val DB_USER: String = "DB_USER"
    const val DB_PASS: String = "DB_PASS"
    // JWT
    const val JWT_SECRET_KEY: String = "JWT_SECRET_KEY"
    const val JWT_EXPIRE_TIME: String = "JWT_EXPIRE_TIME"
    const val JWT_REFRESH_SECRET_KEY: String = "JWT_REFRESH_SECRET_KEY"
    const val JWT_REFRESH_EXPIRE_TIME: String = "JWT_REFRESH_EXPIRE_TIME"

    // GitHub
    const val GITHUB_CLIENT_ID: String = "GITHUB_CLIENT_ID"
    const val GITHUB_CLIENT_SECRET: String = "GITHUB_CLIENT_SECRET"

    // Password encryption
    const val PASSWORD_CRYPT_KEY: String = "PASSWORD_CRYPT_KEY"
}

object FormatConstants {
    const val DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss"
    const val DATE_FORMAT = "yyyy/MM/dd"
}

object DefaultMessages {
    object Error {
        const val BAD_REQUEST = "Bad request."
        const val SOMETHING_WRONG = "Something wrong."
        const val RESOURCE_NOT_FOUND = "Specified resource has not been found."
        const val AUTH_PROCESS_FAILED = "Authentication process has been failed."
        const val INVALID_CREDENTIAL = "Invalid credential."
        const val USER_NOT_FOUND = "User not found."
        fun resourceNotFound(name: String, id: Int) = "$name of which id is $id has not been found."
    }
}

object JwtConstants {
    object Header {
        const val TYPE_KEY = "typ"
        const val TYPE = "JWT"
    }

    object Body {
        const val AUDIENCE = "Ktor-todo"
    }

    const val AUTH_HEADER_SCHEMA = "Bearer"
}

object AuthConstants {
    val loginProvider: Map<String, OAuthServerSettings.OAuth2ServerSettings> = listOf(
            OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    clientId = System.getenv(EnvironmentVariableKeys.GITHUB_CLIENT_ID),
                    defaultScopes = listOf("read:user"),
                    clientSecret = System.getenv(EnvironmentVariableKeys.GITHUB_CLIENT_SECRET)
            )
    ).associateBy { it.name }
}