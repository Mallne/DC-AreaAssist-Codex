package cloud.mallne.dicentra.areaassist

import cloud.mallne.dicentra.areaassist.helper.toBooleanish
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.resources.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.configureHTTP() {
    install(Compression)
    if (environment.config.propertyOrNull("dicentra.behind-proxy")?.getString()?.toBooleanish() == true) {
        install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
        install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    }
    if (environment.config.propertyOrNull("dicentra.enable-cors")?.getString()?.toBooleanish() == true) {
        install(CORS) {
            anyHost()
            anyMethod()
            allowHeader(HttpHeaders.ContentType)
        }
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                explicitNulls = false
            }
        )
    }
    install(Resources)
    install(RateLimit) {
        global {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
        }
    }
}
