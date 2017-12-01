package com.kissybnts.app.route

import com.kissybnts.extension.badRequest
import com.kissybnts.extension.internalServerError
import com.kissybnts.extension.ok
import com.kissybnts.app.repository.TaskRepository
import com.kissybnts.app.request.CreateTaskRequest
import com.kissybnts.app.request.UpdateTaskRequest
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

    get<Tasks> {
        // TODO change to use the user id of which logged in user
        val all = TaskRepository.selectAll(1)
        call.respond(all)
    }

    post<Tasks> {
        val request = call.receive<CreateTaskRequest>()
        try {
            val task = TaskRepository.insert(request)
            call.respond(task)
        } catch (ex: IllegalStateException) {
            println(ex.message)
            call.badRequest()
        }
//        } catch (ex: Exception) {
//            println(ex.message)
//            call.internalServerError()
//        }
    }

    patch<Tasks.Id> {
        val request = call.receive<UpdateTaskRequest>()
        try {
            TaskRepository.update(it.taskId, request)
            call.ok()
        } catch (ex: IllegalStateException) {
            call.badRequest()
        } catch (ex: Exception) {
            call.internalServerError()
        }
    }

    patch<Tasks.Id.Complete> {
        TaskRepository.complete(it.taskId)
        call.ok()
    }
}