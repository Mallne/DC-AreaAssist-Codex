package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.synapse.model.Configuration
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.health() {
    val config by inject<Configuration>()
    log.info("Starting up with Config: {}", config)
    routing {
        get("health") {
            call.respond("RUNNING")
        }
    }
}