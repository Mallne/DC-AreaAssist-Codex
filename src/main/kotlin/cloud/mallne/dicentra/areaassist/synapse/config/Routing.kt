package cloud.mallne.dicentra.areaassist.synapse.config

import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRouting() {
    install(RequestValidation) {
    }
    install(AutoHeadResponse)
}
