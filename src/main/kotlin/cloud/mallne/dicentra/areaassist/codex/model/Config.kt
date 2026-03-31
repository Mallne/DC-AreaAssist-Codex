package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.synapse.helper.toBooleanish
import cloud.mallne.dicentra.synapse.model.Configuration
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.config.*

object Config {
    val Configuration.Nested.SecurityConfiguration.areaAssistClientId: String
        get() = application.environment.config.tryGetString("security.areaassist_client_id") ?: ""

    val Configuration.Nested.SecurityConfiguration.areaAssistClientName: String
        get() = application.environment.config.tryGetString("security.areaassist_client_name") ?: "Authentication"

    val Configuration.Nested.SecurityConfiguration.areaAssistAccConsole: String
        get() = application.environment.config.tryGetString("security.areaassist_account_console") ?: ""

    val Configuration.Nested.ServerConfiguration.autoReleaseVersion: Boolean
        get() = application.environment.config.tryGetString("server.auto_release_version")?.toBooleanish() ?: true

    fun getApplicationOIDCConfig(
        config: Configuration,
    ): OpenApiDoc {
        val issuer = config.security.oidcConfig.issuer
        val authorizationEndpoint = config.security.oidcConfig.authorizationEndpoint.replace(issuer, "")
        val tokenEndpoint = config.security.oidcConfig.tokenEndpoint.replace(issuer, "")
        val accountConsole = config.security.areaAssistAccConsole.replace(issuer, "")
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
