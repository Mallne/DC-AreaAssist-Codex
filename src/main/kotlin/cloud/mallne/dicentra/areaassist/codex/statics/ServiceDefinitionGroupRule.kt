package cloud.mallne.dicentra.areaassist.codex.statics

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceDefinitionGroupRule {
    Single, ServiceLocator;

    companion object {

        fun fromString(value: String): ServiceDefinitionGroupRule {
            return fromStringOrNull(value) ?: ServiceLocator
        }

        fun fromStringOrNull(value: String): ServiceDefinitionGroupRule? {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}