import dev.triumphteam.root.project
import dev.triumphteam.root.single

dependencyResolutionManagement {
    includeBuild("build-logic")
    repositories.gradlePluginPortal()
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.triumphteam.dev/releases")
    }
}

rootProject.name = "docsly"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("dev.triumphteam.root.settings") version "0.0.21"
}

project(
    namespace = "renderer",
    groups = listOf(
        single("discord"),
    ),
)

project(
    projects = listOf(
      single("dokka-plugin"),
      single("gradle-plugin"),

      single("serializable"),
      single("common"),

      single("app"),
      single("discord"),
    ),
)

include("test-module")
