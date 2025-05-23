pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "synapse"

val polyfillLibraryDir = file("../../polyfill/library")
if (polyfillLibraryDir.exists()) {
    include("polyfill")
    project(":polyfill").projectDir = polyfillLibraryDir
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val aviatorKoasDir = file("../../aviator/koas")
if (aviatorKoasDir.exists()) {
    include("koas")
    project(":koas").projectDir = aviatorKoasDir
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val aviatorCoreDir = file("../../aviator/core")
if (aviatorCoreDir.exists()) {
    include("core")
    project(":core").projectDir = aviatorCoreDir
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val aviatorKtorDir = file("../../aviator/clients/aviator-ktor")
if (aviatorKtorDir.exists()) {
    include("aviator:clients:ktor")
    project(":aviator:clients:ktor").projectDir = aviatorKtorDir
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val aviatorTKDir = file("../../aviator/plugins/translation-keys")
if (aviatorTKDir.exists()) {
    include("aviator:plugin:translation-keys")
    project(":aviator:plugin:translation-keys").projectDir = aviatorTKDir
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}
val aviatorInterception = file("../../aviator/plugins/interception")
if (aviatorInterception.exists()) {
    include("aviator:plugin:interception")
    project(":aviator:plugin:interception").projectDir = aviatorInterception
} else {
    println("This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}