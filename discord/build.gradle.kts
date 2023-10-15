plugins {
    id("docsly.base")
}

dependencies {
    implementation(projects.docslyCommon)
    implementation(projects.docslyRendererDiscord)

    implementation(libs.kotlinx.serialization.hocon)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.logger)
    implementation(libs.caffeine)
    implementation(libs.kord)
}
