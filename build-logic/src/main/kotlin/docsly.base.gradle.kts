import dev.triumphteam.root.KotlinOpt
import dev.triumphteam.root.repository.Repository
import dev.triumphteam.root.repository.applyRepo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("com.github.hierynomus.license")
    id("dev.triumphteam.root")
}

repositories {
    mavenCentral()
    applyRepo(Repository.TRIUMPH_SNAPSHOTS)
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines)
}

license {
    header = rootProject.file("LICENSE")
    encoding = "UTF-8"
    mapping("kotlin", "JAVADOC_STYLE")

    include("**/*.kt")
}

root {
    configureKotlin {
        explicitApi()
        jvmVersion(21)
        optIn(KotlinOpt.ALL)
    }
}

/*spotless {
    format("format") {
        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(4)

        target(
            "*.md",
            ".gitignore",
            "*.properties",
        )
    }

    kotlin {
        ktlint("0.47.1").editorConfigOverride(
            mapOf(
                "ktlint_disabled_rules" to "filename,trailing-comma-on-call-site,trailing-comma-on-declaration-site",
            )
        )
    }
}*/
