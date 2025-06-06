package com.renaudmathieu

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/track") {
            val resource = this.javaClass.classLoader.getResource("audio/sample.mp3")
            if (resource != null) {
                call.respondFile(File(resource.toURI()))
            } else {
                call.respondText("Track not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
