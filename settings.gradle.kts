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

val aviatorDir = file("../../aviator")
if (aviatorDir.exists()) {
    includeBuild(aviatorDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.aviator.plugin:interception")).using(project(":plugins:interception"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:translation-keys")).using(project(":plugins:translation-keys"))
            substitute(module("cloud.mallne.dicentra.aviator.client:ktor")).using(project(":clients:ktor"))
        }
    }
} else {
    println("[AREAASSIST:aviator] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}