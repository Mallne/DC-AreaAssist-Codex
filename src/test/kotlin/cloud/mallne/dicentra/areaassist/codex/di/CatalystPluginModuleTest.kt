package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.api.DE_TH
import cloud.mallne.dicentra.areaassist.statics.api.OpenMeteo
import cloud.mallne.dicentra.areaassist.statics.api.Brightsky
import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.synapse.service.CatalystActionFilter
import cloud.mallne.dicentra.synapse.service.CatalystPluginContribution
import cloud.mallne.dicentra.synapse.service.CatalystExecutor
import cloud.mallne.dicentra.synapse.service.CatalystFallbackActionFilter
import cloud.mallne.dicentra.synapse.service.ResolvedCatalystAction
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.aviator.koas.typed.Model
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystParameterCodecs
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.aviator.plugin.synapse.toCatalystParameters
import cloud.mallne.dicentra.aviator.plugin.synapse.toRequestParameters
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.coordinates.CrsRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalystPluginModuleTest {
    @Test
    fun `Catalyst forwards AreaAssist parcel inputs through the real WFS adapter`(): Unit = runBlocking {
        val application = koinApplication { modules(CatalystPluginModule) }
        val host = "www.geoproxy.geoportal-th.de"
        var capturedBody: String? = null
        val httpClient = HttpClient(MockEngine { request ->
            assertEquals(host, request.url.host)
            assertEquals(ContentType.Application.Xml, request.body.contentType?.withoutParameters())
            capturedBody = (request.body as TextContent).text
            respond(
                """<wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0" timeStamp="2026-10-16T00:00:00Z" numberMatched="0" numberReturned="0"/>""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Xml.toString()),
            )
        })
        val jsonContribution = object : CatalystPluginContribution {
            override val identity = JsonAdapter.identity
            override fun create(): AviatorPluginInstance = JsonAdapter.install { }
        }
        val xmlContribution = object : CatalystPluginContribution {
            override val identity = XmlAdapter.identity
            override val activateByDefault = true
            override fun create(): AviatorPluginInstance = XmlAdapter.install { }
        }
        val contributions = application.koin.getAll<CatalystPluginContribution>() + listOf(jsonContribution, xmlContribution)
        val definition = DE_TH.value
        val locator = ParcelConstants.locator
        val (resolvedLocator, route) = AviatorServiceUtils.extractServiceLocators(definition)
            .single { it.first.locator == locator.locator && it.first.flavour == locator.flavour }
        val action = ResolvedCatalystAction(
            APIServiceDTO(id = "thuringia-wfs", serviceDefinition = definition),
            route,
            resolvedLocator,
        )
        val executor = CatalystExecutor(
            contributions = contributions,
            client = httpClient,
            allowedHosts = listOf(host),
            resolveProviderHost = { listOf(InetAddress.getByName("8.8.8.8")) },
        )

        try {
            val areaParameters = AreaAssistParameters.parameters(
                queries = listOf(AreaAssistParameters.parcelIdQuery("16-123-456")),
                returnGeometry = false,
                boundary = Boundary(north = 51.0, east = 11.1, south = 50.9, west = 11.0),
            )
            val request = CatalystRequest(typedParameters = areaParameters.toCatalystParameters(Json))

            assertEquals("[]", executor.execute(listOf(action), request).toString())
            assertTrue(requireNotNull(capturedBody).contains("GetFeature"))
            assertTrue(requireNotNull(capturedBody).contains("16-123-456"))

        } finally {
            executor.close()
            application.close()
        }
    }

    @Test
    fun `areaassist runtime composition provides filter ESRI and WFS factories and codecs`() {
        val application = koinApplication { modules(CatalystPluginModule) }
        try {
            val koin = application.koin
            val registry = koin.get<CrsRegistry>()
            assertNotNull(registry)
            assertTrue(registry.understands("4326"))
            assertTrue(registry.understands("1149"))
            assertTrue(registry.understands("25832"))
            assertTrue(registry.understands("25833"))
            assertEquals(
                setOf("areaassist:parcel-location", "areaassist:weather-coverage"),
                koin.getAll<CatalystActionFilter>().map { it.identity }.toSet(),
            )
            assertTrue(
                koin.getAll<CatalystActionFilter>()
                    .single { it.identity == "areaassist:weather-coverage" } is CatalystFallbackActionFilter,
            )

            val contributions = koin.getAll<CatalystPluginContribution>().associateBy { it.identity }
            assertTrue(contributions.containsKey(EsriAdapterPlugin.identity))
            assertTrue(contributions.containsKey(WfsAdapterPlugin.IDENTITY))
            assertTrue(contributions.containsKey(WeaverPlugin.identity))
            val esri = requireNotNull(contributions[EsriAdapterPlugin.identity])
            val wfs = requireNotNull(contributions[WfsAdapterPlugin.IDENTITY])
            assertTrue(esri.parameterCodecs.isNotEmpty())
            assertTrue(wfs.parameterCodecs.isNotEmpty())
            val expectedInputs = setOf(
                AreaAssistParameters.BOUNDARY,
                AreaAssistParameters.QUERIES,
                AreaAssistParameters.RETURN_GEOMETRY,
            )
            assertEquals(expectedInputs, esri.parameterContracts.map { it.name }.toSet())
            assertEquals(expectedInputs, wfs.parameterContracts.map { it.name }.toSet())
            assertEquals(1, esri.resultCodecs.size)
            assertEquals(1, wfs.resultCodecs.size)
            esri.create()
            wfs.create()
        } finally {
            application.close()
        }
    }

    @Test
    fun `OpenMeteo OpenAPI inputs and Weaver request recipe are configured`() {
        val application = koinApplication { modules(CatalystPluginModule) }
        try {
            assertTrue(application.koin.getAll<CatalystPluginContribution>().any { it.identity == WeaverPlugin.identity })
            val definition = OpenMeteo.value
            val (locator, route) = AviatorServiceUtils.extractServiceLocators(definition)
                .single { it.first.locator == APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER).locator }
            assertNotNull(WeatherServiceOptions.OpenMeteo.weaverSchema)
            val requestParametersSchema = assertNotNull(WeatherServiceOptions.OpenMeteo.requestParametersSchema)
            assertTrue(requestParametersSchema.transforms.any { it.key == "parameters" })
            assertTrue(route.input.map { it.name }.toSet().containsAll(setOf("lat", "lon", "date", "last_date", "max_dist", "tz", "units")))
            assertEquals(
                "auto",
                (route.input.single { it.name == "timezone" }.type as Model.Primitive.String).default,
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `weather Catalyst filter orders BrightSky before OpenMeteo in Germany and excludes it outside coverage`() {
        val brightSkyDefinition = Brightsky.value
        val (brightSkyLocator, brightSkyRoute) = AviatorServiceUtils.extractServiceLocators(brightSkyDefinition)
            .single { it.first.locator == APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER).locator }
        val brightSky = ResolvedCatalystAction(
            APIServiceDTO(id = "brightsky", serviceDefinition = brightSkyDefinition), brightSkyRoute, brightSkyLocator,
        )
        val openMeteoDefinition = OpenMeteo.value
        val (openMeteoLocator, openMeteoRoute) = AviatorServiceUtils.extractServiceLocators(openMeteoDefinition)
            .single { it.first.locator == APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER).locator }
        val openMeteo = ResolvedCatalystAction(
            APIServiceDTO(id = "open-meteo", serviceDefinition = openMeteoDefinition), openMeteoRoute, openMeteoLocator,
        )
        val filter = WeatherCoverageActionFilter()

        val germany = filter.select(
            listOf(openMeteo, brightSky),
            CatalystRequest(),
            RequestParameters(mutableMapOf(
                "lat" to RequestParameter.Single(51.4),
                "lon" to RequestParameter.Single(10.8),
            )),
        )
        val paris = filter.select(
            listOf(openMeteo, brightSky),
            CatalystRequest(),
            RequestParameters(mutableMapOf(
                "lat" to RequestParameter.Single(48.86),
                "lon" to RequestParameter.Single(2.35),
            )),
        )

        assertEquals(listOf("brightsky", "open-meteo"), germany?.map { it.service.id })
        assertEquals(listOf("open-meteo"), paris?.map { it.service.id })
    }

    @Test
    fun `AreaAssist boundary query list and boolean survive registered Catalyst codec roundtrip`() {
        val application = koinApplication { modules(CatalystPluginModule) }
        try {
            val contributions = application.koin.getAll<CatalystPluginContribution>()
            val registry = CatalystParameterCodecs(contributions.flatMap { it.parameterCodecs })
            val boundary = Boundary(north = 52.0, east = 13.0, south = 50.0, west = 11.0)
            val queries = listOf(AreaAssistParameters.parcelIdQuery("parcel-42"))
            val parameters = AreaAssistParameters.parameters(queries, returnGeometry = false, boundary = boundary)
            val envelope = CatalystRequest(typedParameters = parameters.toCatalystParameters(Json))

            val decoded = envelope.toRequestParameters(Json, registry)

            assertEquals(boundary, (decoded.getValue(AreaAssistParameters.BOUNDARY) as RequestParameter.Single<*>).value)
            val decodedQueries = (decoded.getValue(AreaAssistParameters.QUERIES) as RequestParameter.Multi<*>).value
            assertEquals(1, decodedQueries.size)
            val decodedQuery = decodedQueries.single() as cloud.mallne.dicentra.areaassist.model.curator.Query
            assertEquals(queries.single().method, decodedQuery.method)
            assertEquals(queries.single().field?.identifier, decodedQuery.field?.identifier)
            assertEquals(
                (queries.single().content as cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder.STRING).string,
                (decodedQuery.content as cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder.STRING).string,
            )
            assertEquals(
                JsonPrimitive(false),
                envelope.typedParameters.getValue(AreaAssistParameters.RETURN_GEOMETRY).values.single(),
            )
        } finally {
            application.close()
        }
    }
}
