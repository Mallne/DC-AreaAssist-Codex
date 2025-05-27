package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class APIServiceDTO @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val serviceDefinition: OpenAPI,
    val scope: String? = null,
)