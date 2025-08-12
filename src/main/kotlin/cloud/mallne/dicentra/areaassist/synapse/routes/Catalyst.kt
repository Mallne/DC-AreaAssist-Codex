package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import cloud.mallne.dicentra.areaassist.synapse.model.DiscoveryResponse
import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.model.dto.APIServiceDTO.Companion.transform
import cloud.mallne.dicentra.areaassist.synapse.service.APIDBService
import cloud.mallne.dicentra.areaassist.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.areaassist.synapse.statics.APIService
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionGroupRule
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.areaassist.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.catalyst() {
    val config by inject<Configuration>()
    val catalystGenerator by inject<CatalystGenerator>()
    val apiService by inject<APIDBService>()
    routing {
        authenticate(optional = true) {
            get("/catalyst") {
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
    }
}