plugins {
    id("doclopedia.base-conventions")
    `java-gradle-plugin`
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
