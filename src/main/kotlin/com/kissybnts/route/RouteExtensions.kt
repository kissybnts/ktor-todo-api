package com.kissybnts.route

import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import io.ktor.routing.method

fun Route.GET(body: Route.() -> Unit) {
    method(HttpMethod.Get, body)
}

fun Route.POST(body: Route.() -> Unit) {
    method(HttpMethod.Post, body)
}

fun Route.PATCH(body: Route.() -> Unit) {
    method(HttpMethod.Patch, body)
}

fun Route.DELETE(body: Route.() -> Unit) {
    method(HttpMethod.Delete, body)
}