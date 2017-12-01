package com.kissybnts.app.route

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.app.model.GitHubUser
import com.kissybnts.app.model.UserModel
import com.kissybnts.app.model.toCushioningUser
import com.kissybnts.app.repository.CushioningUser
import com.kissybnts.app.repository.UserRepository
import com.kissybnts.app.response.LoginResponse
import com.kissybnts.app.table.AuthProvider
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
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
import io.ktor.http.HttpStatusCode
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.locations.oauthAtLocation
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.param
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executors

@location("/v1/auth/login/{type}") data class Login(val type: String, val code: String? = null, val state: String? = null)

private val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)

private val secretkey = System.getenv(EnvironmentVariableKeys.JWT_SECRET_KEY)

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

fun Route.login(client: HttpClient) {
    location<Login> {
        authentication {
            oauthAtLocation<Login>(client, exec.asCoroutineDispatcher(), providerLookup = { loginProvider[it.type] }, urlProvider = { _, p -> redirectUrl(Login(p.name), false) })
        }

        param("error") {
            handle {
                call.respond(HttpStatusCode.BadRequest, call.parameters.getAll("error").orEmpty())
            }
        }

        handle {
            val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()?: throw IllegalStateException("Principal is null.")

            val type = call.parameters["type"] ?: throw IllegalStateException("Type is null.")
            val code = call.parameters["code"] ?: throw IllegalStateException("code is null.")

            val cushioningUser = try {
                client.acquireUser(type, principal.accessToken, code)
            } catch (ex: Exception) {
                println(ex.message)
                call.respond(HttpStatusCode.BadRequest)
                return@handle
            }

            val user = UserRepository.selectByProvider(AuthProvider.GitHub, cushioningUser.providerId)?.let {
                UserRepository.loginUpdate(it, code)
            } ?: UserRepository.insert(cushioningUser)

            val token = generateToken(user)

            call.respond(LoginResponse(user, token))
        }
    }
}

private suspend fun HttpClient.acquireUser(type: String, accessToken: String, code: String): CushioningUser {
    return when (type) {
        "github" -> {
            val githubUser = acquireGitHubUser(accessToken)
            githubUser.toCushioningUser(code)
        }
        else -> throw IllegalArgumentException("Un supported type: $type")
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

private fun generateToken(user: UserModel): String {
    val expiration = LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault())
    return Jwts.builder()
            .setSubject(user.id.toString())
            .setAudience("Ktor-todo")
            .signWith(SignatureAlgorithm.HS512, secretkey)
            .setExpiration(Date.from(expiration.toInstant()))
            .setHeaderParam("typ", "JWT")
            .compact()
}

private fun <T: Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val hostPort = request.host()!! + request.port().let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://$hostPort${application.locations.href(t)}"
}