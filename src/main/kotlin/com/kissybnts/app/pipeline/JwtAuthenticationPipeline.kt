package com.kissybnts.app.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kissybnts.exception.BadCredentialException
import io.ktor.application.call
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.parseAuthorizationHeader

fun AuthenticationPipeline.jwtAuthentication(realm: String) {
    intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val authHeader = call.request.parseAuthorizationHeader()?: throw badCredentialException()

    }
}

private fun badCredentialException() = BadCredentialException("Invalid credential.")

val objectMapper: ObjectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)