plugins {
    `kotlin-dsl`
}

dependencies {
    // Hack to allow version catalog inside convention plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // Bundled gradle portal plugins for convention plugins
    implementation(libs.bundles.build)
}
