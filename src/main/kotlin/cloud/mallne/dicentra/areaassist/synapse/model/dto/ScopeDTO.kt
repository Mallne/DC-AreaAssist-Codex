package cloud.mallne.dicentra.areaassist.synapse.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScopeDTO(
    @Transient
    val id: Int = 0,
    val name: String,
    val attaches: String,
)