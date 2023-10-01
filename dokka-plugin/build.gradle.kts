plugins {
    id("docsly.base")
    id("docsly.library")
}

dependencies {
    api(projects.docslySerializable)

    api(libs.dokka.base)

    compileOnly(libs.dokka.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.dokka.api.test)
    testImplementation(libs.dokka.base.test)
}
