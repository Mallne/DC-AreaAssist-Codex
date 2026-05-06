package cloud.mallne.dicentra.areaassist.codex.service

import cloud.mallne.dicentra.areaassist.codex.repository.SyncRepository
import cloud.mallne.dicentra.areaassist.model.sync.AutogenerateSyncEntry
import cloud.mallne.dicentra.areaassist.model.sync.SyncAggregateAttestation
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryFull
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@Single
class SyncService(
    private val repository: SyncRepository
) {

    @RequiresTransactionContext
    suspend fun getEntriesByScopes(scopes: List<String>): List<SyncEntryFull> {
        return repository.getAllEntries(scopes)
    }

    @RequiresTransactionContext
    suspend fun getEntriesByFingerprints(fingerprints: List<String>): List<SyncEntryFull> {
        return repository.getEntriesByFingerprints(fingerprints)
    }

    @RequiresTransactionContext
    suspend fun get(fingerprint: String): SyncEntryFull? {
        return repository.getEntry(fingerprint)
    }

    @RequiresTransactionContext
    suspend fun getAttestations(scopes: List<String>): SyncAggregateAttestation {
        val entries = repository.getAggregate(scopes)
        return SyncAggregateAttestation(entries)
    }

    @OptIn(ExperimentalUuidApi::class)
    @RequiresTransactionContext
    suspend fun insert(autogenerateSyncEntry: AutogenerateSyncEntry): SyncEntryFull? {
        val fingerprint = repository.insert(autogenerateSyncEntry)
        return repository.getEntry(fingerprint.toHexDashString())
    }

    @RequiresTransactionContext
    suspend fun update(entry: SyncEntryDomain): SyncEntryFull? {
        repository.update(entry)
        return repository.getEntry(entry.fingerprint)
    }

    @RequiresTransactionContext
    suspend fun deletePackets(fingerprints: List<String>) {
        repository.deleteEntriesByFingerprints(fingerprints)
    }
}
