package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.model.actions.OpenScreen
import cloud.mallne.dicentra.areaassist.model.actions.ServersideActionHolder
import cloud.mallne.dicentra.areaassist.model.screen.DeepLinks
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter
import cloud.mallne.dicentra.synapse.service.DiscoveryGenerator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.storedSearch() {
    val ssa = "/bundle/{id}"
    val discoveryGenerator by inject<DiscoveryGenerator>()

    discoveryGenerator.memorize {
        path(ssa) {
            operation(
                id = "ServersideActions",
                method = HttpMethod.Get,
                authenticationStrategy = DiscoveryGenerator.Companion.AuthenticationStrategy.OPTIONAL,
                locator = APIs.Services.SERVERSIDE_ACTIONS.locator(ServiceMethods.GATHER),
                summary = "Get a stored Serverside Action by ID",
                parameter = listOf(
                    Parameter(
                        name = "id",
                        input = Parameter.Input.Path,
                        description = "The globally unique identifier for a serverside Action.",
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    )
                )
            )
        }
    }

    routing {
        authenticate(optional = true) {
            get(ssa) {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(
                    ServersideActionHolder(
                        id = id,
                        action = OpenScreen(
                            link = DeepLinks.DCAA.singleParcel
                        )
                    )
                )
            }
        }
    }
}