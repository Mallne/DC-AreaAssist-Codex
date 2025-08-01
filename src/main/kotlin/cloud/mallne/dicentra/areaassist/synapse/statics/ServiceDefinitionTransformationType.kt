package cloud.mallne.dicentra.areaassist.synapse.statics

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceDefinitionTransformationType {
    Auto, Native, Catalyst, CatalystAggregate;

    companion object {
        fun fromString(value: String): ServiceDefinitionTransformationType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: Auto
        }
    }
}