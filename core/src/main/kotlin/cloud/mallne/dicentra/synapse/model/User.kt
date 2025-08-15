package cloud.mallne.dicentra.synapse.model

import cloud.mallne.dicentra.synapse.service.ScopeService
import cloud.mallne.dicentra.synapse.statics.Validation
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val email: String,
    val username: String,
    val locked: Boolean = false,
    val access: AccessLevels,
    private val dbScopes: List<String>
) {
    val valid
        get() = !locked && access.any()
    val scopes
        get() = dbScopes + userScope

    val userScope
        get() = ScopeService.user(username)

    @Serializable
    data class AccessLevels(
        val user: Boolean,
        val admin: Boolean,
        val superAdmin: Boolean,
    ) {
        fun any() = Validation.Bool.atLeastOf(1, this)
    }
}
