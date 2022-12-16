plugins {
    id("docsly.base-conventions")
    id("docsly.library-conventions")
}

dependencies {
    api(libs.kotlinx.json)
    api(libs.kotlinx.coroutines)
}
