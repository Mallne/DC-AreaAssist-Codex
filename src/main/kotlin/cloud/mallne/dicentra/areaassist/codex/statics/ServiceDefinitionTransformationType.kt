package cloud.mallne.dicentra.areaassist.codex.statics

import cloud.mallne.dicentra.synapse.model.Configuration
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceDefinitionTransformationType {
    Auto, Native, Catalyst;

    fun distill(
        config: Configuration,
        preferred: ServiceDefinitionTransformationType = config.preferredTransform,
    ): ServiceDefinitionTransformationType? {
        val finalType = if (this == Auto) if (preferred == Auto) config.preferredTransform else preferred else this
        return if (finalType == Auto) null else finalType
    }

    fun canUse(
        config: Configuration,
        preferred: ServiceDefinitionTransformationType = config.preferredTransform,
        native: Boolean = true,
        catalyst: Boolean = config.catalyst.enabled,
    ): Boolean {
        val finalType = distill(config, preferred)
        return when (finalType) {
            Native -> {
                native
            }

            Catalyst -> {
                catalyst && config.catalyst.enabled
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