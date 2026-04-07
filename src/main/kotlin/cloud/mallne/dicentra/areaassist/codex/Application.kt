package cloud.mallne.dicentra.areaassist.codex

import cloud.mallne.dicentra.areaassist.codex.di.CodexDI
import cloud.mallne.dicentra.areaassist.codex.repository.SyncRepository.SyncEntries
import cloud.mallne.dicentra.areaassist.codex.routes.builtin
import cloud.mallne.dicentra.areaassist.codex.routes.health
import cloud.mallne.dicentra.areaassist.codex.routes.storedSearch
import cloud.mallne.dicentra.areaassist.codex.routes.sync
import cloud.mallne.dicentra.areaassist.codex.service.ActionsService
import cloud.mallne.dicentra.synapse.config.configureDatabase
import cloud.mallne.dicentra.synapse.config.configureHTTP
import cloud.mallne.dicentra.synapse.config.configureSecurity
import cloud.mallne.dicentra.synapse.config.routes
import cloud.mallne.dicentra.synapse.di.DI
import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.service.APIDBService
import cloud.mallne.dicentra.synapse.service.ScopeService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.dsl.module
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
        modules(module {
            single { Configuration(this@codexModule) }
        }, DI, CodexDI)
    }
    configureDatabase(APIDBService.APIServiceData, ScopeService.Scopes, ActionsService.Actions, SyncEntries)
    configureSecurity()
    configureHTTP()
    routes()
    builtin()
    storedSearch()
    sync()
    health()
}
