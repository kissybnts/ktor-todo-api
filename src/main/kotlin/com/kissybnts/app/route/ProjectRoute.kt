package com.kissybnts.app.route

import com.kissybnts.extension.notFound
import com.kissybnts.app.repository.ProjectRepository
import com.kissybnts.app.repository.TaskRepository
import com.kissybnts.app.request.CreateProjectRequest
import com.kissybnts.exception.ResourceNotFoundException
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

// private modifier makes an exception when access to that class.
// kotlin.reflect.full.IllegalCallableAccessException: Class kotlin.reflect.jvm.internal.FunctionCaller$Constructor can not access a member of class com.kissybnts.app.route.Projects with modifiers "public"
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

        // TODO change to use the user id of which logged in user
        val project = ProjectRepository.insert(request, 1)
        call.respond(project)
    }

    get<Projects.Id> {
        // TODO change to use the user id of which logged in user
        val project = ProjectRepository.select(it.projectId, 1)
        if (project != null) {
            call.respond(project)
        } else {
            throw ResourceNotFoundException("Project of which id is ${it.projectId} is not found.")
        }
    }

    get<Projects.Id.Tasks> {
        // TODO change to use the user id of which logged in user
        val tasks = TaskRepository.selectAllBelongProject(it.projectId(),1)
        call.respond(tasks)
    }
}

