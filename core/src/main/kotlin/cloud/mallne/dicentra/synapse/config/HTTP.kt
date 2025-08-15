package cloud.mallne.dicentra.synapse.config

import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.statics.Serialization
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.requestvalidation.*
import org.koin.ktor.ext.inject

fun Application.configureHTTP() {
    val config by inject<Configuration>()
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        if (config.server.cors.all) {
            anyHost()
        }
        if (config.server.cors.hosts.isNotEmpty()) {
            for (string in config.server.cors.hosts) {
                allowHost(string)
            }
        }
    }
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                else -> null
            }
        }
    }
    install(Compression)
    install(ContentNegotiation) {
        json(Serialization())
    }
    install(RequestValidation) {
    }
    install(AutoHeadResponse)
}
