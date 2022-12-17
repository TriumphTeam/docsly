plugins {
    id("docsly.base-conventions")
}

dependencies {
    implementation(libs.kotlinx.json)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.ktor.core)
    implementation(libs.ktor.netty)

    // Logger
    implementation(libs.logger.api)
    implementation(libs.logger.core)
    implementation(libs.logger.impl)
}
