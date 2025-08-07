package cloud.mallne.dicentra.areaassist.synapse.model.dto

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import cloud.mallne.dicentra.areaassist.synapse.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.aviator.koas.typed.Route
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverServiceObject
import kotlinx.serialization.json.Json

class CatalystGenerator(
    val configuration: Configuration,
    val json: Json = Serialization(),
) {
    fun generateFor(
        serviceLocator: ServiceLocator,
        route: Route
    ) {
        val schema = if (configuration.catalyst.tlsEnabled) "https://" else "http://"
        val options = route.`x-dicentra-aviator-serviceOptions`
        if (options != null) {
            try {
                val bndl = AviatorServiceUtils.optionBundle<WeaverServiceObject>(options)
            } catch (_: IllegalArgumentException) {

            }
        }
        val catalyst = OpenAPI(
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
            paths = mapOf(
                "/catalyst" to PathItem(
                    get = Operation(
                        extensions = mapOf(
                            AviatorExtensionSpec.ServiceLocator.O.key to serviceLocator.usable(),
                            AviatorExtensionSpec.ServiceOptions.O.key to InflatedServiceOptions.empty.usable()
                        ),
                    )
                ),
            ),
        )
    }
}