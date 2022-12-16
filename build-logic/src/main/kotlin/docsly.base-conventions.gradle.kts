import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Hack which exposes `libs` to this convention plugin
val libs = the<LibrariesForLibs>()

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.hierynomus.license")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

license {
    header = rootProject.file("LICENSE")
    encoding = "UTF-8"
    mapping("kotlin", "JAVADOC_STYLE")

    include("**/*.kt")
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
            freeCompilerArgs = listOf(
                "-Xcontext-receivers",
                "-opt-in=" + listOf(
                    "kotlin.RequiresOptIn",
                    "kotlin.time.ExperimentalTime",
                    "kotlin.io.path.ExperimentalPathApi",
                    "kotlin.io.path.ExperimentalSerializationApi",
                    "kotlin.ExperimentalStdlibApi",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "kotlinx.serialization.InternalSerializationApi",
                    "kotlinx.serialization.ExperimentalSerializationApi",
                ).joinToString(","),
            )
        }
    }
}
