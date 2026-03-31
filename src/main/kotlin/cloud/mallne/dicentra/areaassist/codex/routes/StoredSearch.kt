package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.model.ActionDTO
import cloud.mallne.dicentra.areaassist.codex.service.ActionsService
import cloud.mallne.dicentra.areaassist.model.actions.ServersideActionHolder
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.service.DatabaseService
import cloud.mallne.dicentra.synapse.service.DiscoveryGenerator.Companion.bearer
import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalKtorApi::class)
fun Application.storedSearch() {
    val db by inject<DatabaseService>()
    val scopeService by inject<ScopeService>()
    val actionService by inject<ActionsService>()

    routing {
        authenticate(optional = true) {
            get("/bundle/{id}") {
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
                            action = action.action,
                        ),
                    )
                }
            }
        }.describe {
            `x-dicentra-aviator-serviceDelegateCall` =
                APIs.Services.SERVERSIDE_ACTIONS.locator(ServiceMethods.GATHER)
            summary = "Get a stored Serverside Action by ID"
            operationId = "ServersideActions"
            security {
                optional()
                bearer()
            }
        }
        authenticate {
            get("/bundle/admin/clean") {
                val user: User =
                    call.authentication.principal() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                db {
                    user.attachScopes(scopeService)

                    verify(user.access.superAdmin) { HttpStatusCode.Forbidden to "You must be Superadmin to clean the Database" }

                    log.info("Forcefully compacting Database")
                    actionService.compact()
                    call.respond("Entries have been compacted!")
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    APIs.Services.SERVERSIDE_ACTIONS.locator(ServiceMethods.GATHER)
                summary = "Get a stored Serverside Action by ID"
                operationId = "ServersideActions"
                security {
                    bearer()
                }
            }
            post("/bundle") {
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
                            "The actions expiry date is greater than the User allowed Maximum.",
                        )
                    }

                    val actionScope = action.scope
                    verify(user.access.superAdmin || actionScope == null || user.canWriteTo(actionScope)) {
                        HttpStatusCode.Forbidden to
                                "You must be at least admin to create actions in this scope"
                    }
                    val s = actionService.create(action)
                    call.respond(s)
                }
            }.describe {
                summary = "Create a stored Serverside Action"
                operationId = "CreateServersideAction"
                security {
                    bearer()
                }
            }

            get("/bundle") {
                val user: User =
                    call.authentication.principal() ?: return@get call.respond(HttpStatusCode.Unauthorized)

                db {
                    user.attachScopes(scopeService)

                    verify(user.access.superAdmin) { HttpStatusCode.Forbidden to "To Display all stored Actions, you must be a Superadmin" }
                    val s = actionService.readAll()
                    call.respond(s)
                }
            }.describe {
                summary = "Display all Stored Serverside Actions - Superadmin management"
                operationId = "AllServersideActions"
                security {
                    bearer()
                }
            }

            delete("/bundle/{id}") {
                val user: User =
                    call.authentication.principal() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                db {
                    user.attachScopes(scopeService)

                    val existingAction = actionService.read(id)
                        ?: return@db call.respond(HttpStatusCode.NotFound)

                    val actionScope = existingAction.scope
                    verify(user.access.superAdmin || actionScope == null || user.canWriteTo(actionScope)) {
                        HttpStatusCode.Forbidden to
                                "To delete an action you must be at least admin in this scope"
                    }

                    actionService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }.describe {
                summary = "Delete a stored Serverside Action by ID"
                operationId = "DeleteServersideAction"
                security {
                    bearer()
                }
            }
        }
    }
}
