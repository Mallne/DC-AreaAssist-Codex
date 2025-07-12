plugins {
    alias(libs.plugins.kjvm)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

group = "cloud.mallne.dicentra.areaassist"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.datetime)
    implementation(libs.postgres)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.netty)
    implementation(libs.mcp)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    //aviator
    if (findProject(":aviator:clients:ktor") != null) {
        implementation(project(":aviator:clients:ktor"))
    } else {
        implementation(libs.dc.aviator.clients.ktor)
    }
    if (findProject(":aviator:plugin:translation-keys") != null) {
        implementation(project(":aviator:plugin:translation-keys"))
    } else {
        implementation(libs.dc.aviator.plugins.translationkeys)
    }
    if (findProject(":aviator:plugin:interception") != null) {
        implementation(project(":aviator:plugin:interception"))
    } else {
        implementation(libs.dc.aviator.plugins.interception)
    }
}
