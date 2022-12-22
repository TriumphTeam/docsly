plugins {
    id("docsly.base-conventions")
}

dependencies {
    implementation(projects.docslySerializable)
    implementation(libs.kotlinx.serialization.hocon)

    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logger)
    implementation(libs.bundles.database)

    implementation("io.ktor:ktor-client-core:2.2.1")
    implementation("io.ktor:ktor-client-okhttp:2.2.1")
    implementation("io.ktor:ktor-client-auth:2.2.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.1")
    implementation("io.ktor:ktor-client-logging:2.2.1")

    implementation("com.meilisearch.sdk:meilisearch-java:0.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.1")
}
