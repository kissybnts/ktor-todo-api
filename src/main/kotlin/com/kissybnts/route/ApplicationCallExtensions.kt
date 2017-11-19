package com.kissybnts.route

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

fun ApplicationCall.getResourceId(): Int? {
    return parameters[ResourceId.parameterName]?.toIntOrNull()
}

suspend fun ApplicationCall.ok() {
    respond(HttpStatusCode.OK)
}

suspend fun ApplicationCall.notFound() {
    respond(HttpStatusCode.NotFound)
}

suspend fun ApplicationCall.badRequest() {
    respond(HttpStatusCode.BadRequest)
}

suspend fun ApplicationCall.internalServerError() {
    respond(HttpStatusCode.InternalServerError)
}