package cloud.mallne.dicentra.areaassist.synapse.model

import cloud.mallne.dicentra.areaassist.synapse.helper.toBooleanish
import io.ktor.server.application.*
import io.ktor.server.config.*

class Configuration(
    application: Application
) {
    val security = SecurityConfiguration(application)
    val data = DatabaseConfiguration(application)
    val server = ServerConfiguration(application)

    companion object Nested {
        class ServerConfiguration(application: Application) {
            val cors = ServerCorsConfiguration(application)

            companion object Nested {
                class ServerCorsConfiguration(application: Application) {
                    val all: Boolean =
                        application.environment.config.tryGetString("server.cors.all")?.toBooleanish() ?: false
                    val hosts: List<String> =
                        application.environment.config.tryGetString("server.cors.hosts")?.split(",") ?: listOf()
                }
            }
        }

        class SecurityConfiguration(application: Application) {
            val enabled: Boolean =
                application.environment.config.tryGetString("security.enabled")?.toBooleanish() ?: false
            val issuer = application.environment.config.tryGetString("security.issuer") ?: ""
            val scopes = application.environment.config.tryGetString("security.scopes") ?: ""
            val clientId = application.environment.config.tryGetString("security.client_id") ?: ""
            val clientSecret = application.environment.config.tryGetString("security.client_secret") ?: ""
            val roles = SecurityRolesConfiguration(application)

            companion object Nested {
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
            val schema = application.environment.config.tryGetString("data.schema") ?: "areaassist_synapse"
        }
    }
}