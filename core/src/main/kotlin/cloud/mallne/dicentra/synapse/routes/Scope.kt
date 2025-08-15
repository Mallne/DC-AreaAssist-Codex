package cloud.mallne.dicentra.synapse.routes

import cloud.mallne.dicentra.synapse.model.ScopeRequest
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Configures routing for scope-related operations and handles requests for creating, reading, and deleting scopes.
 *
 * This method provides routes for managing scopes with the following endpoints:
 *
 * - **GET /scope/{scope}**: Reads a list of attachments for the specified scope name,
 *   ensuring the authenticated user has sufficient permissions.
 * - **DELETE /scope/{scope}**: Deletes the specified scope name if the authenticated user has admin privileges.
 * - **POST /scope**: Creates a new scope based on the payload, provided it does not already exist
 *   and the authenticated user meets the required permissions.
 *
 * Authorization is enforced for all operations, and users must authenticate before using the endpoints.
 *
 * Request validations are performed to ensure:
 * - Required parameters are present.
 * - The authenticated user has adequate privileges based on the operation and scope.
 *
 * Any validation failures will result in appropriate HTTP error responses.
 */
fun Application.scope() {
    val scopeService by inject<ScopeService>()
    routing {
        authenticate {
            get("/scope/{scope}") {
                val scope = call.parameters["scope"]
                verify(scope != null) { HttpStatusCode.BadRequest to "You must provide a scope for this request!" }
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.admin || user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be at least admin for this request!" }
                verify(user.access.superAdmin || user.scopes.contains(scope)) { HttpStatusCode.Forbidden to "You must be a member of the Scope you are trying to obtain!" }
                val scopes = scopeService.readForName(scope)
                val response = ScopeRequest(
                    name = scope,
                    attachments = scopes.map { it.attaches }
                )
                call.respond(response)
            }
            delete("/scope/{scope}") {
                val scope = call.parameters["scope"]
                verify(scope != null) { HttpStatusCode.BadRequest to "You must provide a scope for this request!" }
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.admin || user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be at least admin for this request!" }
                verify(user.access.superAdmin || user.scopes.contains(scope)) { HttpStatusCode.Forbidden to "You must be a member of the Scope you are trying to obtain!" }
                scopeService.deleteByName(scope)
                call.respond(scope)
            }
            post<ScopeRequest>("/scope") {
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                verify(user.access.admin || user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be at least admin for this request!" }
                val body = call.receive<ScopeRequest>()
                verify(user.access.superAdmin || user.scopes.contains(body.name) || body.attachments.contains(user.userScope)) { HttpStatusCode.Forbidden to "You must be a member of the Scope you are trying to create!" }
                val already = scopeService.readForName(body.name)
                verify(already.isNotEmpty()) { HttpStatusCode.Conflict to "The Scope '${body.name}' already exists!" }
                val scopes = body.toDTO()
                for (scope in scopes) {
                    scopeService.create(scope)
                }
                call.respond(body)
            }
        }
    }
}