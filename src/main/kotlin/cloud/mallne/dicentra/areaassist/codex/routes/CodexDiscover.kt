package cloud.mallne.dicentra.areaassist.codex.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun Application.codexDiscover() {
    routing {
        get("/") {

        }
    }
}