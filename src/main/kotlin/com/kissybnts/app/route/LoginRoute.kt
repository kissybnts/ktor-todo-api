package com.kissybnts.app.route

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kissybnts.app.DefaultMessages
import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.app.enumeration.AuthProvider
import com.kissybnts.app.model.GitHubUser
import com.kissybnts.app.model.toCushioningUser
import com.kissybnts.app.repository.CushioningUser
import com.kissybnts.app.repository.UserRepository
import com.kissybnts.app.response.LoginResponse
import com.kissybnts.app.service.JwtService
import com.kissybnts.app.service.TokenType
import com.kissybnts.exception.ProviderAuthenticationErrorException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.bodyStream
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.utils.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.locations.oauthAtLocation
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.param
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import java.util.concurrent.Executors

@location("/auth/login/{type}") data class Login(val type: String, val code: String? = null, val state: String? = null)

private val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)

private val loginProvider = listOf(
        OAuthServerSettings.OAuth2ServerSettings(
                name = "github",
                authorizeUrl = "https://github.com/login/oauth/authorize",
                accessTokenUrl = "https://github.com/login/oauth/access_token",
                clientId = System.getenv(EnvironmentVariableKeys.GITHUB_CLIENT_ID),
                defaultScopes = listOf("read:user"),
                clientSecret = System.getenv(EnvironmentVariableKeys.GITHUB_CLIENT_SECRET)
        )
).associateBy { it.name }

fun Route.login(client: HttpClient, jwtService: JwtService = JwtService()) {

    location<Login> {
        authentication {
            oauthAtLocation<Login>(client, exec.asCoroutineDispatcher(), providerLookup = { loginProvider[it.type] }, urlProvider = { _, p -> redirectUrl(Login(p.name), false) })
        }

        param("error") {
            handle {
                val type = call.parameters["type"]?: throw IllegalStateException("Login type is null.")
                throw ProviderAuthenticationErrorException(call.parameters.getAll("error")?.joinToString(",", prefix = "Login with $type has been failed: ")?: DefaultMessages.Error.RESOURCE_NOT_FOUND)
            }
        }

        handle {
            val loginType = call.loginType()

            val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()?: throw IllegalStateException("Principal is null.")

            val code = call.parameters["code"] ?: throw IllegalStateException("Code is null.")

            val cushioningUser = client.acquireUser(loginType, principal.accessToken, code)

            val user = UserRepository.selectByProvider(AuthProvider.GitHub, cushioningUser.providerId)?.let {
                UserRepository.loginUpdate(it, code)
            } ?: UserRepository.insert(cushioningUser)

            val token = jwtService.generateToken(user, TokenType.ACCESS_TOKEN)
            val refresh = jwtService.generateToken(user, TokenType.REFRESH_TOKEN)

            call.respond(LoginResponse(user, token, refresh))
        }
    }
}

private suspend fun ApplicationCall.loginType(): AuthProvider {
    val type = parameters["type"]?: throw IllegalStateException("Login type is not specified.")
    return AuthProvider.fromName(type)
}

private suspend fun HttpClient.acquireUser(type: AuthProvider, accessToken: String, code: String): CushioningUser {
    return when (type) {
        AuthProvider.GitHub -> {
            val githubUser = acquireGitHubUser(accessToken)
            githubUser.toCushioningUser(code)
        }
    }
}

private suspend fun HttpClient.acquireGitHubUser(accessToken: String): GitHubUser {
    val response = call {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        url("https", "api.github.com", 443, "user")
        method = HttpMethod.Get
    }
    return jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE).readValue(response.bodyStream.reader(Charsets.UTF_8), GitHubUser::class.java)
}

private fun <T: Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val hostPort = request.host()!! + request.port().let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://$hostPort${application.locations.href(t)}"
}