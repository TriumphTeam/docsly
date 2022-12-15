plugins {
    id("doclopedia.base-conventions")
    id("com.gradle.plugin-publish") version "1.0.0"
    `java-gradle-plugin`
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

gradlePlugin {
    website.set("https://github.com/TriumphTeam/doclopedia")
    vcsUrl.set("https://github.com/TriumphTeam/doclopedia.git")

    plugins {
        create("doclopedia") {
            id = "dev.triumphteam.doclopedia"
            displayName = "Doclopedia"
            description = "A plugin for easy setting up of Dokka to generate a JSON compatible with the app."
            tags.set(listOf("dokka", "kdocs", "javadocs", "json", "compatibility"))
            implementationClass = "dev.triumphteam.DoclopediaGradlePlugin"
        }
    }
}
