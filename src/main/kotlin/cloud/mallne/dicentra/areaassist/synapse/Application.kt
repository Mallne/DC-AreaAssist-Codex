package cloud.mallne.dicentra.areaassist.synapse

import cloud.mallne.dicentra.areaassist.synapse.config.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    configureRouting()
    routes()
}
