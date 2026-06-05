package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.synapse.model.SynapseConfig
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.health() {
    val config = environment.config.getAs<SynapseConfig>()
    log.info("Starting up with Config: {}", config)
    routing {
        get("health") {
            call.respond("RUNNING")
        }
    }
}