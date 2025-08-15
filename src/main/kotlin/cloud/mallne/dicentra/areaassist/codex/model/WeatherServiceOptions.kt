package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.synapse.statics.Serialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class WeatherServiceOptions(
    val serviceType: ServiceType
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)

    companion object {
        @Serializable
        enum class ServiceType {
            BRIGHTSKY
        }
    }
}