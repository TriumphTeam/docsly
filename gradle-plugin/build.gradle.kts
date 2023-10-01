import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("docsly.base")
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    signing
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.gradle.dokka)
    compileOnly(libs.gradle.kotlin)
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
}

gradlePlugin {
    website.set("https://github.com/TriumphTeam/docsly")
    vcsUrl.set("https://github.com/TriumphTeam/docsly.git")

    plugins {
        create("Docsly") {
            id = "dev.triumphteam.docsly"
            displayName = "Docsly"
            description = "A plugin for easy setting up of Dokka to generate a JSON compatible with the Docsly app."
            tags.set(listOf("dokka", "kdocs", "javadocs", "json", "compatibility"))
            implementationClass = "dev.triumphteam.docsly.DocslyGradlePlugin"
        }
    }
}
