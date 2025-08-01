package cloud.mallne.dicentra.areaassist.synapse.statics

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceDefinitionTransformationType {
    Auto, Native, Catalyst, CatalystAggregate;

    fun canUseTransform(
        config: Configuration,
        native: Boolean = true,
        catalyst: Boolean = config.catalyst.enabled,
        aggregate: Boolean = config.catalyst.aggregation.enabled,
    ): Boolean {
        val finalType = if (this == Auto) config.preferredTransform else this
        return when (finalType) {
            Native -> {
                native
            }

            Catalyst -> {
                catalyst && config.catalyst.enabled
            }

            CatalystAggregate -> {
                aggregate && config.catalyst.aggregation.enabled
            }

            else -> false
        }
    }

    companion object {
        fun fromString(value: String): ServiceDefinitionTransformationType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: Auto
        }
    }
}