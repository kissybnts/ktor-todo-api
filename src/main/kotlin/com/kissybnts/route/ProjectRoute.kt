package com.kissybnts.route

import com.kissybnts.extension.badRequest
import com.kissybnts.extension.internalServerError
import com.kissybnts.extension.notFound
import com.kissybnts.repository.ProjectRepository
import com.kissybnts.repository.TaskRepository
import com.kissybnts.request.CreateProjectRequest
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

// private modifier makes an exception when access to that class.
// kotlin.reflect.full.IllegalCallableAccessException: Class kotlin.reflect.jvm.internal.FunctionCaller$Constructor can not access a member of class com.kissybnts.route.Projects with modifiers "public"
@location("/projects") internal class Projects {
    @location("{projectId}") data class Id(val projectId: Int) {
        @location("/tasks") class Tasks(private val id: Id) {
            fun projectId(): Int = id.projectId
        }
    }
}

internal fun Route.projects() {

    get<Projects> {
        // TODO change to use the user id of which logged in user
        val all = ProjectRepository.selectAll(1)
        call.respond(all)
    }

    post<Projects> {
        val request = call.receive<CreateProjectRequest>()
        try {
            val project = ProjectRepository.insert(request)
            call.respond(project)
        } catch (ex: IllegalStateException) {
            call.badRequest()
        } catch (ex: Exception) {
            call.internalServerError()
        }
    }

    get<Projects.Id> {
        val project = ProjectRepository.select(it.projectId)
        if (project != null) {
            call.respond(project)
        } else {
            call.notFound()
        }
    }

    get<Projects.Id.Tasks> {
        val tasks = TaskRepository.selectAllBelongProject(it.projectId())
        call.respond(tasks)
    }
}

