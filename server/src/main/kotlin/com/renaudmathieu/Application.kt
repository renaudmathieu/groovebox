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

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

data class TrackRef(
    val id: String,
    val title: String,
    val fileName: String
)

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    val albumTracks: List<Pair<String, String>> = listOf(
        "Daftendirekt" to "Daftendirekt.mp3",
        "WDPK 83.7 FM" to "WDPK83_7_FM.mp3",
        "Revolution 909" to "Revolution_909.mp3",
        "Da Funk" to "Da_Funk.mp3",
        "Phoenix" to "Phoenix.mp3",
        "Fresh" to "Fresh.mp3",
        "Around the World" to "Around_The_World.mp3",
        "Rollin’ & Scratchin’" to "Rollin_Scratchin.mp3",
        "Teachers" to "Teachers.mp3",
        "High Fidelity" to "High_Fidelity.mp3",
        "Rock’n Roll" to "Rockn_Roll.mp3",
        "Oh Yeah" to "Oh_Yeah.mp3",
        "Burnin’" to "Burnin.mp3",
        "Indo Silver Club" to "Indo_Silver_Club.mp3",
        "Alive" to "Alive.mp3",
        "Funk Ad" to "Funk_Ad.mp3",
    )

    val trackRefs: List<TrackRef> = albumTracks.map { (title, file) ->
        val id = file.substringBeforeLast('.')
        TrackRef(id = id, title = title, fileName = file)
    }
    val trackById: Map<String, TrackRef> = trackRefs.associateBy { it.id }

    routing {

        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/tracks") {
            val tracks = trackRefs.map { ref ->
                Track(
                    id = ref.id,
                    title = ref.title,
                    artist = "Daft Punk",
                    url = "/track/${ref.id}",
                    duration = 0L
                )
            }
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

            val id = call.parameters["id"] ?: run {
                call.respondText("Track id required", status = HttpStatusCode.BadRequest)
                return@get
            }

            val ref = trackById[id]
            if (ref == null) {
                call.respondText("Track not found", status = HttpStatusCode.NotFound)
                return@get
            }

            val resource = this.javaClass.classLoader.getResource("audio/${ref.fileName}")
            if (resource != null) {
                call.respondFile(File(resource.toURI()))
            } else {
                call.respondText("Track file not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
