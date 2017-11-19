package com.kissybnts.route

import com.fasterxml.jackson.annotation.JsonFormat
import com.kissybnts.repository.NewTask
import com.kissybnts.repository.ProjectRepository
import com.kissybnts.repository.TaskRepository
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.location
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import org.joda.time.DateTime

data class NewProject(val name: String, val description: String)
data class NewTaskRequest(val name: String,
                          val description: String,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
                          val dueDate: DateTime)

fun Route.projects() {
    location<Index> {
        GET {
            handle {
                // TODO change to use the user id of which logged in user
                val all = ProjectRepository.selectAll(1)
                call.respond(all)
            }
        }

        POST {
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
        GET {
            handle {
                val id = call.getResourceId()
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

        route("/tasks") {
            GET {
                handle {
                    val id = call.getResourceId()
                    if (id == null) {
                        call.badRequest()
                        return@handle
                    }

                    val tasks = TaskRepository.selectAllBelongProject(id)
                    call.respond(tasks)
                }
            }

            POST {
                handle {
                    val id = call.getResourceId()
                    if (id == null) {
                        call.badRequest()
                        return@handle
                    }

                    val request = call.receive<NewTaskRequest>()
                    try {
                        val task = TaskRepository.insert(NewTask(id, request.name, request.description, request.dueDate))
                        call.respond(task)
                    } catch (ex: IllegalStateException) {
                        call.badRequest()
                    } catch (ex: Exception) {
                        call.internalServerError()
                    }
                }
            }
        }
    }

}

fun ApplicationCall.getResourceId(): Int? {
    return parameters[ResourceId.parameterName]?.toIntOrNull()
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