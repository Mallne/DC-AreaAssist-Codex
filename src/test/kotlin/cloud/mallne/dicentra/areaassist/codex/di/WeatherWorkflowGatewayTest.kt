package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.statics.api.OpenMeteo
import cloud.mallne.dicentra.areaassist.statics.api.WeatherWorkflows
import cloud.mallne.dicentra.aviator.orchestration.model.ExecutionOutcome
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.synapse.service.CatalystExecutor
import cloud.mallne.dicentra.synapse.service.CatalystPluginContribution
import cloud.mallne.dicentra.synapse.service.StoredWorkflow
import cloud.mallne.dicentra.synapse.service.SynapsePluginBundle
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.koinApplication
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WeatherWorkflowGatewayTest {
    @Test
    fun `bundled workflow maps logical coordinates through Synapse and Weaver`() = runBlocking {
        val koin = koinApplication { modules(CatalystPluginModule) }
        val bundle = koin.koin.get<SynapsePluginBundle>()
        var latitude: String? = null
        var longitude: String? = null
        val client = HttpClient(MockEngine { request ->
            latitude = request.url.parameters["latitude"]
            longitude = request.url.parameters["longitude"]
            assertEquals(null, request.url.parameters["lat"])
            assertEquals(null, request.url.parameters["lon"])
            respond(
                """{"latitude":52.52,"longitude":13.41,"timezone":"Europe/Berlin","current":{"time":"2026-10-19T12:00:00+02:00","temperature_2m":18.5,"weather_code":2,"apparent_temperature":18.0,"relative_humidity_2m":60,"cloud_cover":40,"pressure_msl":1013.2,"precipitation":0.2,"wind_speed_10m":3.6,"wind_direction_10m":180,"wind_gusts_10m":5.4},"hourly":{"time":["2026-10-19T12:00:00+02:00"],"temperature_2m":[18.5],"weather_code":[2],"apparent_temperature":[18.0],"dew_point_2m":[10.0],"relative_humidity_2m":[60],"cloud_cover":[40],"precipitation":[0.2],"precipitation_probability":[10],"pressure_msl":[1013.2],"visibility":[20000],"wind_speed_10m":[3.6],"wind_direction_10m":[180],"wind_gusts_10m":[5.4],"shortwave_radiation":[100],"sunshine_duration":[900]},"daily":{"time":["2026-10-19"],"temperature_2m_min":[12.0],"temperature_2m_max":[20.0],"precipitation_sum":[1.2],"precipitation_probability_max":[20]}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })
        val jsonContribution = object : CatalystPluginContribution {
            override val identity = JsonAdapter.identity
            override val activateByDefault = true
            override fun create() = JsonAdapter.install { }
        }
        val executor = CatalystExecutor(
            contributions = bundle.catalystPlugins + jsonContribution,
            client = client,
            allowedHosts = listOf("api.open-meteo.com"),
            resolveProviderHost = { listOf(InetAddress.getByName("8.8.8.8")) },
            resolveWorkflowSource = bundle.resolveWorkflowSource,
            workflowParameterAliases = bundle.workflowParameterAliases,
        )
        try {
            val result = executor.executeWorkflow(
                StoredWorkflow("weather", null, WeatherWorkflows.openMeteo),
                "forecast",
                listOf(APIServiceDTO(id = "open-meteo", serviceDefinition = OpenMeteo.value, builtin = true)),
                Json.parseToJsonElement("""{"lat":52.52,"lon":13.41}"""),
            )
            assertIs<ExecutionOutcome.Succeeded>(result.outcome)
            assertEquals("52.52", latitude)
            assertEquals("13.41", longitude)
        } finally {
            executor.close()
            koin.close()
        }
    }
}
