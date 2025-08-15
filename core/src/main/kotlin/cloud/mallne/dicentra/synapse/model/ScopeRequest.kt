package cloud.mallne.dicentra.synapse.model

import cloud.mallne.dicentra.synapse.model.dto.ScopeDTO
import cloud.mallne.dicentra.synapse.statics.ResponseObject
import kotlinx.serialization.Serializable

@Serializable
@ResponseObject
data class ScopeRequest(
    val name: String,
    val attachments: List<String>,
) {
    fun toDTO() = attachments.map { ScopeDTO(name = name, attaches = it) }
}
