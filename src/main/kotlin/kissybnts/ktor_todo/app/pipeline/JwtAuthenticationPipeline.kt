package kissybnts.ktor_todo.app.pipeline

import io.ktor.application.ApplicationCall
import kissybnts.ktor_todo.app.JwtConstants
import kissybnts.ktor_todo.app.service.JwtService
import kissybnts.ktor_todo.app.enumeration.TokenType
import kissybnts.ktor_todo.exception.InvalidCredentialException
import io.ktor.application.call
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.pipeline.PipelineContext
import kissybnts.ktor_todo.app.service.JwtUserSubject

fun AuthenticationPipeline.jwtAuthentication(jwtService: JwtService = JwtService()) {
    intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val user = verifyToken(jwtService, TokenType.ACCESS_TOKEN)
        context.principal(user)
    }
}

fun AuthenticationPipeline.jwtRefreshAuthentication(jwtService: JwtService = JwtService()) {
    intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val user = verifyToken(jwtService, TokenType.REFRESH_TOKEN)
        context.principal(user)
    }
}

private fun PipelineContext<*, ApplicationCall>.verifyToken(jwtService: JwtService, tokenType: TokenType): JwtUserSubject {
    val authHeader = call.request.parseAuthorizationHeader()?: throw InvalidCredentialException()
    return if (authHeader.authScheme == JwtConstants.AUTH_HEADER_SCHEMA && authHeader is HttpAuthHeader.Single) {
        jwtService.verifyToken(authHeader.blob, tokenType)
    } else {
        throw InvalidCredentialException()
    }
}