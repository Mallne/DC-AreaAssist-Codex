package cloud.mallne.dicentra.synapse.config

import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.model.IntrospectionResponse
import cloud.mallne.dicentra.synapse.model.OAuthConfig
import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.statics.Client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Application.configureSecurity() {

    // Get security settings and default to enabled if missing
    // See https://ktor.io/docs/server-jwt.html#configure-verifier
    val config by inject<Configuration>()
    val settings = OAuthConfig(config)
    runBlocking {
        settings.configure()
    }
    val scopeService by inject<ScopeService>()


    authentication {
        bearer {
            if (settings.enabled) {
                val log = LoggerFactory.getLogger("Security")
                authenticate { jwt ->
                    val token = jwt.token
                    log.debug(
                        "Attempting introspection for token (first 10 chars): ${token.take(10)}..."
                    )
                    try {
                        val client = Client()
                        val response = client.post(settings.oidcConfig.introspectionEndpoint) {
                            contentType(ContentType.Application.FormUrlEncoded)
                            // Client (this Ktor app) authenticates itself to the introspection endpoint
                            // using its client_id and client_secret
                            setBody(
                                listOf(
                                    "token" to token, // The token to be introspected
                                ).formUrlEncode()
                            )
                            header(HttpHeaders.Authorization, "Basic ${settings.encodedCredentials()}")
                        }.body<IntrospectionResponse>()
                        log.info("User ${response.name} requested a Resource")
                        response.toUser(
                            config = settings,
                            scopes = scopeService.readForAttachment(ScopeService.user(response.preferredUsername))
                                .map { it.name })
                    } catch (e: Exception) {
                        log.error(e.message, e)
                    }
                }
            }
        }
    }
}