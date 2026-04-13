package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.service.SyncService
import cloud.mallne.dicentra.areaassist.model.sync.SyncDownloadResponse
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.synapse.model.Configuration
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalKtorApi::class)
fun Application.sync() {
    val syncPath = "/sync"
    val aggregatePath = "/sync/aggregate"
    val config by inject<Configuration>()
    val db by inject<DatabaseService>()
    val scopeService by inject<ScopeService>()
    val syncService by inject<SyncService>()
    routing {
        authenticate {
            post(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val scope = call.request.queryParameters["scope"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing scope parameter")

                db {
                    user.attachScopes(scopeService)

                    val canWrite = user.canWriteTo(scope)
                    verify(canWrite) {
                        HttpStatusCode.Forbidden to "You do not have write access to this scope"
                    }

                    val packets = call.receive<List<SyncPacket>>()
                    val rejections = syncService.uploadPackets(scope, packets)
                    val accepted = syncService.getLastAccepted(scope)

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "accepted" to accepted.map { packet ->
                                mapOf(
                                    "fingerprint" to packet.fingerprint,
                                    "checksum" to packet.checksum,
                                    "version" to packet.version,
                                    "created" to packet.created.toEpochMilliseconds(),
                                    "updated" to packet.updated.toEpochMilliseconds(),
                                    "blame" to packet.blame,
                                    "isManaged" to packet.isManaged
                                )
                            },
                            "rejected" to rejections.map {
                                mapOf(
                                    "fingerprint" to it.rejection.packetFingerprint,
                                    "reason" to it.rejection.reason.name
                                )
                            },
                            "timestamp" to Clock.System.now().toEpochMilliseconds()
                        )
                    )
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.UPSERT)
                summary = "Upload sync packets to Codex server"
                operationId = "SyncUpload"
                security {
                    bearer()
                }
            }

            get(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val scope = call.request.queryParameters["scope"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing scope parameter")
                val fingerprints = call.request.queryParameters.getAll("fingerprints")

                db {
                    user.attachScopes(scopeService)

                    val hasAccess = user.isDirectMember(scope) || user.isAdminOf(scope)
                    verify(hasAccess) {
                        HttpStatusCode.Forbidden to "You do not have access to this scope"
                    }

                    val entries = if (!fingerprints.isNullOrEmpty()) {
                        syncService.requestPackets(scope, fingerprints)
                    } else {
                        syncService.getAllEntries(scope).filter { !it.isStale() }
                    }

                    call.respond(
                        SyncDownloadResponse<SyncEntryDomain>(
                            packets = entries,
                            timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                        )
                    )
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.GATHER)
                summary = "Download sync packets from Codex server"
                operationId = "SyncDownload"
                security {
                    bearer()
                }
            }

            get(aggregatePath) {
                val user: User = call.authentication.principal()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val scope = call.request.queryParameters["scope"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing scope parameter")

                db {
                    user.attachScopes(scopeService)

                    val hasAccess = user.isDirectMember(scope) || user.isAdminOf(scope)
                    verify(hasAccess) {
                        HttpStatusCode.Forbidden to "You do not have access to this scope"
                    }

                    val aggregate = syncService.getAggregate(scope)
                    call.respond(aggregate)
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.GATHER)
                summary = "Get sync aggregate (fingerprint to checksum mapping)"
                operationId = "SyncAggregate"
                security {
                    bearer()
                }
            }

            delete(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val scope = call.request.queryParameters["scope"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing scope parameter")
                val fingerprints = call.request.queryParameters.getAll("fingerprints")
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing fingerprints parameter")

                db {
                    user.attachScopes(scopeService)

                    val canWrite = user.canWriteTo(scope)
                    verify(canWrite) {
                        HttpStatusCode.Forbidden to "You do not have write access to this scope"
                    }

                    syncService.deletePackets(scope, fingerprints)
                    call.respond(HttpStatusCode.NoContent)
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.DELETE)
                summary = "Delete sync packets from Codex server"
                operationId = "SyncDelete"
                security {
                    bearer()
                }
            }
        }
    }
}
