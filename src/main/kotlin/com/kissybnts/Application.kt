package com.kissybnts

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.kissybnts.app.EnvironmentVariableKeys
import com.kissybnts.route.Index
import com.kissybnts.extension.ok
import com.kissybnts.route.login
import com.kissybnts.route.projects
import com.kissybnts.route.tasks
import io.ktor.application.*
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.config.ApplicationConfig
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.locations.get
import org.jetbrains.exposed.sql.Database

fun Application.main() {
    val databaseConfig = environment.config.config("database")
    Database.setUp(databaseConfig)

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JodaModule())
        }
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

/**
 * set up database config with `ApplicationConfig` and environment variables.
 */
private fun Database.Companion.setUp(databaseConfig: ApplicationConfig) {
    apply {
        val host = getEnv(EnvironmentVariableKeys.Gradle.DB_HOST, "127.0.0.1")
        val port = getEnv(EnvironmentVariableKeys.Gradle.DB_PORT, "3306")
        val name = getEnv(EnvironmentVariableKeys.Gradle.DB_NAME, "ktor-todo")
        val user = getEnv(EnvironmentVariableKeys.Gradle.DB_USER, "root")
        val password = getEnv(EnvironmentVariableKeys.Gradle.DB_PASS, "")
        val driver = databaseConfig.property("driver").getString()
        connect("jdbc:mysql://$host:$port/$name?useSSL=false", driver, user, password)
    }
}