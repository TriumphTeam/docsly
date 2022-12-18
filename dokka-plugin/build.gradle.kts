plugins {
    id("docsly.base-conventions")
    id("docsly.library-conventions")
}

dependencies {
    api(projects.docslySerializable)

    api(libs.dokka.base)

    compileOnly(libs.dokka.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.dokka.api.test)
    testImplementation(libs.dokka.base.test)
}
