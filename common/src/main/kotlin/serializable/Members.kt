package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed interface Member : Linkable

@Serializable
@SerialName("PROPERTY")
data class Property(
    override val link: String,
    val name: String,
    // TODO
) : Member

@Serializable
@SerialName("FUNCTION")
data class Function(
    override val link: String,
    val name: String,
    val annotations: List<String> = emptyList(),
) : Member
