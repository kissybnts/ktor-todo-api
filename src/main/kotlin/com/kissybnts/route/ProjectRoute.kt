package com.kissybnts.route

import com.kissybnts.repository.ProjectRepository
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.location
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.method

data class NewProject(val name: String, val description: String)

fun Route.projects() {
    location<Index> {
        method(HttpMethod.Get) {
            handle {
                // TODO change to use the user id of which logged in user
                val all = ProjectRepository.selectAll(1)
                call.respond(all)
            }
        }

        method(HttpMethod.Post) {
            handle {
                val request = call.receive<NewProject>()
                try {
                    val project = ProjectRepository.insert(request)
                    call.respond(project)
                } catch (ex: IllegalStateException) {
                    call.badRequest()
                } catch (ex: Exception) {
                    call.internalServerError()
                }

            }
        }
    }

    location<ResourceId> {
        method(HttpMethod.Get) {
            handle {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.badRequest()
                    return@handle
                }

                val project = ProjectRepository.select(id)
                if (project != null) {
                    call.respond(project)
                } else {
                    call.notFound()
                }

            }
        }
    }

}

suspend fun ApplicationCall.notFound() {
    respond(HttpStatusCode.NotFound)
}

suspend fun ApplicationCall.badRequest() {
    respond(HttpStatusCode.BadRequest)
}

suspend fun ApplicationCall.internalServerError() {
    respond(HttpStatusCode.InternalServerError)
}