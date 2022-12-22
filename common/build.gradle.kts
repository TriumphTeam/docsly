plugins {
    id("docsly.base-conventions")
}

dependencies {
    api(projects.docslySerializable)
    api(libs.ktor.resources)
}
