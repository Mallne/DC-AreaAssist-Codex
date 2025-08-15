plugins {
    alias(libs.plugins.kjvm)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

group = "cloud.mallne.dicentra.areaassist.codex"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    ksp(libs.koin.ksp)
    implementation(libs.dc.synapse.core)
}
