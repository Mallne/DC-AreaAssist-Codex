package cloud.mallne.dicentra.areaassist.codex.repository

import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toInstant
import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toLocalDateTime
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.LandUsagePacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.LandmarkPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.NotePacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.ParcelPropertyPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.SettingPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket.TaskPacket
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import cloud.mallne.dicentra.synapse.statics.Serialization
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Instant

@Single
class SyncRepository {

    object SyncEntries : IdTable<String>("sync_entries") {
        val scope = varchar("scope", 255).index()
        val fingerprint = varchar("fingerprint", 255).uniqueIndex()
        val packetType = varchar("packet_type", 50)
        val packetData = jsonb<JsonElement>("packet_data", Serialization())
        val checksum = varchar("checksum", 64)
        val version = long("version").default(1L)
        val isStale = bool("is_stale").default(false)
        val isManaged = bool("is_managed").default(false)
        val created = datetime("created").defaultExpression(CurrentDateTime)
        val updated = datetime("updated").defaultExpression(CurrentDateTime)
        val blame = varchar("blame", 255)

        override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    }

    @RequiresTransactionContext
    suspend fun getAllEntries(scope: String): List<SyncEntryRow> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope) }
            .map { row ->
                SyncEntryRow(
                    id = row[SyncEntries.id].value,
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.fingerprint],
                    packetType = row[SyncEntries.packetType],
                    packetData = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version],
                    isStale = row[SyncEntries.isStale],
                    isManaged = row[SyncEntries.isManaged],
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.toList()
    }

    @RequiresTransactionContext
    suspend fun getEntry(fingerprint: String): SyncEntryRow? {
        return SyncEntries.selectAll()
            .where { SyncEntries.fingerprint.eq(fingerprint) }
            .map { row ->
                SyncEntryRow(
                    id = row[SyncEntries.id].value,
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.fingerprint],
                    packetType = row[SyncEntries.packetType],
                    packetData = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version],
                    isStale = row[SyncEntries.isStale],
                    isManaged = row[SyncEntries.isManaged],
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.singleOrNull()
    }

    @RequiresTransactionContext
    suspend fun getDanglingPackets(scope: String): List<SyncPacket> {
        val syncedFingerprints = SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope).and(SyncEntries.isStale.eq(false)) }
            .map { it[SyncEntries.fingerprint] }
            .toList()
            .toSet()

        val allEntries = getAllEntries(scope)
        return allEntries
            .filter { it.fingerprint !in syncedFingerprints || it.isStale }
            .map { row -> decodePacket(row) }
    }

    @RequiresTransactionContext
    suspend fun upsertEntry(entry: SyncEntryDomain) {
        val existing = getEntry(entry.fingerprint)
        if (existing == null) {
            SyncEntries.insert {
                it[id] = EntityID(entry.fingerprint, SyncEntries)
                it[this.scope] = entry.scope
                it[this.fingerprint] = entry.fingerprint
                it[this.packetType] = entry.packet::class.simpleName ?: "unknown"
                it[this.packetData] = Json.encodeToJsonElement(entry.packet)
                it[this.checksum] = entry.checksum
                it[this.version] = entry.version.toLong()
                it[this.isStale] = false
                it[this.isManaged] = entry.isManaged
                it[this.blame] = entry.blame
            }
        } else {
            SyncEntries.update({ SyncEntries.fingerprint.eq(entry.fingerprint) }) {
                it[this.checksum] = entry.checksum
                it[this.version] = entry.version.toLong()
                it[this.updated] = Clock.System.now().toLocalDateTime()
                it[this.packetData] = Json.encodeToJsonElement(entry.packet)
            }
        }
    }

    @RequiresTransactionContext
    suspend fun deleteEntry(fingerprint: String, propagate: Boolean = false) {
        if (propagate) {
            SyncEntries.deleteWhere { SyncEntries.fingerprint.eq(fingerprint) }
        } else {
            SyncEntries.update({ SyncEntries.fingerprint.eq(fingerprint) }) {
                it[this.isStale] = true
                it[this.updated] = Clock.System.now().toLocalDateTime()
            }
        }
    }

    @RequiresTransactionContext
    suspend fun deletePacket(packet: SyncPacket) {
        val fingerprint = when (packet) {
            is ParcelPropertyPacket -> packet.link.forParcel
            is NotePacket -> packet.link.forParcel
            is LandUsagePacket -> packet.link.forParcel
            is LandmarkPacket -> packet.linkedParcels.firstOrNull()?.forParcel ?: ""
            is TaskPacket -> packet.linkedParcels.firstOrNull()?.forParcel ?: ""
            is SettingPacket -> packet.key
        }
        SyncEntries.update({ SyncEntries.fingerprint.eq(fingerprint) }) {
            it[this.isStale] = true
            it[this.updated] = Clock.System.now().toLocalDateTime()
        }
    }

    @RequiresTransactionContext
    suspend fun getAggregate(scope: String): Map<String, String> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope).and(SyncEntries.isStale.eq(false)) }
            .toList()
            .associate { row ->
                row[SyncEntries.fingerprint] to row[SyncEntries.checksum]
            }
    }

    @RequiresTransactionContext
    suspend fun getEntriesByFingerprints(scope: String, fingerprints: List<String>): List<SyncEntryRow> {
        if (fingerprints.isEmpty()) return emptyList()
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope).and(SyncEntries.fingerprint.inList(fingerprints)) }
            .map { row ->
                SyncEntryRow(
                    id = row[SyncEntries.id].value,
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.fingerprint],
                    packetType = row[SyncEntries.packetType],
                    packetData = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version],
                    isStale = row[SyncEntries.isStale],
                    isManaged = row[SyncEntries.isManaged],
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.toList()
    }

    @RequiresTransactionContext
    suspend fun deleteEntriesByFingerprints(scope: String, fingerprints: List<String>) {
        if (fingerprints.isEmpty()) return
        SyncEntries.deleteWhere {
            SyncEntries.scope.eq(scope).and(SyncEntries.fingerprint.inList(fingerprints))
        }
    }

    internal fun decodePacket(row: SyncEntryRow): SyncPacket {
        val json = row.packetData
        return when (row.packetType) {
            "ParcelPropertyPacket" -> Json.decodeFromJsonElement<ParcelPropertyPacket>(json)
            "NotePacket" -> Json.decodeFromJsonElement<NotePacket>(json)
            "LandUsagePacket" -> Json.decodeFromJsonElement<LandUsagePacket>(json)
            "LandmarkPacket" -> Json.decodeFromJsonElement<LandmarkPacket>(json)
            "TaskPacket" -> Json.decodeFromJsonElement<TaskPacket>(json)
            "SettingPacket" -> Json.decodeFromJsonElement<SettingPacket>(json)
            else -> throw IllegalArgumentException("Unknown packet type: ${row.packetType}")
        }
    }

    data class SyncEntryRow(
        val id: String,
        val scope: String,
        val fingerprint: String,
        val packetType: String,
        val packetData: JsonElement,
        val checksum: String,
        val version: Long,
        val isStale: Boolean,
        val isManaged: Boolean,
        val created: Instant,
        val updated: Instant,
        val blame: String
    )
}
