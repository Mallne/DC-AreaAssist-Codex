plugins {
    alias(libs.plugins.kjvm)
    alias(libs.plugins.android.library) apply false
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
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.dc.areaassist.shared)
    implementation(libs.dc.synapse.core)
    runtimeOnly(libs.postgres)
}

tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}