package com.kissybnts.route

import com.kissybnts.extension.ok
import com.kissybnts.repository.TaskRepository
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.patch
import io.ktor.response.respond
import io.ktor.routing.Route

@location("/tasks") internal class Tasks {
    @location("/{taskId}") data class Id(val taskId: Int) {
        @location("/complete") data class Complete(val id: Id) {
            val taskId: Int = id.taskId
        }
    }
}

internal fun Route.tasks() {

    get<Tasks> {
        // TODO change to use the user id of which logged in user
        val all = TaskRepository.selectAll(1)
        call.respond(all)
    }

    patch<Tasks.Id.Complete> {
        TaskRepository.complete(it.taskId)
        call.ok()
    }
}