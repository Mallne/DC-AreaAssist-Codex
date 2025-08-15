package cloud.mallne.dicentra.synapse.routes

import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.model.DiscoveryRequest
import cloud.mallne.dicentra.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO.Companion.transform
import cloud.mallne.dicentra.synapse.service.APIDBService
import cloud.mallne.dicentra.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.synapse.statics.APIService
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
fun Application.discovery() {
    val catalystGenerator by inject<CatalystGenerator>()
    val apiService by inject<APIDBService>()
    routing {
        authenticate(optional = true) {
            get("/services") {
                val user: User? = call.authentication.principal()
                if (call.queryParameters.contains("builtin")) {
                    verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                    verify(user.access.admin || user.access.superAdmin) {
                        HttpStatusCode.Forbidden to "You need to be at least admin to access the baked in Service Definitions!"
                    }
                    val discoveryResponse = DiscoveryResponse(
                        user,
                        APIService.apis,
                    )
                    call.respond(discoveryResponse)
                } else {
                    val services = apiService.readForScope(null).toMutableList()
                    if (user != null) {
                        val userServices = apiService.readForScopes(user.scopes)
                        services.addAll(userServices)
                    }
                    val transformationType = ServiceDefinitionTransformationType.fromString(
                        call.request.queryParameters["transformationType"]
                            ?: ServiceDefinitionTransformationType.Auto.name
                    )

                    val groupRule = ServiceDefinitionGroupRule.fromString(
                        call.request.queryParameters["groupRule"]
                            ?: ServiceDefinitionGroupRule.ServiceLocator.name
                    )

                    val definitions = services.transform(
                        requestedTransformationType = transformationType,
                        requestedRule = groupRule,
                        catalystGenerator = catalystGenerator,
                    )
                    val response = DiscoveryResponse(
                        user,
                        definitions,
                    )
                    call.respond(response)
                }
            }
        }
        authenticate(optional = false) {
            get("/services/{id}") {
                val id = call.parameters["id"]
                verify(id != null) { HttpStatusCode.BadRequest to "You must enter an ID!" }
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                val inDB = apiService.read(id)
                verify(inDB != null) { HttpStatusCode.NotFound to "No Service Definition with this ID present!" }
                verify(inDB.scope == null || user.scopes.contains(inDB.scope)) {
                    HttpStatusCode.Forbidden to "You must be a member of the Scope of the Service Definition you are trying to obtain!"
                }
                val discoveryResponse = DiscoveryResponse(
                    user,
                    listOf(inDB.serviceDefinition),
                )
                call.respond(discoveryResponse)
            }

            get("/services/scope/{scope}") {
                val scope = call.parameters["scope"]
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.superAdmin || user.scopes.contains(scope)) { HttpStatusCode.Forbidden to "You must be a member of the Scope you are trying to obtain!" }
                val inDB = apiService.readForScope(scope)
                val discoveryResponse = DiscoveryResponse(
                    user,
                    inDB.map { it.serviceDefinition },
                )
                call.respond(discoveryResponse)
            }

            post<DiscoveryRequest> {
                val body = call.receive<DiscoveryRequest>()
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                val publicReq = body.forScope == null
                if (publicReq) {
                    verify(user.access.superAdmin) { HttpStatusCode.Forbidden to "You need to be a superadmin to publish public Service Definitions!" }
                }
                verify(publicReq || user.access.superAdmin || user.access.admin && user.scopes.contains(body.forScope) || user.userScope == body.forScope) {
                    HttpStatusCode.Forbidden to "You must be a member of the Scope you are trying to publish the Service Definitions to!"
                }
                val inDB = apiService.read(body.id)
                if (inDB != null) {
                    verify(user.access.superAdmin || (user.access.admin && user.scopes.contains(inDB.scope)) || user.userScope == body.forScope) {
                        HttpStatusCode.Forbidden to "The Service Definition with the id: ${body.id} is already in DB and you are not eligible to alter this resource!"
                    }
                    apiService.update(body.toDTO())
                } else {
                    apiService.create(body.toDTO())
                }
                call.respond(body.id)
            }

            delete("/services/{id}") {
                val id = call.parameters["id"]
                verify(id != null) { HttpStatusCode.BadRequest to "You must enter an ID!" }
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                val inDB = apiService.read(id)
                verify(inDB != null) { HttpStatusCode.NotFound to "No Service Definition with this ID present!" }
                if (inDB.scope == null) {
                    verify(user.access.superAdmin) {
                        HttpStatusCode.Forbidden to "You must be a Superadmin to delete a public Service Definition!"
                    }
                }
                verify(user.access.superAdmin || (user.access.admin && user.scopes.contains(inDB.scope)) || user.userScope == inDB.scope) {
                    HttpStatusCode.Forbidden to "You are not able to delete a public Service Definition!"
                }
                apiService.delete(inDB.id)
                call.respond(inDB.id)
            }
        }
    }
}