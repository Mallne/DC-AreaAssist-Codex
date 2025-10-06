package cloud.mallne.dicentra.areaassist.codex

import cloud.mallne.dicentra.areaassist.codex.di.DCAAAppModule
import cloud.mallne.dicentra.areaassist.codex.routes.builtin
import cloud.mallne.dicentra.areaassist.codex.routes.health
import cloud.mallne.dicentra.areaassist.codex.routes.storedSearch
import cloud.mallne.dicentra.areaassist.codex.service.ActionsService
import cloud.mallne.dicentra.synapse.config.configureDatabase
import cloud.mallne.dicentra.synapse.config.configureHTTP
import cloud.mallne.dicentra.synapse.config.configureSecurity
import cloud.mallne.dicentra.synapse.config.routes
import cloud.mallne.dicentra.synapse.di.AppModule
import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.service.APIDBService
import cloud.mallne.dicentra.synapse.service.ScopeService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.codexModule() {
    val banner = object {}.javaClass.getResource("/banner.txt")?.readText()
    println(banner)
    install(Koin) {
        slf4jLogger()
        modules(org.koin.dsl.module {
            single { Configuration(this@codexModule) }
        })
        modules(AppModule().module)
        modules(DCAAAppModule().module)
    }
    configureDatabase(APIDBService.APIServiceData, ScopeService.Scopes, ActionsService.Actions)
    configureSecurity()
    configureHTTP()
    routes()
    builtin()
    storedSearch()
    health()
}
