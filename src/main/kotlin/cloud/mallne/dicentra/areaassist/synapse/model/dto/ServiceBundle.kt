package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionGroupRule
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import kotlinx.serialization.Serializable

@Serializable
data class ServiceBundle(
    val service: APIServiceDTO,
    val locator: ServiceLocator,
    val transformationType: ServiceDefinitionTransformationType,
    val groupRule: ServiceDefinitionGroupRule?
)
