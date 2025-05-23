package cloud.mallne.dicentra.areaassist

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routes() {
    routing {
        discovery()
    }
}