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

rootProject.name = "Codex"

val aviatorDir = file("../../aviator")
if (aviatorDir.exists()) {
    includeBuild(aviatorDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.aviator.plugin:interception")).using(project(":plugins:interception"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin.adapter:adapter-xml")).using(project(":plugins:adapter-xml"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin.adapter:adapter-json")).using(project(":plugins:adapter-json"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:weaver")).using(project(":plugins:weaver"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:synapse")).using(project(":plugins:synapse"))
            substitute(module("cloud.mallne.dicentra.aviator.client:ktor")).using(project(":clients:ktor"))
            substitute(module("cloud.mallne.dicentra.aviator:koas")).using(project(":koas"))
            substitute(module("cloud.mallne.dicentra.aviator:core")).using(project(":core"))
        }
    }
} else {
    println("[AREAASSIST_CODEX:aviator] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}

val synapseDir = file("../../synapse")
if (synapseDir.exists()) {
    includeBuild(synapseDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.synapse:core")).using(project(":core"))
        }
    }
} else {
    println("[AREAASSIST_CODEX:synapse] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}

val sharedDir = file("../shared")
if (sharedDir.exists()) {
    includeBuild(sharedDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.areaassist:shared")).using(project(":"))
        }
    }
} else {
    println("[AREAASSIST_CODEX:shared] This Project seems to be running without the Monorepo Context, please consider using the Monorepo")
}

