package cloud.mallne.dicentra.areaassist.codex.routes

import cloud.mallne.dicentra.synapse.statics.ResponseObject
import kotlinx.serialization.Serializable

@ResponseObject
@Serializable
data class SearchBundleRequest(
    val attachesTo: List<String>
)