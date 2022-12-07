import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    id("doclopedia.base-conventions")
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.json)
    implementation(libs.dokka.base)

    compileOnly(libs.dokka.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.dokka.api.test)
    testImplementation(libs.dokka.base.test)
}

val dokkaOutputDir = "$buildDir/dokka"

tasks {
    dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

java {
    withSourcesJar()
}
