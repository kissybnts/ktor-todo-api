package com.kissybnts.route

import com.kissybnts.repository.ProjectRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

data class NewProject(val name: String, val description: String)

fun Route.projects() {
    get<Index> {
        // TODO change to use the user id of which logged in user
        val all = ProjectRepository.selectAll(1)
        call.respond(all)
    }

    post {
        val request = call.receiveOrNull<NewProject>()
        if (request == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val project = ProjectRepository.insert(request)
        call.respond(HttpStatusCode.OK, project)
    }

    get<ResourceId> {
        val project = ProjectRepository.select(it.id)
        if (project != null) {
            call.respond(project)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}