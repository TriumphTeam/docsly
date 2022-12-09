package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.reflect.Member

@Serializable
sealed interface Object : Linkable {

    val members: List<Member>
}

@Serializable
@SerialName("CLASS")
data class Class(
    override val link: String,
    val name: String,
    val annotations: List<String> = emptyList(),
    override val members: List<Member> = emptyList(),
    // TODO
) : Object

@Serializable
@SerialName("INTERFACE")
data class Interface(
    override val link: String,
    val name: String,
    val extends: List<Interface> = emptyList(),
    override val members: List<Member> = emptyList(),
    // TODO
) : Object
