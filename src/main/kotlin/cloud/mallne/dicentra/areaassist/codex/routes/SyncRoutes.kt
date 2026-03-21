package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.areaassist.codex.service.SyncService
import cloud.mallne.dicentra.areaassist.model.sync.SyncApiContracts.SyncDownloadResponse
import cloud.mallne.dicentra.areaassist.model.sync.SyncApiContracts.SyncUploadRequest
import cloud.mallne.dicentra.areaassist.model.sync.SyncApiContracts.SyncUploadResponse
import cloud.mallne.dicentra.areaassist.model.sync.SyncModels.SyncPacket
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.synapse.model.User
import cloud.mallne.dicentra.synapse.service.DatabaseService
import cloud.mallne.dicentra.synapse.service.DiscoveryGenerator
import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.service.ScopeService.Companion.ADMIN_PREFIX
import cloud.mallne.dicentra.synapse.service.ScopeService.Companion.SCOPE_PREFIX
import cloud.mallne.dicentra.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.koin.ktor.ext.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.sync() {
    val syncPath = "/sync"
    val discoveryGenerator by inject<DiscoveryGenerator>()
    val db by inject<DatabaseService>()
    val scopeService by inject<ScopeService>()
    val syncService by inject<SyncService>()

    discoveryGenerator.memorize {
        path(syncPath) {
            operation(
                id = "SyncUpload",
                method = HttpMethod.Post,
                locator = APIs.Services.SYNC_SERVICE.locator(ServiceMethods.UPSERT),
                authenticationStrategy = DiscoveryGenerator.Companion.AuthenticationStrategy.MANDATORY,
                summary = "Upload sync packets to Codex server",
            )
        }
        path(syncPath) {
            operation(
                id = "SyncDownload",
                method = HttpMethod.Get,
                locator = APIs.Services.SYNC_SERVICE.locator(ServiceMethods.GATHER),
                authenticationStrategy = DiscoveryGenerator.Companion.AuthenticationStrategy.MANDATORY,
                summary = "Download sync packets from Codex server",
            )
        }
    }

    routing {
        authenticate {
            post(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                db {
                    user.attachScopes(scopeService)
                    val request = call.receive<SyncUploadRequest>()

                    val scopeName = extractScopeName(request.scope)
                        ?: return@db call.respond(HttpStatusCode.BadRequest, "Invalid scope format")

                    val isAdminOfScope = scopeService.isAdminOf(user.id, scopeName)

                    val packets = request.packets.map { packet ->
                        SyncService.SyncPacketRecord(
                            id = packet.packetId,
                            scope = packet.scope,
                            packetId = packet.packetId,
                            packetType = packet.packetType.name,
                            isManaged = packet.isManaged,
                            data = jsonElementToMap(packet.data),
                            checksum = packet.checksum,
                            version = packet.version.toLong(),
                            updated = packet.updated,
                            created = packet.created,
                            createdBy = user.id
                        )
                    }

                    val result = syncService.uploadPackets(
                        scope = request.scope,
                        packets = packets,
                        userScopes = user.scopes,
                        isAdminOfScope = isAdminOfScope
                    )

                    syncService.updateLastSyncTimestamp(request.scope)

                    call.respond(
                        SyncUploadResponse(
                            accepted = result.accepted,
                            rejected = result.rejected.map {
                                cloud.mallne.dicentra.areaassist.model.sync.SyncApiContracts.RejectedPacket(
                                    packetId = it.packetId,
                                    reason = cloud.mallne.dicentra.areaassist.model.sync.SyncApiContracts.RejectionReason.valueOf(
                                        it.reason.name
                                    ),
                                    serverVersion = it.serverVersion
                                )
                            },
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            get(syncPath) {
                val user: User = call.authentication.principal()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val scope = call.request.queryParameters["scope"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing scope parameter")
                val sinceTimestampStr = call.request.queryParameters["since_timestamp"]
                val sinceTimestamp = sinceTimestampStr?.toLongOrNull()?.let {
                    kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                }

                db {
                    user.attachScopes(scopeService)

                    val scopeName = extractScopeName(scope)
                    val hasAccess = scopeName != null && (
                            user.scopes.contains(scope) ||
                                    (scopeName != null && scopeService.isAdminOf(user.id, scopeName))
                            )

                    verify(hasAccess) {
                        HttpStatusCode.Forbidden to "You do not have access to this scope"
                    }

                    val packets = syncService.downloadPackets(scope, sinceTimestamp)
                    val managedPackets = syncService.downloadManagedPackets(scope)

                    call.respond(
                        SyncDownloadResponse(
                            packets = packets.map { it.toSyncPacket() },
                            managedPackets = managedPackets.map { it.toManagedPacket() },
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}

private fun extractScopeName(scope: String): String? {
    return when {
        scope.startsWith(SCOPE_PREFIX) -> scope.removePrefix(SCOPE_PREFIX)
        scope.startsWith(ADMIN_PREFIX) -> scope.removePrefix(ADMIN_PREFIX)
        else -> null
    }
}

private fun jsonElementToMap(element: JsonElement): Map<String, Any> {
    return when (element) {
        is JsonPrimitive -> mapOf("value" to element.content)
        else -> emptyMap()
    }
}

private fun SyncService.SyncPacketRecord.toSyncPacket(): SyncPacket {
    return SyncPacket(
        scope = scope,
        packetId = packetId,
        packetType = cloud.mallne.dicentra.areaassist.model.sync.PacketType.valueOf(packetType),
        isManaged = isManaged,
        data = JsonPrimitive(""),
        checksum = checksum,
        version = version.toInt(),
        updated = updated,
        created = created
    )
}

private fun SyncService.SyncPacketRecord.toManagedPacket(): cloud.mallne.dicentra.areaassist.model.sync.ManagedPacket {
    return cloud.mallne.dicentra.areaassist.model.sync.ManagedPacket(
        id = id,
        licenseId = createdBy,
        packetType = cloud.mallne.dicentra.areaassist.model.sync.PacketType.valueOf(packetType),
        isEnforced = isManaged,
        data = JsonPrimitive(""),
        version = version.toInt(),
        createdAt = created,
        createdBy = createdBy
    )
}
