package cloud.mallne.dicentra.areaassist.codex.routes

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.storedSearch() {
    routing {
        authenticate(optional = true) {
            get("/bundle/{id}") {

            }
        }
    }
}