package kissybnts.ktor_todo.app.pipeline

import kissybnts.ktor_todo.app.JwtConstants
import kissybnts.ktor_todo.app.service.JwtService
import kissybnts.ktor_todo.app.enumeration.TokenType
import kissybnts.ktor_todo.exception.InvalidCredentialException
import io.ktor.application.call
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.parseAuthorizationHeader

fun AuthenticationPipeline.jwtAuthentication(jwtService: JwtService = JwtService()) {
    intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val authHeader = call.request.parseAuthorizationHeader()?: throw InvalidCredentialException()
        val user = if (authHeader.authScheme == JwtConstants.AUTH_HEADER_SCHEMA && authHeader is HttpAuthHeader.Single) {
            jwtService.verifyToken(authHeader.blob, TokenType.ACCESS_TOKEN)
        } else {
            throw InvalidCredentialException()
        }
        context.principal(user)
    }
}
