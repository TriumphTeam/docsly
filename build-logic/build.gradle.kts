plugins {
    `kotlin-dsl`
}

dependencies {
    // Hack to allow version catalog inside convention plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.build.kotlin)
    implementation(libs.build.serialization)
    implementation(libs.build.license)
}