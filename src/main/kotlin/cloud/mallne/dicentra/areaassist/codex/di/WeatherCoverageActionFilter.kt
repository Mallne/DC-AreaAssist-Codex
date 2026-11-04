package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.model.weather.WeatherProviderCandidate
import cloud.mallne.dicentra.areaassist.model.weather.WeatherProviderSelection
import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.synapse.service.CatalystActionFilter
import cloud.mallne.dicentra.synapse.service.ResolvedCatalystAction

/** Selects weather actions whose configured coverage contains the request coordinate. */
class WeatherCoverageActionFilter : CatalystActionFilter {
    override val identity: String = "areaassist:weather-coverage"
    override val selectionInputs: Set<String> = setOf("lat", "lon")

    private val weatherLocators = setOf(
        APIs.Services.WEATHER_SERVICE_WARNING.locator(ServiceMethods.GATHER),
        APIs.Services.WEATHER_SERVICE_CURRENT.locator(ServiceMethods.GATHER),
        APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER),
    )

    override fun select(
        candidates: List<ResolvedCatalystAction>,
        request: CatalystRequest,
        parameters: RequestParameters,
    ): List<ResolvedCatalystAction>? {
        val weatherCandidates = candidates.filter { it.locator in weatherLocators }
        if (weatherCandidates.isEmpty()) return null

        val latitude = parameters.coordinate("lat") ?: return weatherCandidates
        val longitude = parameters.coordinate("lon") ?: return weatherCandidates

        return WeatherProviderSelection.select(
            candidates = weatherCandidates.map { action ->
                WeatherProviderCandidate(action.weatherOptions(), action)
            },
            latitude = latitude,
            longitude = longitude,
        ).map { it.service }
    }

    private fun ResolvedCatalystAction.weatherOptions(): WeatherServiceOptions =
        route.`x-dicentra-aviator-serviceOptions`
            ?.let { InflatedServiceOptions.inflate(it, WeatherServiceOptions.serializer()) }
            ?: WeatherServiceOptions()

    private fun RequestParameters.coordinate(name: String): Double? =
        (this[name] as? RequestParameter.Single<*>)?.value as? Double
}
