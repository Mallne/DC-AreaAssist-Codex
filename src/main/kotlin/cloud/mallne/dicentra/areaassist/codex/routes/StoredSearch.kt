package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.model.ActionDTO
import cloud.mallne.dicentra.areaassist.codex.service.ActionsService
import cloud.mallne.dicentra.areaassist.model.actions.ServersideActionHolder
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.service.DatabaseService
import cloud.mallne.dicentra.synapse.service.DiscoveryGenerator
import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.storedSearch() {
    val ssa = "/bundle"
    val ssaClean = "/bundle/admin/clean"
    val ssaId = "$ssa/{id}"
    val discoveryGenerator by inject<DiscoveryGenerator>()
    val db by inject<DatabaseService>()
    val scopeService by inject<ScopeService>()
    val actionService by inject<ActionsService>()

    discoveryGenerator.memorize {
        path(ssaId) {
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
            get(ssaId) {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                db {
                    val action = actionService.read(id) ?: return@db call.respond(HttpStatusCode.NotFound)

                    if (action.scope != null) {
                        val user: User =
                            call.authentication.principal() ?: return@db call.respond(HttpStatusCode.Unauthorized)
                        user.attachScopes(scopeService)

                        verify(user.scopes.contains(action.scope)) { HttpStatusCode.Forbidden to "You are not allowed to use this action" }
                    }

                    if (action.expires <= Clock.System.now()) {
                        log.info("Action is expired, compacting Database")
                        actionService.compact()
                        return@db call.respond(HttpStatusCode.Gone, "Action is expired")
                    }

                    call.respond(
                        ServersideActionHolder(
                            id = action.id,
                            action = action.action
                        )
                    )
                }
            }
        }
        authenticate {
            get(ssaClean) {
                val user: User =
                    call.authentication.principal() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                db {
                    user.attachScopes(scopeService)

                    verify(user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be Superadmin to clean the Database" }

                    log.info("Forcefully compacting Database")
                    actionService.compact()
                    call.respond("Entries have been compacted!")

                }
            }
            post(ssa) {
                val user: User =
                    call.authentication.principal() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                db {
                    user.attachScopes(scopeService)
                    val action = call.receive<ActionDTO>()

                    val threshold = Clock.System.now().plus(21.days)
                    if (action.expires <= threshold && !user.access.superAdmin) {
                        log.info("The actions expiry date is greater than the User allowed Maximum.")
                        return@db call.respond(
                            HttpStatusCode.Forbidden,
                            "The actions expiry date is greater than the User allowed Maximum."
                        )
                    }

                    verify(user.access.admin || user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be at least admin to create actions" }
                    val s = actionService.create(action)
                    call.respond(s)
                }
            }

            get(ssa) {
                val user: User =
                    call.authentication.principal() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                db {
                    user.attachScopes(scopeService)

                    verify(user.access.superAdmin) { HttpStatusCode.Forbidden to "To Display all stored Actions, you must be a Superadmin" }
                    val s = actionService.readAll()
                    call.respond(s)
                }
            }

            delete(ssaId) {
                val user: User =
                    call.authentication.principal() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                db {
                    user.attachScopes(scopeService)

                    verify(user.access.admin || user.access.superAdmin) { HttpStatusCode.Forbidden to "To delete an action you must be at least admin" }

                    actionService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}