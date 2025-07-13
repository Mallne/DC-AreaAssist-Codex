package cloud.mallne.dicentra.areaassist.synapse.model

import cloud.mallne.dicentra.areaassist.synapse.statics.ResponseObject
import kotlinx.serialization.Serializable

@Serializable
@ResponseObject
@Deprecated("Make this also a ServiceDefinition")
data class OAuthConfigResponse(
    val issuer: String,
    val scopes: String,
    val clientId: String,
) {
    constructor(config: Configuration.Nested.SecurityConfiguration.Nested.SecurityAppConfiguration) : this(
        config.issuer,
        config.scopes,
        config.clientId
    )
}