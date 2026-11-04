package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import io.ktor.openapi.OpenApiDoc

internal fun OpenApiDoc.asBuiltinService(): APIServiceDTO {
    val locators = AviatorServiceUtils.extractServiceLocators(this).map { it.first }
    val mapStyle = APIs.Services.MAPLAYER.locator(ServiceMethods.GATHER)
    // MapLibre consumes the original GET URL and its style metadata, not a
    // Catalyst POST envelope returning an array of provider results.
    val nativeMapStyle = locators.isNotEmpty() && locators.all { it == mapStyle }
    return APIServiceDTO(
        serviceDefinition = this,
        builtin = true,
        catalystTransformable = !nativeMapStyle,
    )
}
