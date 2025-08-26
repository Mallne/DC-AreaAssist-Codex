package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.io.MediaType
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter
import cloud.mallne.dicentra.aviator.koas.parameters.RequestBody
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.synapse.model.Configuration
import io.ktor.http.*
import io.ktor.server.config.*

object Config {
    val Configuration.Nested.SecurityConfiguration.areaAssistClientId: String
        get() = application.environment.config.tryGetString("security.areaassist_client_id") ?: ""

    val Configuration.Nested.SecurityConfiguration.areaAssistClientName: String
        get() = application.environment.config.tryGetString("security.areaassist_client_name") ?: "Authentication"

    val Configuration.Nested.SecurityConfiguration.areaAssistAccConsole: String
        get() = application.environment.config.tryGetString("security.areaassist_account_console") ?: ""

    fun getApplicationOIDCConfig(
        config: Configuration,
    ): OpenAPI {
        val issuer = config.security.oidcConfig.issuer
        val authorizationEndpoint = config.security.oidcConfig.authorizationEndpoint.replace(issuer, "")
        val tokenEndpoint = config.security.oidcConfig.tokenEndpoint.replace(issuer, "")
        val accountConsole = config.security.areaAssistAccConsole.replace(issuer, "")
        return OpenAPI(
            extensions = mapOf(
                AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                    AviatorExtensionSpec.SpecVersion
                )
            ),
            servers = listOf(
                Server(issuer)
            ),
            info = Info(
                title = config.security.areaAssistClientName,
                description = "The OAuth2/OIDC Server used for Authentication",
                version = AviatorExtensionSpec.SpecVersion
            ),
            paths = mapOf(
                accountConsole to PathItem(
                    get = Operation(
                        operationId = "AuthenticationAccountConsole",
                        extensions = mapOf(
                            AviatorExtensionSpec.ServiceLocator.O.key to APIs.Services.AUTH_ACCOUNT.locator(
                                ServiceMethods.GATHER
                            ).usable(),
                            AviatorExtensionSpec.ServiceOptions.O.key to AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                        ),
                    )
                ),
                authorizationEndpoint to PathItem(
                    get = Operation(
                        operationId = "AuthenticationAuthorizationEndpoint",
                        extensions = mapOf(
                            AviatorExtensionSpec.ServiceLocator.O.key to APIs.Services.AUTH_AUTHORIZATION.locator(
                                ServiceMethods.GATHER
                            ).usable(),
                            AviatorExtensionSpec.ServiceOptions.O.key to AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                        ),
                        parameters = listOf(
                            ReferenceOr.value(Parameter(
                                name = APIs.OAuth2.CLIENT_ID,
                                input = Parameter.Input.Query,
                                schema = ReferenceOr.schema("StringPrimitive")
                            )),
                            ReferenceOr.value(Parameter(
                                name = APIs.OAuth2.REDIRECT_URI,
                                input = Parameter.Input.Query,
                                schema = ReferenceOr.schema("StringPrimitive")
                            )),
                                    ReferenceOr.value(Parameter(
                                name = APIs.OAuth2.STATE,
                                input = Parameter.Input.Query,
                                        schema = ReferenceOr.schema("StringPrimitive")
                            )),
                                    ReferenceOr.value(Parameter(
                                name = APIs.OAuth2.RESPONSE_TYPE,
                                input = Parameter.Input.Query,
                                        schema = ReferenceOr.schema("StringPrimitive")
                            ))
                        ),
                    )
                ),
                tokenEndpoint to PathItem(
                    post = Operation(
                        operationId = "AuthenticationTokenEndpoint",
                        extensions = mapOf(
                            AviatorExtensionSpec.ServiceLocator.O.key to APIs.Services.AUTH_TOKEN.locator(
                                ServiceMethods.GATHER
                            ).usable(),
                            AviatorExtensionSpec.ServiceOptions.O.key to AuthServiceOptions(
                                clientId = config.security.areaAssistClientId
                            ).usable()
                        ),
                        requestBody = ReferenceOr.value(
                            RequestBody(
                                content = mapOf(
                                    ContentType.Application.FormUrlEncoded.toString() to MediaType(
                                        schema = ReferenceOr.value(
                                            Schema(
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
                ),
            ),
            components = Components(
                schemas = mapOf(
                    "StringPrimitive" to ReferenceOr.value(Schema(type = Schema.Type.Basic.String)),
                )
            )
        )
    }
}
