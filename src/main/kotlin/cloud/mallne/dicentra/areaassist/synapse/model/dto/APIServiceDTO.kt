package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class APIServiceDTO @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    val id: String = Uuid.random().toString(),
    val serviceDefinition: OpenAPI,
    val scope: String? = null,
    val created: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val nativeTransformable: Boolean = true,
    val catalystTransformable: Boolean = true,
    val aggregateApi: Boolean = true,
    val mcpEnabled: Boolean = true,
    val preferredTransform: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Auto,
)