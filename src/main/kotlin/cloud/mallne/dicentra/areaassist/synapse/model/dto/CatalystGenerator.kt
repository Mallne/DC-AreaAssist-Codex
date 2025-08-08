package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import cloud.mallne.dicentra.areaassist.synapse.model.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.synapse.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.aviator.koas.typed.Route
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.aviator.plugin.synapse.SynapsePlugin
import cloud.mallne.dicentra.aviator.plugin.synapse.SynapsePluginConfig
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverServiceObject
import io.ktor.http.URLBuilder
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import java.net.URLEncoder

class CatalystGenerator(
    val configuration: Configuration,
    val json: Json = Serialization(),
) {
    fun generateFor(
        route: Route,
        useId: String? = null
    ): Pair<String, PathItem> {
        val rawOptions = route.`x-dicentra-aviator-serviceOptions`
        val rawLocator = route.`x-dicentra-aviator-serviceDelegateCall`
        val locator = rawLocator?.let { ServiceLocator(it) } ?: throw IllegalArgumentException("Service Locator is required")
        //filter out potential weaver translation schema
        val options = if (rawOptions != null) {
            try {
                AviatorServiceUtils.optionBundle<WeaverServiceObject>(rawOptions)
                rawOptions.jsonObject.filterNot { it.key == "x-dicentra-weaver-schema" } as ServiceOptions
            } catch (_: IllegalArgumentException) {
                rawOptions
            }
        } else InflatedServiceOptions.empty.usable()
        val queryParams = if (useId != null) "?id=$useId" else "?locator=${locator.toString().encodeURLParameter()}"
        val rawPlugins = route.`x-dicentra-aviator-pluginMaterialization` ?: mapOf()
        val plugins = rawPlugins + mapOf(
            SynapsePlugin.identity to json.encodeToJsonElement(SynapsePluginConfig(true))
        )
        return "/catalyst$queryParams" to PathItem(
                    post = Operation(
                        extensions = mapOf(
                            AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                            AviatorExtensionSpec.ServiceOptions.O.key to options,
                            AviatorExtensionSpec.PluginMaterialization.O.key to json.encodeToJsonElement(plugins)
                        ),
                    )
        )
    }

    fun generateWrapper(paths: List<Pair<String, PathItem>>): OpenAPI {
        val schema = if (configuration.catalyst.tlsEnabled) "https://" else "http://"
        return OpenAPI(
            extensions = mapOf(
                AviatorExtensionSpec.Version.key to json.parseToJsonElement(
                    AviatorExtensionSpec.SpecVersion
                )
            ),
            servers = listOf(
                Server(
                    url = schema + configuration.catalyst.serverName
                )
            ),
            info = Info(
                title = configuration.catalyst.title,
                description = configuration.catalyst.description,
                version = AviatorExtensionSpec.SpecVersion
            ),
            paths = paths.toMap()
        )
    }
}