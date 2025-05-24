package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.statics.APIService
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.discovery() {
    authenticate(optional = true) {
        get("/") {
            call.authentication.principal()
            call.respondText("true")
        }
    }
    authenticate(optional = false) {
        get("/baked") {
            val user: User? = call.authentication.principal()
            val discoveryResponse = DiscoveryResponse(
                user,
                APIService.apis
            )
            call.respond(discoveryResponse)
        }
    }
}