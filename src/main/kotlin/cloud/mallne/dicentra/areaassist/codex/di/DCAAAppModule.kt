package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.codex.service.SyncService
import cloud.mallne.dicentra.areaassist.codex.sync.JvmSyncChecksumGenerator
import cloud.mallne.dicentra.areaassist.model.sync.SyncChecksumGenerator
import cloud.mallne.dicentra.areaassist.model.sync.SyncFingerprintGenerator
import cloud.mallne.dicentra.synapse.di.AppModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@Module
@ComponentScan(
    "cloud.mallne.dicentra.areaassist.codex.di",
    "cloud.mallne.dicentra.areaassist.codex.service",
    "cloud.mallne.dicentra.areaassist.codex.repository"
)
@Configuration
class DCAAAppModule

@KoinApplication(modules = [AppModule::class, DCAAAppModule::class])
@Configuration
class DCAACodex

val CodexDI = module {
    singleOf(::SyncService)
}