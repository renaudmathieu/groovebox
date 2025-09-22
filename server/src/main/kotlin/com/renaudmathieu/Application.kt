package com.renaudmathieu

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File
import java.util.UUID

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    routing {

        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }


        get("/tracks") {
            val id = UUID.randomUUID().toString()
            val track = Track(
                id = id,
                title = "Sample Track",
                artist = "Unknown Artist",
                url = "/track/$id",
                duration = 187000
            )

            val tracks = listOf(
                track
            )
            call.respond(tracks)
        }

        get("/track/{id}") {
            val platform = call.request.queryParameters["platform"]
            val validPlatforms = setOf("android", "ios", "jvm")
            if (platform == null || platform !in validPlatforms) {
                call.respondText(
                    "Query parameter 'platform' must be one of ${validPlatforms.joinToString(", ")}",
                    status = HttpStatusCode.BadRequest
                )
                return@get
            }

            val resource = this.javaClass.classLoader.getResource("audio/sample.mp3")
            if (resource != null) {
                call.respondFile(File(resource.toURI()))
            } else {
                call.respondText("Track not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
