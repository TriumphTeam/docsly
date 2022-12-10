package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Represents a jvm type. */
@Serializable
sealed interface Type

/** Covers most types, contains type parameters and their projects if needed. */
@Serializable
@SerialName("BASIC")
data class BasicType(
    @SerialName("class") val type: String,
    val parameters: List<Type> = emptyList(),
    val projection: GenericProjection? = null,
    val name: String? = null,
    val nullability: Nullability,
    override val annotations: List<String> = emptyList(),
) : Type, AnnotationContainer

@Serializable
@SerialName("FUNCTION")
data class FunctionType(
    val params: List<Type> = emptyList(),
    val receiver: Type? = null,
    val returnType: Type? = null,
    val isSuspendable: Boolean = false,
    val name: String? = null,
    val nullability: Nullability,
    override val annotations: List<String> = emptyList(),
) : Type, AnnotationContainer

/** A start type, or Java wildcard. */
@Serializable
object StarType : Type

/** The type of projection to be used. */
@Serializable
enum class GenericProjection(val kotlin: String, val java: String) {
    OUT("out", "extends"),
    IN("in", "super");
}

@Serializable
enum class Nullability {
    NOT_NULL,
    NULLABLE,
    DEFINITELY_NOT_NULL
}