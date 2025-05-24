package cloud.mallne.dicentra.areaassist.synapse

import cloud.mallne.dicentra.areaassist.synapse.config.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val banner = object {}.javaClass.getResource("/banner.txt")?.readText()
    println(banner)
    configureFrameworks()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    configureRouting()
    routes()
}
