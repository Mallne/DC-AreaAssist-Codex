package cloud.mallne.dicentra.areaassist.synapse.model

import cloud.mallne.dicentra.areaassist.synapse.statics.ResponseObject
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlinx.serialization.Serializable

@Serializable
@ResponseObject
data class DiscoveryResponse(
    val principal: User? = null,
    val services: List<OpenAPI>,
)
