package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.service.SyncService
import cloud.mallne.dicentra.areaassist.codex.sync.JvmSyncChecksumGenerator
import cloud.mallne.dicentra.areaassist.model.sync.*
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.synapse.model.Configuration
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.service.DatabaseService
import cloud.mallne.dicentra.synapse.service.ScopeService
import io.ktor.http.*
import io.ktor.openapi.*
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
    val attestationPath = "/sync/attestations"
    val config by inject<Configuration>()
    val db by inject<DatabaseService>()
    val scopeService by inject<ScopeService>()
    val syncService by inject<SyncService>()
    routing {
        authenticate {
            get(attestationPath) {
                val user: User = call.authentication.principal()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                db {
                    user.attachScopes(scopeService)
                    val aggregate = syncService.getAttestations(user.scopes.keys.toList())
                    call.respond(aggregate)
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}SyncAttestations", ServiceMethods.GATHER)
                summary = "Get sync aggregate (fingerprint to checksum mapping)"
                operationId = "SyncAttestations"
                responses {
                    HttpStatusCode.OK {
                        schema = jsonSchema<SyncAggregateAttestation>()
                    }
                    HttpStatusCode.Unauthorized {
                        ContentType.Text.Plain()
                    }
                }
            }

            post(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val body = call.receiveNullable<SyncAggregateRequest>()

                db {
                    user.attachScopes(scopeService)

                    val entries = if (body == null) {
                        syncService.getEntriesByScopes(user.scopes.keys.toList())
                    } else {
                        syncService.getEntriesByFingerprints(body)
                    }

                    call.respond(
                        SyncAggregateResponse(
                            entries = entries,
                            timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                        )
                    )
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.GATHER)
                summary = "Download sync packets from Codex server"
                operationId = "SyncDownload"
                requestBody {
                    content {
                        schema = jsonSchema<SyncAggregateRequest>()
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        schema = jsonSchema<SyncAggregateResponse>()
                    }
                    HttpStatusCode.Unauthorized {
                        ContentType.Text.Plain()
                    }
                }
            }


            patch(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                //mapping of a ClientIdentifier to a Upsert Entity
                val body: Map<String, SyncUpsert> = call.receive()
                val checksumGenerator = JvmSyncChecksumGenerator()

                db {
                    user.attachScopes(scopeService)
                    val accepted: MutableMap<String, SyncEntryFull> = mutableMapOf()
                    val rejected: MutableMap<String, RejectionReason> = mutableMapOf()

                    body.forEach { (identifier, upsert) ->
                        val scope = upsert.scope ?: user.userScope.first
                        if (user.canWriteTo(scope)) {
                            when (upsert) {
                                is SyncUpsert.New -> {
                                    val newone = syncService.insert(
                                        NewSyncEntry(
                                            checksum = checksumGenerator.compute(upsert.packet),
                                            version = 1,
                                            created = Clock.System.now(),
                                            updated = Clock.System.now(),
                                            blame = user.username,
                                            packet = upsert.packet,
                                            scope = scope,
                                        )
                                    )
                                    if (newone != null) {
                                        accepted[identifier] = newone
                                    } else {
                                        rejected[identifier] = RejectionReason.SERVER_ERROR
                                    }
                                }

                                is SyncUpsert.Update -> {
                                    val current = syncService.get(upsert.fingerprint)
                                    if (current != null && current.scope == scope) {
                                        val newone = syncService.update(
                                            current.copy(
                                                packet = upsert.packet,
                                                checksum = checksumGenerator.compute(upsert.packet),
                                                updated = Clock.System.now(),
                                                version = current.version + 1,
                                                blame = user.username,
                                            )
                                        )
                                        if (newone != null) {
                                            accepted[identifier] = newone
                                        } else {
                                            rejected[identifier] = RejectionReason.SERVER_ERROR
                                        }
                                    } else {
                                        rejected[identifier] = RejectionReason.SCOPE_MISMATCH
                                    }
                                }
                            }
                        } else {
                            rejected[identifier] = RejectionReason.PERMISSION_ERROR
                        }
                    }

                    call.respond(SyncUploadResponse(accepted, rejected, Clock.System.now()))
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.UPSERT)
                summary = "Upload sync packets to Codex server"
                operationId = "SyncUpload"
                requestBody {
                    content {
                        schema = jsonSchema<Map<String, SyncUpsert>>()
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        schema = jsonSchema<SyncUploadResponse>()
                    }
                    HttpStatusCode.Unauthorized {
                        ContentType.Text.Plain()
                    }
                }
            }

            delete(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val fingerprints = call.receive<List<String>>()

                db {
                    user.attachScopes(scopeService)
                    val readEntries = syncService.getEntriesByFingerprints(fingerprints)
                    val deletableEntries = readEntries.filter { user.canWriteTo(it.scope) }
                    val loudForbiddenEntries =
                        readEntries.filter { !user.canWriteTo(it.scope) && user.isDirectMember(it.scope) }
                    val silentForbiddenEntries =
                        readEntries.filter { !user.canWriteTo(it.scope) && !user.isDirectMember(it.scope) }

                    syncService.deletePackets(deletableEntries.map { it.fingerprint })
                    call.respond(
                        SyncDeleteResponse(
                            accepted = (deletableEntries + silentForbiddenEntries).map { it.fingerprint },
                            rejected = loudForbiddenEntries.map {
                                RejectedPacket(
                                    entry = it,
                                    reason = RejectionReason.PERMISSION_ERROR
                                )
                            },
                            timestamp = Clock.System.now()
                        )
                    )
                }
            }.describe {
                `x-dicentra-aviator-serviceDelegateCall` =
                    ServiceLocator("${config.server.baseLocator}Sync", ServiceMethods.DELETE)
                summary = "Delete sync packets from Codex server"
                operationId = "SyncDelete"
                requestBody {
                    content {
                        schema = jsonSchema<List<String>>()
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        schema = jsonSchema<SyncDeleteResponse>()
                    }
                    HttpStatusCode.Unauthorized {
                        ContentType.Text.Plain()
                    }
                }
            }
        }
    }
}
