package cloud.mallne.dicentra.synapse.config

import cloud.mallne.dicentra.synapse.di.AppModule
import cloud.mallne.dicentra.synapse.model.Configuration
import io.ktor.server.application.*
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(org.koin.dsl.module {
            single { Configuration(this@configureFrameworks) }
        })
        modules(AppModule().module)
    }
}
