package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.synapse.service.CatalystActionFilter
import cloud.mallne.dicentra.synapse.service.CatalystPluginContribution
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystParameterCodecs
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.aviator.plugin.synapse.toCatalystParameters
import cloud.mallne.dicentra.aviator.plugin.synapse.toRequestParameters
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.coordinates.CrsRegistry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalystPluginModuleTest {
    @Test
    fun `areaassist runtime composition provides filter ESRI and WFS factories and codecs`() {
        val application = koinApplication { modules(CatalystPluginModule) }
        try {
            val koin = application.koin
            assertNotNull(koin.get<CrsRegistry>())
            assertEquals(listOf("areaassist:parcel-location"), koin.getAll<CatalystActionFilter>().map { it.identity })

            val contributions = koin.getAll<CatalystPluginContribution>().associateBy { it.identity }
            assertTrue(contributions.containsKey(EsriAdapterPlugin.identity))
            assertTrue(contributions.containsKey(WfsAdapterPlugin.IDENTITY))
            val esri = requireNotNull(contributions[EsriAdapterPlugin.identity])
            val wfs = requireNotNull(contributions[WfsAdapterPlugin.IDENTITY])
            assertTrue(esri.parameterCodecs.isNotEmpty())
            assertTrue(wfs.parameterCodecs.isNotEmpty())
            assertEquals(1, esri.resultCodecs.size)
            assertEquals(1, wfs.resultCodecs.size)
            esri.create()
            wfs.create()
        } finally {
            application.close()
        }
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
