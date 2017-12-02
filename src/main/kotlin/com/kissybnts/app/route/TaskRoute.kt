package com.kissybnts.app.route

import com.kissybnts.extension.ok
import com.kissybnts.app.request.CreateTaskRequest
import com.kissybnts.app.request.UpdateTaskRequest
import com.kissybnts.app.service.TaskService
import com.kissybnts.extension.jwtUserSubject
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

internal fun Route.tasks(taskService: TaskService = TaskService()) {
    get<Tasks> {
        val principal = call.jwtUserSubject()

        val all = taskService.selectAll(principal.id)
        call.respond(all)
    }

    post<Tasks> {
        val principal = call.jwtUserSubject()
        val request = call.receive<CreateTaskRequest>()

        val task = taskService.create(request, principal.id)
        call.respond(task)
    }

    patch<Tasks.Id> {
        val principal = call.jwtUserSubject()
        val request = call.receive<UpdateTaskRequest>()

        taskService.update(it.taskId, request, principal.id)
        call.ok()
    }

    patch<Tasks.Id.Complete> {
        val principal = call.jwtUserSubject()

        taskService.complete(it.taskId, principal.id)
        call.ok()
    }
}