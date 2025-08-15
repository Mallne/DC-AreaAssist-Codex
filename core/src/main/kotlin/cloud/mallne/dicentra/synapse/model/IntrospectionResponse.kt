package cloud.mallne.dicentra.synapse.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IntrospectionResponse(
    val exp: Long,
    val iat: Long,
    @SerialName("auth_time")
    val authTime: Long,
    val iss: String,
    val aud: String,
    val typ: String,
    val sub: String,
    val sid: String,
    val scope: String,
    val acr: String,
    @SerialName("email_verified")
    val emailVerified: Boolean = false,
    val name: String,
    val groups: List<String>,
    @SerialName("preferred_username")
    val preferredUsername: String,
    val active: Boolean = false,
    @SerialName("token_type")
    val tokenType: String,
    val email: String,
    @SerialName("given_name")
    val givenName: String,
    @SerialName("family_name")
    val familyName: String,
    @SerialName("client_id")
    val clientId: String,
) {
    fun toUser(config: OAuthConfig, scopes: List<String>): User {
        val acl = User.AccessLevels(
            config.roles.user in groups,
            config.roles.admin in groups,
            config.roles.superAdmin in groups
        )
        val locked = config.enabled && !active && !emailVerified
        return User(name, email, preferredUsername, locked, acl, scopes)
    }
}