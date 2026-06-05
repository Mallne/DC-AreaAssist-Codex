package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.synapse.model.*
import cloud.mallne.dicentra.synapse.model.Server
import cloud.mallne.dicentra.synapse.statics.ServiceDefinitionTransformationType
import io.ktor.http.*
import io.ktor.openapi.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class CodexServer(
    val cors: ServerCors,
    val hostname: String = "0.0.0.0",
    @SerialName("tls_enabled")
    val tlsEnabled: Boolean = true,
    val info: String = "DiCentra Synapse",
    val description: String = "A discovery endpoint for Aviator services.",
    @SerialName("base_locator")
    val baseLocator: String = "synapse",
    @SerialName("discovery_exclusions")
    val discoveryExclusions: List<String> = listOf(),
    @SerialName("auto_release_version")
    val autoReleaseVersion: Boolean = true,
)

@Serializable
data class CodexSecurity(
    val enabled: Boolean = false,
    val issuer: String = "",
    val authorizationEndpoint: String = "$issuer/protocol/openid-connect/auth",
    val tokenEndpoint: String = "$issuer/protocol/openid-connect/token",
    val introspectionEndpoint: String = "$issuer/protocol/openid-connect/introspect",
    val scopes: String = "",
    @SerialName("client_id")
    val clientId: String = "",
    @SerialName("client_secret")
    val clientSecret: String = "",
    val groups: SecurityGroups,
    @SerialName("area_assist_client_id")
    val areaAssistClientId: String = "",
    @SerialName("areaassist_client_name")
    val areaAssistClientName: String = "Authentication",
    @SerialName("areaassist_account_console")
    val areaAssistAccountConsole: String = "",
) {
    @OptIn(ExperimentalEncodingApi::class)
    fun encodedCredentials() = Base64.encode("$clientId:$clientSecret".toByteArray())
}

@Serializable
data class CodexConfig(
    val security: CodexSecurity,
    val data: Database,
    val server: CodexServer,
    val catalyst: Catalyst,
    val preferredTransform: ServiceDefinitionTransformationType = ServiceDefinitionTransformationType.Native
)

object Config {
    fun getApplicationOIDCConfig(
        config: CodexConfig,
    ): OpenApiDoc {
        val issuer = config.security.issuer
        val authorizationEndpoint = config.security.authorizationEndpoint.replace(issuer, "")
        val tokenEndpoint = config.security.tokenEndpoint.replace(issuer, "")
        val accountConsole = config.security.areaAssistAccountConsole.replace(issuer, "")
        return OpenApiDoc.build {
            `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
            servers {
                server(issuer)
            }
            info = OpenApiInfo(
                title = config.security.areaAssistClientName,
                description = "The OAuth2/OIDC Server used for Authentication",
                version = AviatorExtensionSpec.SpecVersion
            )
            components = Components(
                schemas = mapOf(
                    "StringPrimitive" to JsonSchema(type = JsonType.STRING)
                )
            )
        }.copy(
            paths = mapOf(
                accountConsole to ReferenceOr.value(
                    PathItem(
                        get = Operation.build {
                            operationId = "AuthenticationAccountConsole"
                            `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.AUTH_ACCOUNT.locator(
                                ServiceMethods.GATHER
                            )
                            `x-dicentra-aviator-serviceOptions` = AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                        }
                    )),
                authorizationEndpoint to ReferenceOr.value(
                    PathItem(
                        get = Operation.build {
                            operationId = "AuthenticationAuthorizationEndpoint"
                            `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.AUTH_AUTHORIZATION.locator(
                                ServiceMethods.GATHER
                            )
                            `x-dicentra-aviator-serviceOptions` = AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                            parameters {
                                query(APIs.OAuth2.CLIENT_ID) {
                                    schema = JsonSchema(type = JsonType.STRING)
                                }
                                query(APIs.OAuth2.REDIRECT_URI) {
                                    schema = JsonSchema(type = JsonType.STRING)
                                }
                                query(APIs.OAuth2.STATE) {
                                    schema = JsonSchema(type = JsonType.STRING)
                                }
                                query(APIs.OAuth2.RESPONSE_TYPE) {
                                    schema = JsonSchema(type = JsonType.STRING)
                                }
                            }
                        }
                    )),
                tokenEndpoint to ReferenceOr.value(
                    PathItem(
                        post = Operation.build {
                            operationId = "AuthenticationTokenEndpoint"
                            `x-dicentra-aviator-serviceDelegateCall` =
                                APIs.Services.AUTH_TOKEN.locator(ServiceMethods.GATHER)
                            `x-dicentra-aviator-serviceOptions` = AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                        }.copy(
                            requestBody = ReferenceOr.value(
                                RequestBody(
                                    content = mapOf(
                                        ContentType.Application.FormUrlEncoded to MediaType(
                                            schema = ReferenceOr.value(
                                                JsonSchema(
                                                    properties = mapOf(
                                                        APIs.OAuth2.CODE to ReferenceOr.schema("StringPrimitive"),
                                                        APIs.OAuth2.CLIENT_ID to ReferenceOr.schema("StringPrimitive"),
                                                        APIs.OAuth2.REDIRECT_URI to ReferenceOr.schema("StringPrimitive"),
                                                        APIs.OAuth2.GRANT_TYPE to ReferenceOr.schema("StringPrimitive"),
                                                        APIs.OAuth2.REFRESH_TOKEN to ReferenceOr.schema("StringPrimitive"),
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
            ),
        )
    }
}
