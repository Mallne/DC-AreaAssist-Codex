package cloud.mallne.dicentra.areaassist.synapse.model

import cloud.mallne.dicentra.areaassist.synapse.statics.Validation
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val email: String,
    val username: String,
    val locked: Boolean = false,
    val access: AccessLevels,
) {
    val valid
        get() = !locked && access.any()

    @Serializable
    data class AccessLevels(
        val user: Boolean,
        val superAdmin: Boolean,
    ) {
        fun any() = Validation.Bool.atLeastOf(1, this)
    }
}
