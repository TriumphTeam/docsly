package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.Serializable

@Serializable
sealed interface Type

@Serializable
data class ClassType(val type: String, val parameters: List<Type> = emptyList(), val projection: GenericProjection) : Type

@Serializable
object StarType : Type

@Serializable
enum class GenericProjection(val java: String) {
    OUT("extends"),
    IN("super"),
    NONE("")
}