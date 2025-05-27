package cloud.mallne.dicentra.areaassist.synapse

import cloud.mallne.dicentra.areaassist.synapse.config.configureFrameworks
import cloud.mallne.dicentra.areaassist.synapse.config.configureHTTP
import cloud.mallne.dicentra.areaassist.synapse.config.configureSecurity
import cloud.mallne.dicentra.areaassist.synapse.config.routes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val banner = object {}.javaClass.getResource("/banner.txt")?.readText()
    println(banner)
    configureFrameworks()
    configureSecurity()
    configureHTTP()
    routes()
}
