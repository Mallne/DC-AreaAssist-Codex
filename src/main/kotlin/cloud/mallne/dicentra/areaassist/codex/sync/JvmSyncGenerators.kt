package cloud.mallne.dicentra.areaassist.codex.sync

import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.areaassist.sync.SyncChecksumGenerator
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.security.MessageDigest

@Single
class JvmSyncChecksumGenerator : SyncChecksumGenerator {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    override fun compute(packet: SyncPacket): String {
        val canonicalJson = json.encodeToString(kotlinx.serialization.serializer<SyncPacket>(), packet)
        return sha256(canonicalJson.toByteArray())
    }

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).toHexString()
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
}
