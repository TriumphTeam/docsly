dependencyResolutionManagement {
    includeBuild("build-logic")
    repositories.gradlePluginPortal()
}

rootProject.name = "doclopedia"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

listOf(
    "dokka-plugin",
).forEach(::includeProject)

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
