plugins {
    id("doclopedia.base-conventions")
    id("doclopedia.library-conventions")
}

dependencies {
    api(projects.doclopediaSerializable)
    implementation(libs.dokka.base)

    compileOnly(libs.dokka.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.dokka.api.test)
    testImplementation(libs.dokka.base.test)
}
