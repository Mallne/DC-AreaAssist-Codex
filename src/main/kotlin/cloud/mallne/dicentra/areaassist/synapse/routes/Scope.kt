package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.ScopeRequest
import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.service.ScopeService
import cloud.mallne.dicentra.areaassist.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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
                verify(already.isEmpty()) { HttpStatusCode.Conflict to "The Scope '${body.name}' already exists!" }
                val scopes = body.toDTO()
                for (scope in scopes) {
                    scopeService.create(scope)
                }
                call.respond(body)
            }
        }
    }
}