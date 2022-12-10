package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A simple representation of an annotation with all its needed arguments. */
@Serializable
data class Annotation(
    @SerialName("class") val type: String,
    val arguments: Map<String, AnnotationArgument> = emptyMap(),
)

// ARGUMENTS

/** Represents an annotation argument. */
@Serializable
sealed interface AnnotationArgument

/** An annotation holder .. annotation. */
@Serializable
@SerialName("ANNOTATION")
data class AnnotationAnnotationArgument(val value: Annotation) : AnnotationArgument

/** An array annotation which has a list of arguments. */
@Serializable
@SerialName("ARRAY")
data class ArrayAnnotationArgument(val value: List<AnnotationArgument>) : AnnotationArgument

/** An argument of a type, normally either class type or enum type. */
@Serializable
@SerialName("TYPED")
data class TypedAnnotationArgument(
    val typeName: String,
    val valueType: ValueType,
) : AnnotationArgument

/** An argument holding a constant value. */
@Serializable
@SerialName("LITERAL")
data class LiteralAnnotationArgument(val typeName: String) : AnnotationArgument

/** The value type for a [TypedAnnotationArgument]. */
@Serializable
enum class ValueType {
    CLASS,
    ENUM,
}
