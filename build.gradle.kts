plugins {
    alias(libs.plugins.kjvm)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.dependency.analysis)
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
    ksp(libs.koin.ksp)
    implementation(libs.dc.synapse.core)
    implementation(libs.postgres)
}
