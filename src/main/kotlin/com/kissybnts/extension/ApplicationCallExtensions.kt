package com.kissybnts.extension

import com.kissybnts.app.response.ErrorResponse
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

suspend fun ApplicationCall.ok() {
    respond(HttpStatusCode.OK)
}

suspend fun ApplicationCall.notFound(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.NotFound, errorResponse)
}

suspend fun ApplicationCall.badRequest(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.BadRequest, errorResponse)
}

suspend fun ApplicationCall.internalServerError(errorResponse: ErrorResponse) {
    respond(HttpStatusCode.InternalServerError, errorResponse)
}