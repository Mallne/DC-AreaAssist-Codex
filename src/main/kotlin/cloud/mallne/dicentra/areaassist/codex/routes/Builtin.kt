package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.statics.APIService
import cloud.mallne.dicentra.synapse.model.DiscoveryRequest
import cloud.mallne.dicentra.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO.Companion.transform
import cloud.mallne.dicentra.synapse.service.APIDBService
import cloud.mallne.dicentra.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionGroupRule
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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
    routing {
        authenticate(optional = false) {
            get("/services/builtin") {
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.admin || user.access.superAdmin) {
                    HttpStatusCode.Forbidden to "You need to be at least admin to access the baked in Service Definitions!"
                }
                val discoveryResponse = DiscoveryResponse(
                    user,
                    APIService.apis,
                )
                call.respond(discoveryResponse)
            }
        }
    }
}