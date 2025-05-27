package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.DiscoveryRequest
import cloud.mallne.dicentra.areaassist.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.service.APIDBService
import cloud.mallne.dicentra.areaassist.synapse.statics.APIService
import cloud.mallne.dicentra.areaassist.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Routing.discovery() {
    val apiService by inject<APIDBService>()
    authenticate(optional = true) {
        get {
            val user: User? = call.authentication.principal()
            val services = apiService.readForScope(null).toMutableList()
            if (user != null) {
                val userServices = apiService.readForScopes(user.scopes)
                services.addAll(userServices)
            }
            val response = DiscoveryResponse(
                user,
                services.map { it.serviceDefinition }
            )
            call.respond(response)
        }
    }
    authenticate(optional = false) {
        get("/services") {
            val user: User? = call.authentication.principal()
            verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
            verify(user.access.admin || user.access.superAdmin) {
                HttpStatusCode.Forbidden to "You need to be at least admin to access the baked in Service Definitions!"
            }
            val discoveryResponse = DiscoveryResponse(
                user,
                APIService.apis
            )
            call.respond(discoveryResponse)
        }

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
                listOf(inDB.serviceDefinition)
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
                inDB.map { it.serviceDefinition }
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