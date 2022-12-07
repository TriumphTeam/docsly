import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.hierynomus.license")
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

license {
    header = rootProject.file("LICENSE")
    encoding = "UTF-8"
    mapping("kotlin", "JAVADOC_STYLE")

    include("**/*.kt")
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }
}