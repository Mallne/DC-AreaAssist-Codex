package cloud.mallne.dicentra.areaassist.codex.service

import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toInstant
import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toLocalDateTime
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import cloud.mallne.dicentra.synapse.statics.Serialization
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Single
class SyncService {

    object SyncPackets : IdTable<String>("sync_packets") {
        val scope = varchar("scope", 255).index()
        val packetId = varchar("packet_id", 255)
        val packetType = varchar("packet_type", 50).index()
        val isManaged = bool("is_managed").default(false)
        val data = jsonb<Map<String, Any>>("data", Serialization())
        val checksum = varchar("checksum", 64)
        val version = long("version").default(1L)
        val updated = datetime("updated").defaultExpression(CurrentDateTime)
        val created = datetime("created").defaultExpression(CurrentDateTime)
        val createdBy = varchar("created_by", 255)

        override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    }

    object SyncMetadata : IdTable<String>("sync_metadata") {
        val lastSyncTimestamp = datetime("last_sync_timestamp").nullable()
        val syncEnabled = bool("sync_enabled").default(true)
        val enabledPacketTypes = jsonb<List<String>>("enabled_packet_types", Serialization())

        override val id: Column<EntityID<String>> = varchar("scope", 255).entityId()
    }

    object UserSyncState : IdTable<String>("user_sync_state") {
        val userId = varchar("user_id", 255)
        val scope = varchar("scope", 255)
        val pendingUploads = integer("pending_uploads").default(0)
        val lastUploadTimestamp = datetime("last_upload_timestamp").nullable()

        override val id: Column<EntityID<String>> = varchar("id", 100).entityId()
    }

    @RequiresTransactionContext
    suspend fun upsertPacket(
        scope: String,
        packetId: String,
        packetType: String,
        isManaged: Boolean,
        data: Map<String, Any>,
        checksum: String,
        version: Long,
        createdBy: String
    ): String = SyncPackets.insert {
        it[this.scope] = scope
        it[this.packetId] = packetId
        it[this.packetType] = packetType
        it[this.isManaged] = isManaged
        it[this.data] = data
        it[this.checksum] = checksum
        it[this.version] = version
        it[this.createdBy] = createdBy
    }[SyncPackets.id].value

    @RequiresTransactionContext
    suspend fun getPacketsForScope(scope: String): List<SyncPacketRecord> {
        return SyncPackets.selectAll()
            .where { SyncPackets.scope.eq(scope) }
            .map { row ->
                SyncPacketRecord(
                    id = row[SyncPackets.id].value,
                    scope = row[SyncPackets.scope],
                    packetId = row[SyncPackets.packetId],
                    packetType = row[SyncPackets.packetType],
                    isManaged = row[SyncPackets.isManaged],
                    data = row[SyncPackets.data],
                    checksum = row[SyncPackets.checksum],
                    version = row[SyncPackets.version].toInt(),
                    updated = row[SyncPackets.updated].toInstant(),
                    created = row[SyncPackets.created].toInstant(),
                    createdBy = row[SyncPackets.createdBy]
                )
            }.toList()
    }

    @RequiresTransactionContext
    suspend fun getManagedPacketsForScope(scope: String): List<SyncPacketRecord> {
        return SyncPackets.selectAll()
            .where { SyncPackets.scope.eq(scope).and(SyncPackets.isManaged.eq(true)) }
            .map { row ->
                SyncPacketRecord(
                    id = row[SyncPackets.id].value,
                    scope = row[SyncPackets.scope],
                    packetId = row[SyncPackets.packetId],
                    packetType = row[SyncPackets.packetType],
                    isManaged = row[SyncPackets.isManaged],
                    data = row[SyncPackets.data],
                    checksum = row[SyncPackets.checksum],
                    version = row[SyncPackets.version].toInt(),
                    updated = row[SyncPackets.updated].toInstant(),
                    created = row[SyncPackets.created].toInstant(),
                    createdBy = row[SyncPackets.createdBy]
                )
            }.toList()
    }

    @RequiresTransactionContext
    suspend fun getMetadata(scope: String): SyncMetadataRecord? {
        return SyncMetadata.selectAll()
            .where { SyncMetadata.id.eq(scope) }
            .map { row ->
                SyncMetadataRecord(
                    scope = row[SyncMetadata.id].value,
                    lastSyncTimestamp = row[SyncMetadata.lastSyncTimestamp]?.toInstant(),
                    syncEnabled = row[SyncMetadata.syncEnabled],
                    enabledPacketTypes = row[SyncMetadata.enabledPacketTypes]
                )
            }.singleOrNull()
    }

    @RequiresTransactionContext
    suspend fun upsertMetadata(scope: String, enabledPacketTypes: List<String>) {
        val existing = getMetadata(scope)
        if (existing == null) {
            SyncMetadata.insert {
                it[id] = EntityID(scope, SyncMetadata)
                it[this.syncEnabled] = true
                it[this.enabledPacketTypes] = enabledPacketTypes
            }
        } else {
            SyncMetadata.update({ SyncMetadata.id.eq(scope) }) {
                it[this.enabledPacketTypes] = enabledPacketTypes
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun updateLastSyncTimestamp(scope: String) {
        SyncMetadata.update({ SyncMetadata.id.eq(scope) }) {
            it[this.lastSyncTimestamp] = Clock.System.now().toLocalDateTime()
        }
    }

    @RequiresTransactionContext
    suspend fun deletePacket(id: String) {
        SyncPackets.deleteWhere { SyncPackets.id.eq(id) }
    }

    @RequiresTransactionContext
    suspend fun getUserSyncState(userId: String, scope: String): UserSyncStateRecord? {
        return UserSyncState.selectAll()
            .where { UserSyncState.userId.eq(userId).and(UserSyncState.scope.eq(scope)) }
            .map { row ->
                UserSyncStateRecord(
                    id = row[UserSyncState.id].value,
                    userId = row[UserSyncState.userId],
                    scope = row[UserSyncState.scope],
                    pendingUploads = row[UserSyncState.pendingUploads],
                    lastUploadTimestamp = row[UserSyncState.lastUploadTimestamp]?.toInstant()
                )
            }.singleOrNull()
    }

    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun upsertUserSyncState(userId: String, scope: String, pendingUploads: Int): String {
        val existingId = getUserSyncState(userId, scope)?.id
        if (existingId != null) {
            UserSyncState.update({ UserSyncState.id.eq(existingId) }) {
                it[this.pendingUploads] = pendingUploads
                it[this.lastUploadTimestamp] = Clock.System.now().toLocalDateTime()
            }
            return existingId
        } else {
            return UserSyncState.insert {
                it[id] = EntityID("${userId}_${scope}", UserSyncState)
                it[this.userId] = userId
                it[this.scope] = scope
                it[this.pendingUploads] = pendingUploads
                it[this.lastUploadTimestamp] = Clock.System.now().toLocalDateTime()
            }[UserSyncState.id].value
        }
    }

    @RequiresTransactionContext
    suspend fun incrementPendingUploads(userId: String, scope: String) {
        val state = getUserSyncState(userId, scope)
        if (state != null) {
            upsertUserSyncState(userId, scope, state.pendingUploads + 1)
        } else {
            upsertUserSyncState(userId, scope, 1)
        }
    }

    @RequiresTransactionContext
    suspend fun decrementPendingUploads(userId: String, scope: String) {
        val state = getUserSyncState(userId, scope)
        if (state != null && state.pendingUploads > 0) {
            upsertUserSyncState(userId, scope, state.pendingUploads - 1)
        }
    }

    data class SyncPacketRecord(
        val id: String,
        val scope: String,
        val packetId: String,
        val packetType: String,
        val isManaged: Boolean,
        val data: Map<String, Any>,
        val checksum: String,
        val version: Int,
        val updated: Instant,
        val created: Instant,
        val createdBy: String
    )

    data class SyncMetadataRecord(
        val scope: String,
        val lastSyncTimestamp: Instant?,
        val syncEnabled: Boolean,
        val enabledPacketTypes: List<String>
    )

    data class UserSyncStateRecord(
        val id: String,
        val userId: String,
        val scope: String,
        val pendingUploads: Int,
        val lastUploadTimestamp: Instant?
    )

    data class UploadResult(
        val accepted: List<String>,
        val rejected: List<RejectedPacketRecord>
    )

    data class RejectedPacketRecord(
        val packetId: String,
        val reason: RejectionReason,
        val serverVersion: Int? = null
    )

    enum class RejectionReason {
        VERSION_CONFLICT,
        SCOPE_MISMATCH,
        CHECKSUM_INVALID,
        TYPE_NOT_ALLOWED,
        MANAGED_READ_ONLY,
        SERVER_ERROR
    }

    suspend fun uploadPackets(
        scope: String,
        packets: List<SyncPacketRecord>,
        userScopes: List<String>,
        canWriteToScope: Boolean
    ): UploadResult {
        val accepted = mutableListOf<String>()
        val rejected = mutableListOf<RejectedPacketRecord>()

        for (packet in packets) {
            val rejection = validatePacket(scope, packet, userScopes, canWriteToScope)
            if (rejection != null) {
                rejected.add(rejection)
                continue
            }

            upsertPacket(
                scope = scope,
                packetId = packet.packetId,
                packetType = packet.packetType,
                isManaged = packet.isManaged,
                data = packet.data,
                checksum = packet.checksum,
                version = packet.version.toLong(),
                createdBy = packet.createdBy
            )
            accepted.add(packet.packetId)
        }

        return UploadResult(accepted, rejected)
    }

    private suspend fun validatePacket(
        scope: String,
        packet: SyncPacketRecord,
        userScopes: List<String>,
        canWriteToScope: Boolean
    ): RejectedPacketRecord? {
        if (packet.scope != scope) {
            return RejectedPacketRecord(packet.packetId, RejectionReason.SCOPE_MISMATCH)
        }

        if (packet.isManaged && !canWriteToScope) {
            return RejectedPacketRecord(packet.packetId, RejectionReason.MANAGED_READ_ONLY)
        }

        val existingPackets = getPacketsForScope(scope).filter { it.packetId == packet.packetId }
        if (existingPackets.isNotEmpty()) {
            val latest = existingPackets.maxByOrNull { it.version }
            if (latest != null && packet.version <= latest.version) {
                return RejectedPacketRecord(packet.packetId, RejectionReason.VERSION_CONFLICT, latest.version)
            }
        }

        return null
    }

    @OptIn(ExperimentalTime::class)
    suspend fun downloadPackets(
        scope: String,
        sinceTimestamp: Instant?,
        includeManaged: Boolean = true
    ): List<SyncPacketRecord> {
        val packets = getPacketsForScope(scope)

        return if (sinceTimestamp != null) {
            packets.filter { it.updated > sinceTimestamp }
        } else {
            packets
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun downloadManagedPackets(scope: String): List<SyncPacketRecord> {
        return getManagedPacketsForScope(scope)
    }
}
