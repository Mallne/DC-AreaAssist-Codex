package cloud.mallne.dicentra.synapse.model

import cloud.mallne.dicentra.synapse.statics.ResponseObject
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlinx.serialization.Serializable

@Serializable
@ResponseObject
data class DiscoveryResponse(
    val principal: User? = null,
    val services: List<OpenAPI>,
)
