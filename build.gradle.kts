plugins {
    alias(libs.plugins.kjvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.koin.compiler)
}

allprojects {
    apply {
        plugin("com.autonomousapps.dependency-analysis")
    }
}

group = "cloud.mallne.dicentra.areaassist.codex"
version = "0.0.1-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    //Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.annotations)
    //Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
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
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.openapi)
    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.rdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.migrations.r2dbc)
    // Database
    implementation(libs.postgres)
    implementation(libs.flyway)
    runtimeOnly(libs.flyway.pg)
    runtimeOnly(libs.postgres.jdbc)
    // Other
    implementation(libs.mcp)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.datetime)
    //aviator
    implementation(libs.dc.aviator.client.ktor)
    implementation(libs.dc.aviator.adapter.xml)
    implementation(libs.dc.aviator.adapter.json)
    implementation(libs.dc.aviator.plugin.interception)
    implementation(libs.dc.aviator.plugin.weaver)
    implementation(libs.dc.aviator.plugin.synapse)
    implementation(libs.dc.polyfill)
    implementation(libs.dc.areaassist.shared)
    implementation(libs.dc.synapse.core)
}

tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching("logback.xml") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}