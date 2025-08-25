package cloud.mallne.dicentra.areaassist.codex

import cloud.mallne.dicentra.areaassist.codex.routes.builtin
import cloud.mallne.dicentra.areaassist.codex.routes.storedSearch
import cloud.mallne.dicentra.synapse.config.configureFrameworks
import cloud.mallne.dicentra.synapse.config.configureHTTP
import cloud.mallne.dicentra.synapse.config.configureSecurity
import cloud.mallne.dicentra.synapse.config.routes
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val banner = object {}.javaClass.getResource("/banner.txt")?.readText()
    println(banner)
    configureFrameworks()
    configureSecurity()
    configureHTTP()
    routes()
    builtin()
    storedSearch()
}
