package cloud.mallne.dicentra.areaassist.synapse.config

import cloud.mallne.dicentra.areaassist.synapse.routes.discovery
import cloud.mallne.dicentra.areaassist.synapse.routes.scope
import cloud.mallne.dicentra.areaassist.synapse.routes.user
import io.ktor.server.application.*

fun Application.routes() {
    discovery()
    scope()
    user()
}