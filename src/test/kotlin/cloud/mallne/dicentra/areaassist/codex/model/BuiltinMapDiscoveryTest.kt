package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.api.MapDark
import cloud.mallne.dicentra.areaassist.statics.api.MapLight
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.synapse.model.Catalyst
import cloud.mallne.dicentra.synapse.model.Database
import cloud.mallne.dicentra.synapse.model.Security
import cloud.mallne.dicentra.synapse.model.SecurityGroups
import cloud.mallne.dicentra.synapse.model.Server
import cloud.mallne.dicentra.synapse.model.ServerCors
import cloud.mallne.dicentra.synapse.model.SynapseConfig
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO.Companion.transform
import cloud.mallne.dicentra.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.synapse.statics.Serialization
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuiltinMapDiscoveryTest {
    private val generator = CatalystGenerator(
        SynapseConfig(
            security = Security(groups = SecurityGroups()),
            data = Database(),
            server = Server(cors = ServerCors()),
            catalyst = Catalyst(),
        ),
    )

    @Test
    fun `Catalyst discovery preserves native map URLs and complete options`() {
        val maps = listOf(MapLight.value, MapDark.value)
        val client = HttpClient(MockEngine { error("Discovery conversion must not perform network requests") })
        try {
            val converter = KtorAviatorServiceConverter(client, Serialization())
            val result = maps.map { it.asBuiltinService() }.transform(
                requestedTransformationType = ServiceDefinitionTransformationType.Catalyst,
                catalystGenerator = generator,
            )
            // No synthetic map action: original definitions retain URLs, options and separate themes.
            val nativeMaps = result.filter { it in maps }
            assertEquals(maps, nativeMaps)
            val gateway = result.first()
            val mapLocator = APIs.Services.MAPLAYER.locator(ServiceMethods.GATHER)
            assertFalse(AviatorServiceUtils.extractServiceLocators(gateway).any { it.first == mapLocator })
            for (definition in nativeMaps) {
                val service = converter.build(definition).single()
                val options = service.optionBundle<MapStyleServiceOptions>()
                assertEquals("Roboto Regular", options.mapFont)
                assertTrue(options.extraSources.isNotEmpty())
                val originalUrl = definition.servers.orEmpty().single().url + definition.paths.orEmpty().keys.single()
                assertEquals(listOf(originalUrl), AviatorServiceUtils.catchPaths(service))
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun `parcel services remain Catalyst capable`() {
        val parcel = APIs.esri.asBuiltinService()
        assertTrue(parcel.builtin)
        assertTrue(parcel.catalystTransformable)
        assertEquals(ServiceDefinitionTransformationType.Catalyst, parcel.transformOrNull(generator.configuration, ServiceDefinitionTransformationType.Catalyst))
    }
}
