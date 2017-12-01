package com.kissybnts

import com.kissybnts.extension.jacksonSetUp
import com.kissybnts.extension.ok
import com.kissybnts.extension.setUp
import com.kissybnts.route.Index
import com.kissybnts.route.login
import com.kissybnts.route.projects
import com.kissybnts.route.tasks
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
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

    install(Routing) {
        get<Index> {
            call.respond(HttpStatusCode.OK, "Hello from Ktor!")
        }
        route("/intercept"){
            intercept(ApplicationCallPipeline.Infrastructure) {
                println("Infrastructure, Intercepted!")
            }
            intercept(ApplicationCallPipeline.Call) {
                println("Call, Intercepted!")
            }
            intercept(ApplicationCallPipeline.Fallback) {
                println("Fallback, Intercepted!")
            }
            get<Index> {
                println("get!")
                call.ok()
            }
        }

        val client = HttpClient(ApacheBackend)
        environment.monitor.subscribe(ApplicationStopping) {
            client.close()
        }

        login(client)

        route("/v1") {
            projects()
            tasks()
        }
    }
}

fun getEnv(name: String, default: String): String = System.getenv(name)?: default