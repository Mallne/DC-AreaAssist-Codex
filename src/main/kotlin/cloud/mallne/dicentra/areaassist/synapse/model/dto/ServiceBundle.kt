package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionGroupRule
import cloud.mallne.dicentra.areaassist.synapse.statics.ServiceDefinitionTransformationType
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.typed.Route
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import kotlinx.serialization.Serializable

@Serializable
data class ServiceBundle(
    val service: APIServiceDTO,
    val route: Route,
    val locator: ServiceLocator,
    val transformationType: ServiceDefinitionTransformationType,
    val groupRule: ServiceDefinitionGroupRule?
) {
    companion object {
        fun List<ServiceBundle>.buildBundles(generator: CatalystGenerator): List<OpenAPI> {
            val catalystTransforms =
                this.filter { it.transformationType == ServiceDefinitionTransformationType.Catalyst }
            val catalystRoutes = catalystTransforms.map {
                generator.generateFor(
                    it.route,
                    if (it.groupRule == ServiceDefinitionGroupRule.Single) it.service.id else null
                )
            }
            val catalystEndpoint = generator.generateWrapper(catalystRoutes)
            val natives = this.filter { it.transformationType == ServiceDefinitionTransformationType.Native }
                .distinctBy { it.service }
            val nativeEndpoints = natives.mapNotNull { it.service.transformNative() }

            return listOf(catalystEndpoint) + nativeEndpoints
        }
    }
}