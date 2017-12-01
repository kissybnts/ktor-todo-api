package com.kissybnts.app.route

import com.kissybnts.extension.ok
import com.kissybnts.app.request.CreateTaskRequest
import com.kissybnts.app.request.UpdateTaskRequest
import com.kissybnts.app.service.TaskService
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

@location("/tasks") internal class Tasks {
    @location("/{taskId}") data class Id(val taskId: Int) {
        @location("/complete") data class Complete(private val id: Id) {
            val taskId: Int = id.taskId
        }
    }
}

internal fun Route.tasks() {
    val taskService = TaskService()

    get<Tasks> {
        // TODO change to use the user id of which logged in user
        val all = taskService.selectAll(1)
        call.respond(all)
    }

    post<Tasks> {
        val request = call.receive<CreateTaskRequest>()

        // TODO change to use the user id of which logged in user
        val task = taskService.create(request, 1)
        call.respond(task)
    }

    patch<Tasks.Id> {
        val request = call.receive<UpdateTaskRequest>()

        // TODO change to use the user id of which logged in user
        taskService.update(it.taskId, request, 1)
        call.ok()
    }

    patch<Tasks.Id.Complete> {
        // TODO change to use the user id of which logged in user
        taskService.complete(it.taskId, 1)
        call.ok()
    }
}