package com.kissybnts

import com.kissybnts.app.pipeline.jwtAuthentication
import com.kissybnts.extension.jacksonSetUp
import com.kissybnts.extension.setUp
import com.kissybnts.app.route.Index
import com.kissybnts.app.route.login
import com.kissybnts.app.route.projects
import com.kissybnts.app.route.tasks
import io.ktor.application.*
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import org.jetbrains.exposed.sql.Database

fun Application.main() {
    val databaseConfig = environment.config.config("database")
    Database.setUp(databaseConfig)

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        jacksonSetUp()
    }
    install(StatusPages) {
        setUp(log)
    }

    install(Routing) {
        get<Index> {
            call.respond(HttpStatusCode.OK, "Hello from Ktor!")
        }

        val client = HttpClient(ApacheBackend)
        environment.monitor.subscribe(ApplicationStopping) {
            client.close()
        }

        login(client)

        route("/v1") {
            authentication {
                jwtAuthentication()
            }
            projects()
            tasks()
        }
    }
}

fun getEnv(name: String, default: String): String = System.getenv(name)?: default