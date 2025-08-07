package cloud.mallne.dicentra.areaassist.synapse.statics

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import cloud.mallne.dicentra.areaassist.synapse.model.dto.APIServiceDTO
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceDefinitionGroupRule {
    Single, ServiceLocator, All;

    companion object {
        fun inferFrom() {

        }
        fun fromString(value: String): ServiceDefinitionGroupRule {
            return fromStringOrNull(value) ?: ServiceLocator
        }

        fun fromStringOrNull(value: String): ServiceDefinitionGroupRule? {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}