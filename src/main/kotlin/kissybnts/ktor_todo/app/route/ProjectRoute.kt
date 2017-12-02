package kissybnts.ktor_todo.app.route

import kissybnts.ktor_todo.app.request.CreateProjectRequest
import kissybnts.ktor_todo.app.service.ProjectService
import kissybnts.ktor_todo.extension.jwtUserSubject
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

// private modifier makes an exception when access to that class.
// kotlin.reflect.full.IllegalCallableAccessException: Class kotlin.reflect.jvm.internal.FunctionCaller$Constructor can not access a member of class kissybnts.Projects with modifiers "public"
@location("/projects") internal class Projects {
    @location("{projectId}") data class Id(val projectId: Int) {
        @location("/tasks") class Tasks(private val id: Id) {
            fun projectId(): Int = id.projectId
        }
    }
}

internal fun Route.projects(projectService: ProjectService = ProjectService()) {
    get<Projects> {
        val principal = call.jwtUserSubject()

        val all = projectService.selectAll(principal.id)
        call.respond(all)
    }

    post<Projects> {
        val principal = call.jwtUserSubject()
        val request = call.receive<CreateProjectRequest>()

        val project = projectService.create(request, principal.id)
        call.respond(project)
    }

    get<Projects.Id> {
        val principal = call.jwtUserSubject()

        val project = projectService.select(it.projectId, principal.id)
        call.respond(project)
    }

    get<Projects.Id.Tasks> {
        val principal = call.jwtUserSubject()

        val tasks = projectService.selectTasks(it.projectId(), principal.id)
        call.respond(tasks)
    }
}

