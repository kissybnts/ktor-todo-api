package com.kissybnts.app.route

import com.kissybnts.app.AuthConstants.loginProvider
import com.kissybnts.app.DefaultMessages
import com.kissybnts.app.enumeration.AuthProvider
import com.kissybnts.app.response.LoginResponse
import com.kissybnts.app.service.JwtService
import com.kissybnts.app.service.TokenType
import com.kissybnts.app.service.UserService
import com.kissybnts.exception.ProviderAuthenticationErrorException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
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

fun Route.login(client: HttpClient, jwtService: JwtService = JwtService(), userService: UserService = UserService()) {
    val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)

    location<Login> {
        authentication {
            oauthAtLocation<Login>(client, exec.asCoroutineDispatcher(), providerLookup = { loginProvider[it.type] }, urlProvider = { _, p -> redirectUrl(Login(p.name), false) })
        }

        param("error") {
            handle {
                val type = call.parameters["type"]?: throw IllegalStateException("Login type is null.")
                throw ProviderAuthenticationErrorException(call.parameters.getAll("error")?.joinToString(",", prefix = "Login with $type has been failed: ")?: DefaultMessages.Error.AUTH_PROCESS_FAILED)
            }
        }

        handle {
            val loginType = call.loginType()
            val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()?: throw IllegalStateException("Principal is null.")
            val code = call.parameters["code"] ?: throw IllegalStateException("Code is null.")

            val user = userService.loginWithProvider(loginType, principal.accessToken, code)

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

private fun <T: Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val hostPort = request.host()!! + request.port().let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://$hostPort${application.locations.href(t)}"
}