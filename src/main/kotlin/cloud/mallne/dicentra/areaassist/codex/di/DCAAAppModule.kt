package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.codex.service.ActionsService
import cloud.mallne.dicentra.areaassist.codex.service.SyncService
import cloud.mallne.dicentra.synapse.di.AppModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@Module
@ComponentScan("cloud.mallne.dicentra.areaassist.codex.di", "cloud.mallne.dicentra.areaassist.codex.service")
@Configuration
class DCAAAppModule

@KoinApplication(modules = [AppModule::class, DCAAAppModule::class])
@Configuration
class DCAACodex

val CodexDI = module {
    singleOf(::ActionsService)
    singleOf(::SyncService)
}