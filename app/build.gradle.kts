plugins {
    id("docsly.base-conventions")
}

dependencies {
    implementation(projects.docslySerializable)

    implementation(libs.kotlinx.serialization.hocon)

    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)

    implementation(libs.bundles.logger)


    implementation(libs.bundles.database)
    implementation("io.ktor:ktor-client-resources:2.2.1")
}
