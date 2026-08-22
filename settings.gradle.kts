pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "letstroll"

include("processor")
include("core")
include("paper")
include("fabric")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.theroer.dev/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}
