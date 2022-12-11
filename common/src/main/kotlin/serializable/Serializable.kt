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

    val annotations: List<Annotation>
}

interface GenericsContainer {

    val generics: List<Generic>
}

interface ModifierContainer {

    val modifiers: List<Modifier>
}


@Serializable
enum class Visibility {
    PRIVATE,
    PROTECTED,
    INTERNAL,
    PUBLIC,
    PACKAGE;

    fun fromString(name: String) = Modifier.values().find { name.uppercase() == it.name }
}

@Serializable
enum class Modifier(val displayName: String? = null) {
    // KOTLIN ONLY
    INLINE,
    VALUE,
    INFIX,
    EXTERNAL,
    SUSPEND,
    REIFIED,
    CROSSINLINE,
    NOINLINE,
    OVERRIDE,
    DATA,
    CONST,
    INNER,
    LATEINIT,
    OPERATOR,
    TAILREC,
    VARARG,
    FUN,

    // JAVA ONLY
    STATIC,
    NATIVE,
    SYNCHRONIZED,
    STRICTFP,
    TRANSIENT,
    VOLATILE,
    TRANSITIVE,
    RECORD,
    NONSEALED("non-sealed"),

    // COMMON
    OPEN,
    FINAL,
    ABSTRACT,
    SEALED;

    companion object {
        // Shout out to non-sealed for this being needed, thanks Java
        private val MAPPED_VALUES = values().associateBy { it.displayName ?: it.name.lowercase() }

        fun fromString(name: String) = MAPPED_VALUES[name]
    }
}
