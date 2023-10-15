import org.gradle.internal.impldep.org.bouncycastle.asn1.x500.style.RFC4519Style.name

dependencyResolutionManagement {
    includeBuild("build-logic")
    repositories.gradlePluginPortal()
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "docsly"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

listOf(
    "dokka-plugin",
    "gradle-plugin",

    "serializable",
    "common",

    "app",
    "discord",

    "renderer/discord"
).forEach {
    includeProject(it)
}

include("test-module")

fun includeProject(path: String) {
    val name = path.replace("/", "-")
    include(name) {
        this.name = "${rootProject.name}-$name"
        this.projectDir = file(path)
    }
}

fun include(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
