plugins {
    java
}

dependencies {
    compileOnly(libs.magicutils.bukkit)
    annotationProcessor(project(":processor"))

    testImplementation(libs.magicutils.bukkit)
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
