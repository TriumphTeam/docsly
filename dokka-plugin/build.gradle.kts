plugins {
    id("docsly.base-conventions")
    id("docsly.library-conventions")
}

dependencies {
    api(projects.docslySerializable)

    api(libs.dokka.base)
    api(libs.kotlinx.json)
    api(libs.kotlinx.coroutines)

    compileOnly(libs.dokka.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.dokka.api.test)
    testImplementation(libs.dokka.base.test)
}
