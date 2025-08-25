package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.synapse.service.APIDBService
import cloud.mallne.dicentra.synapse.service.DiscoveryGenerator
import cloud.mallne.dicentra.synapse.statics.ResponseObject
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

/**
 * Configures the discovery-related endpoints for the application. This method
 * sets up a routing structure for handling service discovery, retrieval, creation,
 * updates, and deletions.
 *
 * The following routes are defined:
 *
 * - A GET endpoint to fetch public and user-specific services. If a user is
 *   authenticated, their scopes are used to filter accessible services. Supports
 *   optional authentication for public data access.
 *
 * - A GET endpoint at `/services` to retrieve all available services in the system.
 *   This route requires authentication and admin or superadmin privileges.
 *
 * - A GET endpoint at `/services/{id}` to retrieve a specific service by its ID.
 *   This route requires authentication and enforces scope restrictions for access.
 *
 * - A GET endpoint at `/services/scope/{scope}` to retrieve services linked to a
 *   specific scope. This route requires authentication and verifies that the user
 *   belongs to the specified scope or is a superadmin.
 *
 * - A POST endpoint to create or update service definitions. Scope verification
 *   ensures the user has adequate permissions to publish or modify services.
 *
 * - A DELETE endpoint at `/services/{id}` to remove a specific service by its ID.
 *   Deletion is scope-protected, requiring superadmin rights for public services
 *   and appropriate permissions for scoped services.
 *
 * Authentication and authorization are central to all routes, with various levels
 * of access restrictions based on user roles (e.g., admin, superadmin) and scopes.
 * Responses include relevant HTTP status codes and error messages for failed
 * verification checks.
 */
fun Application.builtin() {
    val builtinService = "/services/builtin"
    val builtinServiceAutoIngest = "/services/builtin/ingest"

    val discoveryGenerator by inject<DiscoveryGenerator>()
    val config by inject<Configuration>()
    val apiService by inject<APIDBService>()

    val log = LoggerFactory.getLogger("Builtin")

    discoveryGenerator.memorize {
        path(builtinService) {
            operation(
                id = "BuiltinServices",
                method = HttpMethod.Get,
                locator = ServiceLocator("${config.server.baseLocator}Builtin", ServiceMethods.GATHER),
                authenticationStrategy = DiscoveryGenerator.Companion.AuthenticationStrategy.MANDATORY,
                summary = "Get all the Builtin Services",
            )
        }
        path(builtinServiceAutoIngest) {
            operation(
                id = "AutoIngestBuiltinServices",
                method = HttpMethod.Get,
                locator = ServiceLocator("${config.server.baseLocator}BuiltinIngest", ServiceMethods.GATHER),
                authenticationStrategy = DiscoveryGenerator.Companion.AuthenticationStrategy.MANDATORY,
                summary = "Automatically ingest and overwrite the Builtin Services as global services. Perfect for server Quickstart.",
            )
        }
    }

    routing {
        authenticate(optional = false) {
            get(builtinService) {
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.admin || user.access.superAdmin) {
                    HttpStatusCode.Forbidden to "You need to be at least admin to access the baked in Service Definitions!"
                }
                val discoveryResponse = DiscoveryResponse(
                    user,
                    APIs.apis,
                )
                call.respond(discoveryResponse)
            }

            get(builtinServiceAutoIngest) {
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.superAdmin) {
                    HttpStatusCode.Forbidden to "You need to be a Super Admin to auto-ingest the Builtin Services!"
                }

                val services = APIs.apis.map {
                    APIServiceDTO(
                        serviceDefinition = it,
                        builtin = true
                    )
                }

                log.info("Auto-ingesting ${services.size} Builtin Services...")

                val toDelete = apiService.readBuiltin()
                val deleted = mutableListOf<String>()
                toDelete.collect {
                    apiService.delete(it.id)
                    log.debug("Deleted Builtin Service: ${it.id}")
                    deleted.add(it.id)
                }

                val created = mutableListOf<String>()
                log.info("Creating ${services.size} Builtin Services...")
                for (service in services) {
                    val id = apiService.create(service)
                    log.debug("Created Builtin Service: $id")
                    created.add(id)
                }

                log.info("Auto-ingest complete!")

                call.respond(AutoIngestResponse(deleted, created))
            }
        }
    }
}

@Serializable
@ResponseObject
private data class AutoIngestResponse(
    val deleted: List<String>,
    val created: List<String>,
)