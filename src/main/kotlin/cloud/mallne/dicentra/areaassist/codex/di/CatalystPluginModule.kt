package cloud.mallne.dicentra.areaassist.codex.di

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.dicentra.synapse.service.CatalystPluginContribution
import cloud.mallne.dicentra.synapse.service.CatalystParameterContract
import cloud.mallne.dicentra.synapse.service.CatalystActionFilter
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystResultCodec
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystParameterValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.dsl.module
import org.koin.core.qualifier.named

private class EsriCatalystContribution : CatalystPluginContribution {
    override val identity = EsriAdapterPlugin.identity
    override val parameterCodecs: List<KSerializer<*>> = listOf(Boundary.serializer(), Query.serializer(), Boolean.serializer())
    override val parameterContracts = areaAssistParameterContracts()
    override val resultCodecs = listOf(AreaAssistParcelResultCodec)
    override fun create(): AviatorPluginInstance = EsriAdapterPlugin.install { }
}

private class WfsCatalystContribution(private val registry: CrsRegistry) : CatalystPluginContribution {
    override val identity = WfsAdapterPlugin.IDENTITY
    override val handlesRequestBody = true
    override val parameterCodecs: List<KSerializer<*>> = listOf(Boundary.serializer(), Query.serializer(), Boolean.serializer())
    override val parameterContracts = areaAssistParameterContracts()
    override val resultCodecs = listOf(AreaAssistParcelResultCodec)
    override fun create(): AviatorPluginInstance = WfsAdapterPlugin(registry).install { }
}

private fun areaAssistParameterContracts() = listOf(
    CatalystParameterContract(
        AreaAssistParameters.BOUNDARY,
        CatalystParameterValue.Cardinality.SINGLE,
        Boundary.serializer(),
    ),
    CatalystParameterContract(
        AreaAssistParameters.QUERIES,
        CatalystParameterValue.Cardinality.MULTI,
        Query.serializer(),
    ),
    CatalystParameterContract(
        AreaAssistParameters.RETURN_GEOMETRY,
        CatalystParameterValue.Cardinality.SINGLE,
        Boolean.serializer(),
    ),
)

private object AreaAssistParcelResultCodec : CatalystResultCodec {
    override val identity: String = "areaassist:parcel-crate-list"
    private val serializer = ListSerializer(ParcelCrateEntity.serializer())

    override fun supports(value: Any): Boolean =
        value is List<*> && value.all { it is ParcelCrateEntity }

    @Suppress("UNCHECKED_CAST")
    override fun encode(value: Any, json: Json): JsonElement {
        require(supports(value)) { "Unsupported value for AreaAssist parcel result codec" }
        return json.encodeToJsonElement(serializer, value as List<ParcelCrateEntity>)
    }
}

/** AreaAssist-specific plugin/codec contributions; Synapse core remains provider-agnostic. */
val CatalystPluginModule = module {
    single { CrsRegistry() }
    single<CatalystActionFilter> { AreaAssistCatalystActionFilter() }
    single<CatalystPluginContribution>(named(EsriAdapterPlugin.identity)) { EsriCatalystContribution() }
    single<CatalystPluginContribution>(named(WfsAdapterPlugin.IDENTITY)) { WfsCatalystContribution(get()) }
}
