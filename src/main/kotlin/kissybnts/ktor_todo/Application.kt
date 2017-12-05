package kissybnts.ktor_todo

import kissybnts.ktor_todo.app.pipeline.jwtAuthentication
import kissybnts.ktor_todo.extension.jacksonSetUp
import kissybnts.ktor_todo.extension.setUp
import kissybnts.ktor_todo.app.service.JwtService
import io.ktor.application.*
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import kissybnts.ktor_todo.app.route.*
import org.jetbrains.exposed.sql.Database

fun Application.main() {
    val databaseConfig = environment.config.config("database")
    Database.setUp(databaseConfig)

    install(DefaultHeaders)
    install(HSTS)
    install(CORS) { setUp() }
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        jacksonSetUp()
    }
    install(StatusPages) { setUp(log) }

    install(Routing) {
        val jwtService = JwtService()

        get<Index> {
            call.respond(HttpStatusCode.OK, "Hello from Ktor!")
        }

        val client = HttpClient(ApacheBackend)
        environment.monitor.subscribe(ApplicationStopping) {
            client.close()
        }

        auth(client, jwtService)

        route("/v1") {
            authentication {
                jwtAuthentication(jwtService)
            }
            projects()
            tasks()
        }
    }
}

fun getEnv(name: String, default: String): String = System.getenv(name)?: default