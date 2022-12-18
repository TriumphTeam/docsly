plugins {
    id("docsly.base-conventions")
}

dependencies {
    implementation(projects.docslySerializable)
    implementation(libs.kotlinx.serialization.hocon)

    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logger)
    implementation(libs.bundles.database)
}
