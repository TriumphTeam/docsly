plugins {
    id("docsly.base")
}

dependencies {
    api(projects.docslySerializable)
    api(libs.ktor.resources)
}
