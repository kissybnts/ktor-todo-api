package kissybnts.ktor_todo.app.route

import kissybnts.ktor_todo.extension.ok
import kissybnts.ktor_todo.app.request.CreateTaskRequest
import kissybnts.ktor_todo.app.request.UpdateTaskRequest
import kissybnts.ktor_todo.app.service.TaskService
import kissybnts.ktor_todo.extension.jwtUserSubject
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.patch
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import kissybnts.ktor_todo.app.service.ProjectService

@location("/tasks") internal class Tasks {
    @location("/{taskId}") data class Id(val taskId: Int) {
        @location("/complete") data class Complete(private val id: Id) {
            val taskId: Int = id.taskId
        }
    }
}

internal fun Route.tasks(taskService: TaskService = TaskService(),
                         projectService: ProjectService = ProjectService()) {
    get<Tasks> {
        val principal = call.jwtUserSubject()

        val all = taskService.selectAll(principal.id)
        call.respond(all)
    }

    post<Tasks> {
        val principal = call.jwtUserSubject()
        val request = call.receive<CreateTaskRequest>()
        projectService.checkIsUsersProject(request.projectId, principal.id)

        val task = taskService.create(request)
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