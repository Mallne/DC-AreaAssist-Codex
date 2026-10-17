package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
import cloud.mallne.dicentra.areaassist.model.geokit.GeokitVertexInterop
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.synapse.service.CatalystActionFilter
import cloud.mallne.dicentra.synapse.service.ResolvedCatalystAction
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.Vertex

/** Mirrors AreaAssist's parcel endpoint selection: spatial candidates, then parcel-id inference. */
class AreaAssistCatalystActionFilter : CatalystActionFilter {
    override val identity: String = "areaassist:parcel-location"
    private val parcelLocator = APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER)

    override fun select(
        candidates: List<ResolvedCatalystAction>,
        request: CatalystRequest,
        parameters: RequestParameters,
    ): List<ResolvedCatalystAction>? {
        val parcelCandidates = candidates.filter { it.locator == parcelLocator }
        if (parcelCandidates.isEmpty()) return null

        var filtered = parcelCandidates
        val boundary = parameters[AreaAssistParameters.BOUNDARY].asBoundary()
        if (boundary != null) {
            val points = with(GeokitVertexInterop) {
                listOf(boundary.northEast, boundary.northWest, boundary.southEast, boundary.southWest, boundary.center)
                    .map { it.toPoint() }
            }
            filtered = filtered.filter { action ->
                val options = action.parcelOptions()
                options.boundary.coordinates.isEmpty() || points.any { point -> CalculationExtensions.rayCast(point, options.boundary) }
            }
        }

        val parcelId = parameters[AreaAssistParameters.QUERIES].asQueries()
            .firstNotNullOfOrNull { query ->
                if (query.field?.identifier == ParcelConstants.DefaultKeys.PARCELID.identifier) {
                    (query.content as? QueryContentHolder.STRING)?.string
                } else null
            }
        if (parcelId != null) {
            filtered = filtered.filter { action ->
                val prefix = action.parcelOptions().parcelIdPrefix
                // Without an explicit matcher, this service cannot be ruled out by the ID.
                prefix == null || parcelId.startsWith(prefix)
            }
        }
        return filtered
    }

    private fun RequestParameter?.asBoundary(): Boundary? =
        (this as? RequestParameter.Single<*>)?.value as? Boundary

    private fun RequestParameter?.asQueries(): List<Query> =
        (this as? RequestParameter.Multi<*>)?.value?.filterIsInstance<Query>().orEmpty()

    private fun ResolvedCatalystAction.parcelOptions(): ParcelServiceOptions =
        route.`x-dicentra-aviator-serviceOptions`
            ?.let { InflatedServiceOptions.inflate<ParcelServiceOptions>(it, ParcelServiceOptions.serializer()) }
            ?: ParcelServiceOptions()
}
