package com.kissybnts.route

import com.kissybnts.repository.TaskRepository
import io.ktor.application.call
import io.ktor.locations.location
import io.ktor.response.respond
import io.ktor.routing.Route

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