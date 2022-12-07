package dev.triumphteam.doclopedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Linkable {

    val link: String
}

@Serializable
sealed interface Object : Linkable

@Serializable
sealed interface Container : Object {

    val members: List<Member>
}

@Serializable
sealed interface Member : Linkable

@Serializable
data class Package(
    override val link: String,
    val path: String,
    val objects: List<Object>,
    // TODO
) : Linkable

// Containers

@Serializable
@SerialName("CLASS")
data class Class(
    override val link: String,
    val name: String,
    val annotations: List<String> = emptyList(),
    override val members: List<Member> = emptyList(),
    // TODO
) : Container

@Serializable
@SerialName("INTERFACE")
data class Interface(
    override val link: String,
    val name: String,
    val extends: List<Interface> = emptyList(),
    override val members: List<Member> = emptyList(),
    // TODO
) : Container

// Members
@Serializable
@SerialName("PROPERTY")
data class Property(
    override val link: String,
    val name: String,
    // TODO
) : Member

// Members
@Serializable
@SerialName("FUNCTION")
data class Function(
    override val link: String,
    val name: String,
    // TODO
) : Member
