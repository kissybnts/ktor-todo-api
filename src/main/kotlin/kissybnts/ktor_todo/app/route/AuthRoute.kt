package kissybnts.ktor_todo.app.route

import kissybnts.ktor_todo.app.AuthConstants.loginProvider
import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.response.LoginResponse
import kissybnts.ktor_todo.app.service.JwtService
import kissybnts.ktor_todo.app.service.TokenType
import kissybnts.ktor_todo.app.service.UserService
import kissybnts.ktor_todo.exception.ProviderAuthenticationErrorException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.locations.location
import io.ktor.locations.locations
import io.ktor.locations.oauthAtLocation
import io.ktor.locations.post
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.method
import io.ktor.routing.param
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import java.util.concurrent.Executors

@location("/oauth/login/{type}") data class OAuthLogin(val type: String, val code: String? = null, val state: String? = null)

@location("/auth") class Auth {
    @location("/login") class Login
    @location("/sign-in") class SignIn
}

fun Route.auth(client: HttpClient, jwtService: JwtService = JwtService(), userService: UserService = UserService()) {
    val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)

    location<Auth.Login> {
        method(HttpMethod.Post) {
            authentication {

            }

            handle {
                call.respond("OK")
            }
        }
    }

    post<Auth.SignIn> {
        call.respond("OK")
    }

    location<OAuthLogin> {
        authentication {
            oauthAtLocation<OAuthLogin>(client, exec.asCoroutineDispatcher(), providerLookup = { loginProvider[it.type] }, urlProvider = { _, p -> redirectUrl(OAuthLogin(p.name), false) })
        }

        param("error") {
            handle {
                val type = call.parameters["type"]?: throw IllegalStateException("OAuthLogin type is null.")
                throw ProviderAuthenticationErrorException(call.parameters.getAll("error")?.joinToString(",", prefix = "OAuthLogin with $type has been failed: ") ?: DefaultMessages.Error.AUTH_PROCESS_FAILED)
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
    val type = parameters["type"]?: throw IllegalStateException("OAuthLogin type is not specified.")
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