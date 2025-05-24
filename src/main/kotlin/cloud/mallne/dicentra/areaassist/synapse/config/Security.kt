package cloud.mallne.dicentra.areaassist.synapse.config

import cloud.mallne.dicentra.areaassist.synapse.helper.toBooleanish
import cloud.mallne.dicentra.areaassist.synapse.model.IntrospectionResponse
import cloud.mallne.dicentra.areaassist.synapse.model.OAuthConfig
import cloud.mallne.dicentra.areaassist.synapse.statics.Client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking

fun Application.configureSecurity() {

    // Get security settings and default to enabled if missing
    // See https://ktor.io/docs/server-jwt.html#configure-verifier
    val settings = OAuthConfig(
        enabled = environment.config.tryGetString("security.enabled")?.toBooleanish() ?: false,
        issuer = environment.config.tryGetString("security.issuer") ?: "",
        scopes = environment.config.tryGetString("security.scopes") ?: "",
        clientId = environment.config.tryGetString("security.client_id") ?: "",
        clientSecret = environment.config.tryGetString("security.client_secret") ?: "",
        roles = OAuthConfig.Roles(
            user = environment.config.tryGetString("security.roles.user") ?: "",
            superAdmin = environment.config.tryGetString("security.roles.superadmin") ?: "",
        )
    )
    runBlocking {
        settings.configure()
    }


    authentication {
        bearer {
            if (settings.enabled) {
                authenticate {
                    val token = it.token
                    this@configureSecurity.log.debug(
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
                        this@configureSecurity.log.info("User ${response.name} requested a Resource")
                        response.toUser(config = settings)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
}