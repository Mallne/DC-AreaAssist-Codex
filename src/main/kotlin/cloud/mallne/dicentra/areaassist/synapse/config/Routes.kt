package cloud.mallne.dicentra.areaassist.synapse.config

import cloud.mallne.dicentra.areaassist.synapse.routes.discovery
import cloud.mallne.dicentra.areaassist.synapse.routes.scope
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routes() {
    routing {
        discovery()
        scope()
    }
}