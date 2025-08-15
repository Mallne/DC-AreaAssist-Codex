package cloud.mallne.dicentra.synapse.model

import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.synapse.statics.ResponseObject
import cloud.mallne.dicentra.synapse.statics.Serialization
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.aviator.core.mock.MockConverter
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@ResponseObject
data class DiscoveryRequest @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val forScope: String? = null,
    val service: OpenAPI,
    val nativeTransformable: Boolean = true,
    val catalystTransformable: Boolean = true,
    val aggregateApi: Boolean = true,
    val mcpEnabled: Boolean = true,
    val preferredTransform: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Auto,
) {
    init {
        val o = MockConverter(Serialization()).build(service)
        require(o.isNotEmpty()) { "Service should not be empty" }
    }

    fun toDTO(): APIServiceDTO = APIServiceDTO(
        id = id,
        scope = forScope,
        serviceDefinition = service,
        nativeTransformable = nativeTransformable,
        catalystTransformable = catalystTransformable,
        mcpEnabled = mcpEnabled,
        preferredTransform = preferredTransform,
    )
}
