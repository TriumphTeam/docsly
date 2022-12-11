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
    val returnType: Type?,
    val parameters: List<Parameter> = emptyList(),
    override val annotations: List<Annotation> = emptyList(),
    override val generics: List<Generic> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
) : Member, AnnotationContainer, GenericsContainer, ModifierContainer

@Serializable
data class Parameter(
    val name: String,
    @SerialName("class") val type: Type,
    override val annotations: List<Annotation>,
    override val modifiers: List<Modifier>,
) : AnnotationContainer, ModifierContainer

@Serializable
data class Generic(
    val name: String,
    val constraints: List<Type> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
) : ModifierContainer
