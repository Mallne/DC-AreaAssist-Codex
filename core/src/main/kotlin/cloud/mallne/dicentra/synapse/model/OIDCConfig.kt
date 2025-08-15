package cloud.mallne.dicentra.synapse.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OIDCConfig(
    val issuer: String,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String,
    @SerialName("token_endpoint")
    val tokenEndpoint: String,
    @SerialName("introspection_endpoint")
    val introspectionEndpoint: String,
    @SerialName("userinfo_endpoint")
    val userinfoEndpoint: String,
    @SerialName("end_session_endpoint")
    val endSessionEndpoint: String,
    @SerialName("frontchannel_logout_session_supported")
    val frontchannelLogoutSessionSupported: Boolean,
    @SerialName("frontchannel_logout_supported")
    val frontchannelLogoutSupported: Boolean,
    @SerialName("jwks_uri")
    val jwksUri: String,
    @SerialName("check_session_iframe")
    val checkSessionIfame: String,
    @SerialName("grant_types_supported")
    val grantTypesSupported: List<String> = emptyList(),
    @SerialName("acr_values_supported")
    val acrValuesSupported: List<String> = emptyList(),
    @SerialName("response_types_supported")
    val responseTypesSupported: List<String> = emptyList(),
    @SerialName("subject_types_supported")
    val subjectTypesSupported: List<String> = emptyList(),
    @SerialName("prompt_values_supported")
    val promptValuesSupported: List<String> = emptyList(),
    @SerialName("registration_endpoint")
    val registrationEndpoint: String,
    @SerialName("token_endpoint_auth_methods_supported")
    val tokenEndpointAuthMethodsSupported: List<String> = emptyList(),
    @SerialName("introspection_endpoint_auth_methods_supported")
    val introspectionEndpointAuthMethodsSupported: List<String> = emptyList(),
    @SerialName("claims_supported")
    val claimsSupported: List<String> = emptyList(),
    @SerialName("claim_types_supported")
    val claimTypesSupported: List<String> = emptyList(),
    @SerialName("claims_parameter_supported")
    val claimsParameterSupported: Boolean = false,
    @SerialName("scopes_supported")
    val scopesSupported: List<String> = emptyList(),
    @SerialName("request_parameter_supported")
    val requestParameterSupported: Boolean = false,
    @SerialName("request_uri_parameter_supported")
    val requestUriParameterSupported: Boolean = false,
    @SerialName("require_request_uri_registration")
    val requireRequestUriRegistration: Boolean = false,
    @SerialName("code_challenge_methods_supported")
    val codeChallengeMethodsSupported: List<String> = emptyList(),
    @SerialName("tls_client_certificate_bound_access_tokens")
    val tlsClientCertificateBoundAccessTokensSupported: Boolean = false,
    @SerialName("revocation_endpoint")
    val revocationEndpoint: String,
    @SerialName("revocation_endpoint_auth_methods_supported")
    val revocationEndpointAuthMethodsSupported: List<String> = emptyList(),
    @SerialName("backchannel_logout_supported")
    val backchannelLogoutSupported: Boolean = false,
    @SerialName("backchannel_logout_session_supported")
    val backchannelLogoutSessionSupported: Boolean = false,
    @SerialName("device_authorization_endpoint")
    val deviceAuthorizationEndpoint: String,
    @SerialName("backchannel_token_delivery_modes_supported")
    val backchannelTokenDeliveryModesSupported: List<String> = emptyList(),
    @SerialName("backchannel_authentication_endpoint")
    val backchannelAuthenticationEndpoint: String,
    @SerialName("require_pushed_authorization_requests")
    val requirePushedAuthorizationRequests: Boolean = false,
    @SerialName("pushed_authorization_request_endpoint")
    val pushedAuthorizationRequestEndpoint: String,
    @SerialName("authorization_response_iss_parameter_supported")
    val authorizationResponseIssParameterSupported: Boolean = false,
)
