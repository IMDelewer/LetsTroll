plugins {
    java
}

extra["langMetadata"] = mapOf(
    "language.name" to "English",
    "language.code" to "en",
    "language.author" to "IMDelewer",
    "language.version" to "1.0"
)

val projectVersion = (findProperty("buildVersion") as String?)?.takeIf { it.isNotBlank() } ?: "2.1.0"

subprojects {
    apply(plugin = "java")

    group = "dev.delewer"
    version = projectVersion

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.add("-Xlint:all,-classfile,-processing")
    }

    tasks.withType<Jar>().configureEach {
        manifest {
            attributes(
                "Implementation-Title" to "LetsTroll",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "IMDelewer"
            )
        }
    }
}
