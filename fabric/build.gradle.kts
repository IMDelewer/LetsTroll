plugins {
    java
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
}

val bundle = configurations.create("bundle")

repositories {
    mavenCentral()
    maven("https://maven.theroer.dev/releases")
    maven("https://maven.fabricmc.net/")
}

loom {
    accessWidenerPath.set(file("src/main/resources/letstroll.accesswidener"))
}

val magicBundle = "dev.ua.theroer:magicutils-fabric-bundle:1.27.4+java21"

dependencies {
    minecraft(libs.minecraft)
    mappings("net.fabricmc:yarn:1.21.11+build.6:v2")
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    modImplementation(magicBundle)
    include(magicBundle)
    modCompileOnly("$magicBundle:dev")
    modRuntimeOnly("$magicBundle:dev")

    implementation(project(":core"))
    bundle(project(":core")) {
        isTransitive = false
    }
}

tasks {
    processResources {
        val values = mapOf("version" to project.version)
        inputs.properties(values)
        filesMatching("fabric.mod.json") {
            expand(values)
        }
    }

    shadowJar {
        configurations = listOf(bundle)
        archiveClassifier.set("dev-shadow")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveFileName.set("LetsTroll-fabric-${libs.versions.minecraft.get()}-${project.version}.jar")
    }
}
