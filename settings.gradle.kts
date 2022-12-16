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
).forEach {
    includeProject(it)
}

include("test-module")

fun includeProject(name: String) {
    include(name) {
        this.name = "${rootProject.name}-$name"
    }
}

fun include(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
