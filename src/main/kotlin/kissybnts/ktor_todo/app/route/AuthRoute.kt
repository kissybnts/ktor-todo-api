package kissybnts.ktor_todo.app.route

import kissybnts.ktor_todo.app.AuthConstants.loginProvider
import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.response.LoginResponse
import kissybnts.ktor_todo.app.service.JwtService
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
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.method
import io.ktor.routing.param
import kissybnts.ktor_todo.app.pipeline.jwtRefreshAuthentication
import kissybnts.ktor_todo.app.request.LoginRequest
import kissybnts.ktor_todo.app.request.SignUpRequest
import kissybnts.ktor_todo.extension.jwtUserSubject
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import java.util.concurrent.Executors

@location("/oauth/login/{type}") data class OAuthLogin(val type: String, val code: String? = null, val state: String? = null)

@location("/auth") class Auth {
    @location("/login") class Login
    @location("/sign-up") class SignUp
    @location("/refresh") class Refresh
}

fun Route.auth(client: HttpClient, jwtService: JwtService = JwtService(), userService: UserService = UserService()) {
    val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)

    post<Auth.SignUp> {
        val signUpRequest = call.receive<SignUpRequest>()
        val user = userService.signUpWithEmail(signUpRequest)
        val tokenPair = jwtService.generateTokenPair(user)
        call.respond(LoginResponse(user, tokenPair.accessToken, tokenPair.refreshToken))
    }

    post<Auth.Login> {
        val request = call.receive<LoginRequest>()
        val user = userService.loginWithEmail(request)
        val tokenPair = jwtService.generateTokenPair(user)
        call.respond(LoginResponse(user, tokenPair.accessToken, tokenPair.refreshToken))
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

            val tokenPair = jwtService.generateTokenPair(user)

            call.respond(LoginResponse(user, tokenPair.accessToken, tokenPair.refreshToken))
        }
    }

    location<Auth.Refresh> {
        method(HttpMethod.Post) {
            authentication {
                jwtRefreshAuthentication(jwtService)
            }
            handle {
                val userSubject = call.jwtUserSubject()
                val user = userService.selectById(userSubject.id)
                val tokenPair = jwtService.generateTokenPair(user)
                call.respond(tokenPair)
            }
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