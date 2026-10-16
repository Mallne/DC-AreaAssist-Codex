package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.koas.typed.Model
import cloud.mallne.dicentra.aviator.koas.typed.Route
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.aviator.plugin.synapse.toCatalystParameters
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.synapse.service.CatalystExecutor
import cloud.mallne.dicentra.synapse.service.CatalystPluginContribution
import cloud.mallne.dicentra.synapse.service.ResolvedCatalystAction
import cloud.mallne.geokit.Boundary
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.openapi.ParameterType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.dsl.koinApplication
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalystAreaAssistParityTest {
    @Test
    fun `ESRI direct Aviator and Catalyst invocations share provider request and final parcel output`() = runBlocking {
        val providerRequests = mutableListOf<Pair<HttpMethod, String>>()
        val client = HttpClient(MockEngine { request ->
            providerRequests += request.method to request.url.toString()
            respond(
                """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":null,"properties":{"flurstueckskennzeichen":"parcel-42","amtlicheFlaeche":25.0}}]}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        val (locator, sourceRoute) = AviatorServiceUtils.extractServiceLocators(APIs.esri)
            .first { (_, route) -> EsriAdapterPlugin.identity in route.`x-dicentra-aviator-pluginMaterialization`.orEmpty() }
        val koin = koinApplication { modules(CatalystPluginModule) }
        val contribution = koin.koin.getAll<CatalystPluginContribution>()
            .single { it.identity == EsriAdapterPlugin.identity }
        val inputRoute = sourceRoute.copy(
            input = listOf(
                Route.Input(
                    AreaAssistParameters.BOUNDARY,
                    Model.FreeFormJson(null, null),
                    false,
                    ParameterType.query,
                    null,
                ),
                Route.Input(
                    AreaAssistParameters.QUERIES,
                    Model.Collection.List(Model.FreeFormJson(null, null), null, null, null),
                    false,
                    ParameterType.query,
                    null,
                ),
                Route.Input(
                    AreaAssistParameters.RETURN_GEOMETRY,
                    Model.Primitive.Boolean(null, null),
                    false,
                    ParameterType.query,
                    null,
                ),
            ),
        )
        val action = ResolvedCatalystAction(APIServiceDTO(id = "areaassist-esri", serviceDefinition = APIs.esri), inputRoute, locator)
        val parameters = AreaAssistParameters.parameters(
            queries = listOf(AreaAssistParameters.parcelIdQuery("parcel-42")),
            returnGeometry = false,
            boundary = Boundary(north = 52.0, east = 13.0, south = 50.0, west = 11.0),
        )
        assertEquals(parameters.keys, inputRoute.input.map { it.name }.filter { it in parameters }.toSet())
        val envelope = CatalystRequest(typedParameters = parameters.toCatalystParameters(Json))
        val executor = CatalystExecutor(
            contributions = listOf(contribution),
            client = client,
            allowedHosts = listOf("services2.arcgis.com"),
            resolveProviderHost = { listOf(InetAddress.getByName("8.8.8.8")) },
        )
        try {
            val directService = KtorAviatorServiceConverter(client, Json).build(APIs.esri) {
                add(EsriAdapterPlugin.install { })
            }.single { it.serviceLocator == locator && it.route.path == sourceRoute.path }
            val direct = requireNotNull(directService.requestContextful<List<ParcelCrateEntity>, NoBody>(
                useSerializer = ListSerializer(ParcelCrateEntity.serializer()),
                requestParams = parameters,
            ).result)
            val directWire = Json.encodeToJsonElement(ListSerializer(ParcelCrateEntity.serializer()), direct)

            val gateway = executor.execute(
                listOf(action),
                envelope,
            )

            assertEquals(normalize(directWire), normalize(gateway))
            assertTrue(direct.isNotEmpty(), "Parity must compare a real parcel result, not two empty lists")
            assertEquals(2, providerRequests.size)
            assertEquals(providerRequests.first(), providerRequests.last())
            assertEquals(HttpMethod.Get, providerRequests.first().first)
            assertTrue(providerRequests.first().second.contains("where="))
            assertTrue(providerRequests.first().second.contains("geometry="))
        } finally {
            executor.close()
            koin.close()
        }
    }

    private fun normalize(value: JsonElement): JsonElement = when (value) {
        is JsonArray -> JsonArray(value.map(::normalize))
        is JsonObject -> JsonObject(value.filterKeys { it !in setOf("fetchDate", "updated") }.mapValues { normalize(it.value) })
        else -> value
    }
}
