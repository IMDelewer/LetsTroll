pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
        mavenCentral()
    }
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
