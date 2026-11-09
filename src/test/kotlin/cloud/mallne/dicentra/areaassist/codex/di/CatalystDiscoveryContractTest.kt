package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.plugin.synapse.aggregateContracts
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.synapse.model.Catalyst
import cloud.mallne.dicentra.synapse.model.Database
import cloud.mallne.dicentra.synapse.model.Security
import cloud.mallne.dicentra.synapse.model.SecurityGroups
import cloud.mallne.dicentra.synapse.model.Server
import cloud.mallne.dicentra.synapse.model.ServerCors
import cloud.mallne.dicentra.synapse.model.SynapseConfig
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.synapse.model.dto.APIServiceDTO.Companion.transform
import cloud.mallne.dicentra.synapse.service.CatalystGenerator
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpMethod
import io.ktor.openapi.OpenApiDoc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalystDiscoveryContractTest {
    @Test
    fun `parcel and weather aggregates parse together without inline union naming failures`() {
        val json = Serialization()
        val sources = listOf(APIs.hessenWfs, APIs.thueringenWfs, APIs.brightSky, APIs.openMeteo)
            .mapIndexed { index, definition -> APIServiceDTO(id = "provider-$index", serviceDefinition = definition) }
        val generator = CatalystGenerator(SynapseConfig(
            security = Security(groups = SecurityGroups()), data = Database(),
            server = Server(cors = ServerCors()), catalyst = Catalyst(),
        ), json)
        val discovered = sources.transform(ServiceDefinitionTransformationType.Catalyst, catalystGenerator = generator).single()
        val restored = json.decodeFromString<OpenApiDoc>(json.encodeToString(OpenApiDoc.serializer(), discovered))
        val client = HttpClient(MockEngine { error("Discovery parsing must not make a network request") })
        try {
            val services = KtorAviatorServiceConverter(client, Serialization.serializers()).build(restored)
            assertTrue(services.size >= 4)
            assertTrue(services.all { it.route.method == HttpMethod.Post })
            assertTrue(services.any { service -> service.aggregateContracts.map { it.identity }.containsAll(listOf("provider-2", "provider-3")) })
        } finally {
            client.close()
        }
    }

    @Test
    fun `real Hessen and Thueringen specs retain native metadata through one parsed Catalyst service`() {
        val json = Serialization()
        val sources = listOf(APIs.hessenWfs, APIs.thueringenWfs).mapIndexed { index, definition ->
            APIServiceDTO(id = "provider-$index", serviceDefinition = definition)
        }
        val generator = CatalystGenerator(SynapseConfig(
            security = Security(groups = SecurityGroups()),
            data = Database(),
            server = Server(cors = ServerCors()),
            catalyst = Catalyst(),
        ), json)
        val discovered = sources.transform(ServiceDefinitionTransformationType.Catalyst, catalystGenerator = generator)
        assertEquals(1, discovered.size)
        val restored = json.decodeFromString<OpenApiDoc>(json.encodeToString(OpenApiDoc.serializer(), discovered.single()))
        val client = HttpClient(MockEngine { error("Metadata inspection must not make a network request") })
        try {
            val converter = KtorAviatorServiceConverter(client, Serialization.serializers())
            val gateway = converter.build(restored).single()
            assertEquals(HttpMethod.Post, gateway.route.method)
            assertEquals(2, gateway.aggregateContracts.size)
            gateway.aggregateContracts.forEach { contract ->
                val source = sources.single { it.id == contract.identity }
                val native = converter.build(source.serviceDefinition).single()
                val options = AviatorServiceUtils.optionBundle<ParcelServiceOptions>(assertNotNull(contract.options))
                assertEquals(native.optionBundle<ParcelServiceOptions>(), options)
                assertEquals(source.serviceDefinition.info, contract.info)
                assertEquals(source.serviceDefinition.externalDocs, contract.externalDocs)
                assertNotNull(options.keys.single { it.identifier == "area" }.format.unit)
            }
        } finally {
            client.close()
        }
    }
}
