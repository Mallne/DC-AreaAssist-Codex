package cloud.mallne.dicentra.areaassist.synapse.routes

import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.discovery() {
    authenticate(optional = true) {
        get("/") {
            call.respondText("true")
        }
    }
}