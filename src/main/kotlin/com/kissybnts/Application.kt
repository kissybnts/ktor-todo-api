package com.kissybnts

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.DateFormat

fun Application.main() {
    Database.apply {
        val host = gradleEnv("DB_HOST", "127.0.0.1")
        val port = gradleEnv("DB_PORT", "3306")
        val name = gradleEnv("DB_NAME", "ktor-todo")
        val user = gradleEnv("DB_USER", "root")
        val password = gradleEnv("DB_PASS", "")
        val driver = environment.config.config("database").property("driver").getString()
        connect("jdbc:mysql://$host:$port/$name?useSSL=false", driver, user, password)
    }
    install(DefaultHeaders)
    install(CallLogging)
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
        get("/items") {
            call.respond(HttpStatusCode.OK, Item("This is a key", "This is a value"))
        }
    }
}

data class Item(val key: String, val value: String)

private fun gradleEnv(name: String, default: String): String = System.getenv(name)?: default