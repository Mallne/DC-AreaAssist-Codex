package cloud.mallne.dicentra.areaassist.codex.repository

import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toInstant
import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toLocalDateTime
import cloud.mallne.dicentra.areaassist.model.sync.*
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import cloud.mallne.dicentra.synapse.statics.Serialization
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class SyncRepository {

    object SyncEntries : UuidTable("sync_entries", "fingerprint") {
        val scope = varchar("scope", 255).index()
        val packetData = jsonb<SyncPacket>("packet_data", Serialization())
        val checksum = varchar("checksum", 64)
        val version = long("version").default(1L)
        val created = datetime("created").defaultExpression(CurrentDateTime)
        val updated = datetime("updated").defaultExpression(CurrentDateTime)
        val blame = varchar("blame", 255)
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getAllEntries(scope: String): List<SyncEntryFull> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope) }
            .map { row ->
                SyncEntryFull(
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.id].value.toHexDashString(),
                    packet = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version].toInt(),
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.toList()
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getAllEntries(scopes: List<String>): List<SyncEntryFull> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.inList(scopes) }
            .map { row ->
                SyncEntryFull(
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.id].value.toHexDashString(),
                    packet = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version].toInt(),
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.toList()
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getEntry(fingerprint: String): SyncEntryFull? {
        return SyncEntries.selectAll()
            .where { SyncEntries.id.eq(Uuid.parseHexDash(fingerprint)) }
            .map { row ->
                SyncEntryFull(
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.id].value.toHexDashString(),
                    packet = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version].toInt(),
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.singleOrNull()
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun insert(entry: AutogenerateSyncEntry): Uuid {
        return SyncEntries.insertAndGetId {
            it[this.scope] = entry.scope
            it[this.packetData] = entry.packet
            it[this.checksum] = entry.checksum
            it[this.version] = entry.version.toLong()
            it[this.blame] = entry.blame
        }.value
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun update(entry: SyncEntryDomain) {
        SyncEntries.update({ SyncEntries.id.eq(Uuid.parseHexDash(entry.fingerprint)) }) {
            it[this.checksum] = entry.checksum
            it[this.version] = entry.version.toLong()
            it[this.updated] = Clock.System.now().toLocalDateTime()
            it[this.packetData] = entry.packet
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getAggregate(scope: String): Map<String, String> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.eq(scope) }
            .toList()
            .associate { row ->
                row[SyncEntries.id].value.toHexDashString() to row[SyncEntries.checksum]
            }
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getAggregate(scopes: List<String>): List<AttestationEntry> {
        return SyncEntries.selectAll()
            .where { SyncEntries.scope.inList(scopes) }
            .toList()
            .map {
                AttestationEntry(
                    fingerprint = it[SyncEntries.id].value.toHexDashString(),
                    checksum = it[SyncEntries.checksum],
                    scope = it[SyncEntries.scope]
                )
            }
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun getEntriesByFingerprints(fingerprints: List<String>): List<SyncEntryFull> {
        if (fingerprints.isEmpty()) return emptyList()
        return SyncEntries.selectAll()
            .where { SyncEntries.id.inList(fingerprints.map { Uuid.parseHexDash(it) }) }
            .map { row ->
                SyncEntryFull(
                    scope = row[SyncEntries.scope],
                    fingerprint = row[SyncEntries.id].value.toHexDashString(),
                    packet = row[SyncEntries.packetData],
                    checksum = row[SyncEntries.checksum],
                    version = row[SyncEntries.version].toInt(),
                    created = row[SyncEntries.created].toInstant(),
                    updated = row[SyncEntries.updated].toInstant(),
                    blame = row[SyncEntries.blame]
                )
            }.toList()
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun deleteEntriesByFingerprints(fingerprints: List<String>) {
        if (fingerprints.isEmpty()) return
        SyncEntries.deleteWhere {
            SyncEntries.id.inList(fingerprints.map { Uuid.parseHexDash(it) })
        }
    }
}
