package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionGroupRule
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.aviator.model.ServiceLocator
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
    val explicitOnly: Boolean = false,
    val mcpEnabled: Boolean = true,
    val preferredTransform: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Auto,
) {
    fun transformOrNull(
        configuration: Configuration,
        requested: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Auto,
    ): ServiceDefinitionTransformationType? {
        return if (requested.canUse(configuration, preferredTransform, nativeTransformable, catalystTransformable)) {
            requested.distill(configuration, preferredTransform)
        } else null
    }

    fun ruleOrNull(
        preferred: ServiceDefinitionGroupRule? = ServiceDefinitionGroupRule.ServiceLocator,
    ): ServiceDefinitionGroupRule? {
        if (!catalystTransformable) return null
        if (explicitOnly) return ServiceDefinitionGroupRule.Single
        if (preferred == ServiceDefinitionGroupRule.All && !aggregateApi) return null
        return preferred ?: ServiceDefinitionGroupRule.ServiceLocator
    }

    fun transformNative(): OpenAPI? {
        return if (nativeTransformable) serviceDefinition else null
    }

    companion object {
        fun List<APIServiceDTO>.transform(
            configuration: Configuration,
            requestedTransformationType: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Auto,
            requestedRule: ServiceDefinitionGroupRule? = null,
        ): List<ServiceBundle> {
            return this.flatMap {
                val transform = it.transformOrNull(configuration, requestedTransformationType)
                val rule =
                    if (transform == ServiceDefinitionTransformationType.Catalyst) it.ruleOrNull(requestedRule) else null
                val locators = AviatorServiceUtils.extractServiceLocators(it.serviceDefinition)
                val bundles = locators.mapNotNull { (locator, route) ->
                    if (transform != null) {
                        ServiceBundle(
                            service = it,
                            locator = locator,
                            transformationType = transform,
                            groupRule = rule,
                        )
                    } else null
                }
                bundles
            }
        }

        fun List<APIServiceDTO>.locatorGroups(): LocatorGroupBundle {
            val groupBundle = this.map {
                it to AviatorServiceUtils.extractServiceLocators(it.serviceDefinition).map { it.first }
            }
            return LocatorGroupBundle(groupBundle)
        }
    }
}