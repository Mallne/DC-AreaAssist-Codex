package cloud.mallne.dicentra.synapse.model

import cloud.mallne.dicentra.synapse.helper.toBooleanish
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import io.ktor.server.application.*
import io.ktor.server.config.*

class Configuration(
    application: Application
) {
    val security = SecurityConfiguration(application)
    val data = DatabaseConfiguration(application)
    val server = ServerConfiguration(application)
    val catalyst = CatalystConfiguration(application)
    val preferredTransform: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.fromString(
        application.environment.config.tryGetString("preferred_transform")
            ?: ServiceDefinitionTransformationType.Native.name
    )

    init {
        require(preferredTransform != ServiceDefinitionTransformationType.Auto) { "Auto is not an allowed preferred transform" }
        require(preferredTransform.canUse(this)) { "Preferred transform is not allowed" }
    }

    companion object Nested {
        class ServerConfiguration(application: Application) {
            val cors = ServerCorsConfiguration(application)
            val hostname = application.environment.config.tryGetString("server.hostname") ?: "0.0.0.0"

            companion object Nested {
                class ServerCorsConfiguration(application: Application) {
                    val all: Boolean =
                        application.environment.config.tryGetString("server.cors.all")?.toBooleanish() ?: false
                    val hosts: List<String> =
                        application.environment.config.tryGetString("server.cors.hosts")?.split(",") ?: listOf()
                }
            }
        }

        class CatalystConfiguration(application: Application) {
            val enabled = application.environment.config.tryGetString("catalyst.enabled")?.toBooleanish() ?: true
            val anonymous = application.environment.config.tryGetString("catalyst.anonymous")?.toBooleanish() ?: true
            val serverName = application.environment.config.tryGetString("catalyst.server_name")
                ?: application.environment.config.tryGetString("server.hostname") ?: "0.0.0.0"
            val tlsEnabled = application.environment.config.tryGetString("catalyst.tls_enabled")?.toBooleanish() ?: true
            val title = application.environment.config.tryGetString("catalyst.title") ?: "Synapse Catalyst"
            val description = application.environment.config.tryGetString("catalyst.description")
                ?: "Make Requests to a stored Service in a single Tenant setup."
        }

        class SecurityConfiguration(application: Application) {
            val enabled: Boolean =
                application.environment.config.tryGetString("security.enabled")?.toBooleanish() ?: false
            val issuer = application.environment.config.tryGetString("security.issuer") ?: ""
            val scopes = application.environment.config.tryGetString("security.scopes") ?: ""
            val clientId = application.environment.config.tryGetString("security.client_id") ?: ""
            val clientSecret = application.environment.config.tryGetString("security.client_secret") ?: ""
            val roles = SecurityRolesConfiguration(application)
            val app = SecurityAppConfiguration(application)

            companion object Nested {
                class SecurityAppConfiguration(application: Application) {
                    val clientId = application.environment.config.tryGetString("security.app.client_id") ?: ""
                    val scopes = application.environment.config.tryGetString("security.app.scopes") ?: ""
                    val issuer = application.environment.config.tryGetString("security.app.issuer") ?: ""
                }

                class SecurityRolesConfiguration(application: Application) {
                    val user = application.environment.config.tryGetString("security.roles.user") ?: ""
                    val admin = application.environment.config.tryGetString("security.roles.admin") ?: ""
                    val superAdmin = application.environment.config.tryGetString("security.roles.superadmin") ?: ""
                }
            }
        }

        class DatabaseConfiguration(application: Application) {
            val url = application.environment.config.tryGetString("data.url") ?: ""
            val user = application.environment.config.tryGetString("data.user") ?: ""
            val password = application.environment.config.tryGetString("data.password") ?: ""
            val schema = application.environment.config.tryGetString("data.schema") ?: "synapse"
        }
    }
}