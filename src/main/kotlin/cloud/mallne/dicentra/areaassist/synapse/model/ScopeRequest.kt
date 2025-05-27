package cloud.mallne.dicentra.areaassist.synapse.model

import cloud.mallne.dicentra.areaassist.synapse.model.dto.ScopeDTO
import cloud.mallne.dicentra.areaassist.synapse.statics.ResponseObject
import kotlinx.serialization.Serializable

@Serializable
@ResponseObject
data class ScopeRequest(
    val name: String,
    val attachments: List<String>,
) {
    fun toDTO() = attachments.map { ScopeDTO(name = name, attaches = it) }
}
