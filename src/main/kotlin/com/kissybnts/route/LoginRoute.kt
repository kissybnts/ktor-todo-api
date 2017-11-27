package com.kissybnts.route

import io.ktor.locations.location
import io.ktor.routing.Route

@location("/v1/auth/login/{type}") data class Login(val type: String, val code: String? = null, val state: String? = null)

fun Route.login() {
    location<Login> {

    }
}