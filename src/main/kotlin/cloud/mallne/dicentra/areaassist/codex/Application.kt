package cloud.mallne.dicentra.areaassist.codex

import cloud.mallne.dicentra.areaassist.codex.routes.builtin
import cloud.mallne.dicentra.areaassist.codex.routes.storedSearch
import cloud.mallne.dicentra.synapse.config.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val banner = object {}.javaClass.getResource("/banner.txt")?.readText()
    println(banner)
    configureFrameworks()
    configureDatabase()
    configureSecurity()
    configureHTTP()
    routes()
    builtin()
    storedSearch()
}
