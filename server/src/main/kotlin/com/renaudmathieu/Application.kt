package com.renaudmathieu

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
    routing {
        // Root for tests and simple health check
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // 1) Return a list of tracks (UUID, title, artist, duration)
        get("/tracks") {
            val id = UUID.randomUUID().toString()
            val track = Track(
                id = id,
                title = "Sample Track",
                artist = "Unknown Artist",
                url = "/track/$id",
                duration = 187000
            )
            val tracksJson = """[{"id":"${track.id}","title":"${track.title}","artist":"${track.artist}","duration":${track.duration}}]"""
            call.respondText(tracksJson, contentType = ContentType.Application.Json)
        }

        // 2) Serve a track file by id and platform query param
        // Usage: GET /track/{id}?platform=android|ios|jvm
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
