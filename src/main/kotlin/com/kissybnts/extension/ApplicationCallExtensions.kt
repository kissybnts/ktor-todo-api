package com.kissybnts.extension

import com.kissybnts.app.response.ErrorResponse
import com.kissybnts.app.service.JwtUserSubject
import io.ktor.application.ApplicationCall
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

suspend fun ApplicationCall.ok() {
    respond(HttpStatusCode.OK)
}

suspend fun ApplicationCall.notFound(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.NotFound, errorResponse)
}

suspend fun ApplicationCall.unauthorized(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.Unauthorized, errorResponse)
}

suspend fun ApplicationCall.badRequest(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.BadRequest, errorResponse)
}

suspend fun ApplicationCall.internalServerError(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.InternalServerError, errorResponse)
}

suspend fun ApplicationCall.jwtUserSubject(): JwtUserSubject = principal() ?: throw IllegalStateException("Request has not been passed the authentication process.")