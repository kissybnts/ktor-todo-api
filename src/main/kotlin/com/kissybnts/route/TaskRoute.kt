package com.kissybnts.route

import com.kissybnts.repository.TaskRepository
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.locations.location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.method

fun Route.tasks() {
    location<Index> {
        GET {
            handle {
                // TODO change to use the user id of which logged in user
                val all = TaskRepository.selectAll(1)
                call.respond(all)
            }
        }
    }
}

fun Route.GET(body: Route.() -> Unit) {
    method(HttpMethod.Get, body)
}