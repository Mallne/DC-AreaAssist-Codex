package cloud.mallne.dicentra.areaassist.synapse.config

import cloud.mallne.dicentra.areaassist.synapse.routes.discovery
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routes() {
    routing {
        discovery()
    }
}