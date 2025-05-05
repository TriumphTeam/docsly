plugins {
    id("docsly.base")
    id("docsly.library")
}

dependencies {
    api(projects.docslySerializable)
    api(libs.dokka.base)
    compileOnly(libs.dokka.core)
}
