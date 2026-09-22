pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven {
            url = uri("https://registry.mallne.cloud/repository/DiCentraArtefacts/")
            credentials {
                username = providers.environmentVariable("NEXUS_USERNAME").getOrElse("")
                password = providers.environmentVariable("NEXUS_PASSWORD").getOrElse("")
            }
            content {
                includeGroupByRegex("cloud\\.mallne\\..*")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Codex"

val monorepoRoot = file("../../..")
val isStandalone = !file("../../aviator/settings.gradle.kts").isFile

if (isStandalone) {
    println("[AREAASSIST_CODEX] Running in standalone mode -- monorepo includes disabled, dependencies will be resolved from Nexus")
} else {
    val aviatorDir = file("../../aviator")
    includeBuild(aviatorDir.absolutePath) {
        dependencySubstitution {
            substitute(module("cloud.mallne.dicentra.aviator.plugin:interception")).using(project(":plugins:interception"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:otel")).using(project(":plugins:otel"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin.adapter:adapter-xml")).using(project(":plugins:adapter-xml"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin.adapter:adapter-json")).using(project(":plugins:adapter-json"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:weaver")).using(project(":plugins:weaver"))
            substitute(module("cloud.mallne.dicentra.aviator.plugin:synapse")).using(project(":plugins:synapse"))
            substitute(module("cloud.mallne.dicentra.aviator.client:ktor")).using(project(":clients:ktor"))
            substitute(module("cloud.mallne.dicentra.aviator.client:mock")).using(project(":clients:mock"))
            substitute(module("cloud.mallne.dicentra.aviator:koas")).using(project(":koas"))
            substitute(module("cloud.mallne.dicentra.aviator:core")).using(project(":core"))
        }
    }

    val synapseDir = file("../../synapse")
    if (synapseDir.exists()) {
        includeBuild(synapseDir.absolutePath) {
            dependencySubstitution {
                substitute(module("cloud.mallne.dicentra.synapse:core")).using(project(":core"))
            }
        }
    }

    val sharedDir = file("../shared")
    if (sharedDir.exists()) {
        includeBuild(sharedDir.absolutePath) {
            dependencySubstitution {
                substitute(module("cloud.mallne.dicentra.areaassist:shared")).using(project(":"))
            }
        }
    }
}
