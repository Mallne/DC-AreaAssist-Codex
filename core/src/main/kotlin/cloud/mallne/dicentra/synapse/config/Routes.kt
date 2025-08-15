package cloud.mallne.dicentra.synapse.config

import cloud.mallne.dicentra.synapse.routes.discovery
import cloud.mallne.dicentra.synapse.routes.scope
import cloud.mallne.dicentra.synapse.routes.user
import io.ktor.server.application.*

fun Application.routes() {
    discovery()
    scope()
    user()
}