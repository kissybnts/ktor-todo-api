package com.kissybnts

import com.kissybnts.route.projects
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.jetbrains.exposed.sql.Database
import java.text.DateFormat

fun Application.main() {
    val databaseConfig = environment.config.config("database")
    Database.setUp(databaseConfig)

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(Routing) {
        get("/") {
            call.respond(HttpStatusCode.OK, "Hello from Ktor!")
        }
        route("/projects") {
            projects()
        }
    }
}

/**
 * Try to get `ORG_GRADLE_PROJECT_***` environment variable.
 */
// TODO write test
fun gradleEnv(name: String, default: String): String = System.getenv(name) ?: default

/**
 * set up database config with `ApplicationConfig` and environment variables.
 */
private fun Database.Companion.setUp(databaseConfig: ApplicationConfig) {
    apply {
        val host = gradleEnv("DB_HOST", "127.0.0.1")
        val port = gradleEnv("DB_PORT", "3306")
        val name = gradleEnv("DB_NAME", "ktor-todo")
        val user = gradleEnv("DB_USER", "root")
        val password = gradleEnv("DB_PASS", "")
        val driver = databaseConfig.property("driver").getString()
        connect("jdbc:mysql://$host:$port/$name?useSSL=false", driver, user, password)
    }
}