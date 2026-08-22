plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.runpaper)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.magicutils.bukkit)
    implementation(libs.bstats.bukkit)
    compileOnly(libs.paper.api)
    compileOnly(libs.netty.transport)
    annotationProcessor(project(":processor"))
}

val brandedTranslations = tasks.register("brandedTranslations") {
    val bundle = configurations.runtimeClasspath
    val metadata = rootProject.extra["langMetadata"]
    @Suppress("UNCHECKED_CAST")
    val values = metadata as Map<String, String>
    val output = layout.buildDirectory.file("generated-resources/lang/en.json")
    inputs.files(bundle)
    inputs.property("metadata", values)
    outputs.file(output)
    doLast {
        val source = bundle.get().first { it.name.startsWith("magicutils-") }
        val bundled = zipTree(source).matching { include("lang/en.json") }.singleFile.readText()
        val patched = values.entries.fold(bundled) { text, (key, value) ->
            text.replace(Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"[^\"]*\""), "\"$key\": \"$value\"")
        }
        val target = output.get().asFile
        target.parentFile.mkdirs()
        target.writeText(patched)
    }
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated-resources"))
}

runPaper.folia.registerTask {
    minecraftVersion(libs.versions.minecraft.get())
    runDirectory(file("run-folia"))
}

tasks {
    processResources {
        dependsOn(brandedTranslations)
        val values = mapOf("version" to project.version)
        inputs.properties(values)
        filesMatching("plugin.yml") {
            expand(values)
        }
    }

    shadowJar {
        archiveFileName.set("LetsTroll-paper-${libs.versions.minecraft.get()}-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
        exclude("paper-plugin.yml")
        exclude("META-INF/maven/**")
        relocate("org.bstats", "dev.delewer.letstroll.libs.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        val cached = rootProject.file(".cache/paper-${libs.versions.minecraft.get()}-132.jar")
        if (cached.isFile) {
            serverJar(cached)
        }
    }
}
