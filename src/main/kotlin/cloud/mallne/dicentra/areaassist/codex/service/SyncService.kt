package cloud.mallne.dicentra.areaassist.codex.service

import cloud.mallne.dicentra.areaassist.codex.repository.SyncRepository
import cloud.mallne.dicentra.areaassist.model.sync.RejectedPacket
import cloud.mallne.dicentra.areaassist.model.sync.RejectionReason
import cloud.mallne.dicentra.areaassist.model.sync.SyncAggregatePaging
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.areaassist.sync.AcceptedPacket
import cloud.mallne.dicentra.areaassist.sync.SyncChecksumGenerator
import cloud.mallne.dicentra.areaassist.sync.SyncFingerprintGenerator
import cloud.mallne.dicentra.areaassist.sync.SyncNetwork
import cloud.mallne.dicentra.areaassist.sync.SyncStorage
import cloud.mallne.dicentra.areaassist.sync.UploadRejection
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Single
class SyncService(
    private val repository: SyncRepository,
    private val checksumGenerator: SyncChecksumGenerator,
    private val fingerprintGenerator: SyncFingerprintGenerator,
) : SyncStorage, SyncNetwork {

    @RequiresTransactionContext
    override suspend fun getAllEntries(scope: String): List<SyncEntryDomain> {
        return repository.getAllEntries(scope).map { row ->
            row.toSyncEntryDomain()
        }
    }

    @RequiresTransactionContext
    override suspend fun getEntry(fingerprint: String): SyncEntryDomain? {
        val row = repository.getEntry(fingerprint) ?: return null
        return row.toSyncEntryDomain()
    }

    @RequiresTransactionContext
    override suspend fun getDanglingPackets(scope: String): List<SyncPacket> {
        return repository.getDanglingPackets(scope)
    }

    @RequiresTransactionContext
    override suspend fun upsertEntry(entry: SyncEntryDomain) {
        repository.upsertEntry(entry)
    }

    @RequiresTransactionContext
    override suspend fun deleteEntry(fingerprint: String, propagate: Boolean) {
        repository.deleteEntry(fingerprint, propagate)
    }

    @RequiresTransactionContext
    override suspend fun deletePacket(packet: SyncPacket) {
        repository.deletePacket(packet)
    }

    @RequiresTransactionContext
    override suspend fun getAggregate(scope: String): SyncAggregatePaging {
        val entries = repository.getAggregate(scope)
        return SyncAggregatePaging(scope, entries)
    }

    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    override suspend fun uploadPackets(
        scope: String,
        packets: List<SyncPacket>,
    ): List<UploadRejection> {
        val rejections = mutableListOf<UploadRejection>()
        val accepted = mutableListOf<AcceptedPacket>()
        val now = Clock.System.now()

        for (packet in packets) {
            val checksum = checksumGenerator.compute(packet)
            val fingerprint = fingerprintGenerator.generate(scope, checksum)

            val existing = repository.getEntry(fingerprint)
            if (existing != null && !existing.isStale) {
                val rejection = RejectedPacket(
                    packetFingerprint = existing.fingerprint,
                    reason = RejectionReason.VERSION_CONFLICT
                )
                rejections.add(UploadRejection(packet, rejection))
                continue
            }

            val version = (existing?.version?.toInt() ?: 0) + 1
            val created = existing?.created ?: now

            val entry = SyncEntryDomainImpl(
                scope = scope,
                fingerprint = fingerprint,
                packet = packet,
                checksum = checksum,
                version = version,
                created = created,
                updated = now,
                blame = "server",
                isManaged = false,
                isStale = false
            )
            repository.upsertEntry(entry)

            accepted.add(
                AcceptedPacket(
                    fingerprint = fingerprint,
                    checksum = checksum,
                    version = version,
                    created = created,
                    updated = now,
                    blame = "server",
                    isManaged = false
                )
            )
        }

        lastAccepted[scope] = accepted
        return rejections
    }

    @RequiresTransactionContext
    override suspend fun requestPackets(
        scope: String,
        fingerprints: List<String>,
    ): List<SyncEntryDomain> {
        val rows = repository.getEntriesByFingerprints(scope, fingerprints)
        return rows.map { it.toSyncEntryDomain() }
    }

    @RequiresTransactionContext
    override suspend fun deletePackets(scope: String, fingerprints: List<String>) {
        repository.deleteEntriesByFingerprints(scope, fingerprints)
    }

    suspend fun getLastAccepted(scope: String): List<AcceptedPacket> {
        return lastAccepted.remove(scope) ?: emptyList()
    }

    private val lastAccepted = mutableMapOf<String, List<AcceptedPacket>>()

    private fun SyncRepository.SyncEntryRow.toSyncEntryDomain(): SyncEntryDomain {
        return SyncEntryDomainImpl(
            scope = this.scope,
            fingerprint = this.fingerprint,
            packet = repository.decodePacket(this),
            checksum = this.checksum,
            version = this.version.toInt(),
            created = this.created,
            updated = this.updated,
            blame = this.blame,
            isManaged = this.isManaged,
            isStale = this.isStale
        )
    }
}

private data class SyncEntryDomainImpl(
    override val scope: String,
    override val fingerprint: String,
    override val packet: SyncPacket,
    override val checksum: String,
    override val version: Int,
    override val created: Instant,
    override val updated: Instant,
    override val blame: String,
    override val isManaged: Boolean,
    private val isStale: Boolean,
) : SyncEntryDomain {
    override suspend fun isStale(): Boolean = isStale
}
