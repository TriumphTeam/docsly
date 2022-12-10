package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.Serializable

interface Linkable {

    val link: String
}

@Serializable
data class Package(
    override val link: String,
    val path: String,
    val objects: List<Object>,
    // TODO
) : Linkable

interface AnnotationContainer {

    val annotations: List<String>
}