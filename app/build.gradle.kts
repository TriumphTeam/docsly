plugins {
    id("docsly.base-conventions")
}

dependencies {
    implementation(libs.kotlinx.serialization.hocon)

    // Rest
    implementation(libs.ktor.core)
    implementation(libs.ktor.netty)

    // Logger
    implementation(libs.logger.api)
    implementation(libs.logger.core)
    implementation(libs.logger.impl)

    // DB
    implementation(libs.exposed)
    implementation(libs.postgres)
    implementation(libs.hikari)
}
