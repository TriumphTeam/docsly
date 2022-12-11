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

interface Annotatable {

    val annotations: List<Annotation>
}

interface Generic {

    val generics: List<GenericType>
}

interface Modifiable {

    val modifiers: List<Modifier>
}

@Serializable
enum class Visibility {
    PRIVATE,
    PROTECTED,
    PUBLIC,
    INTERNAL, // KT only
    PACKAGE; // Java only

    companion object {
        private val MAPPED_VALUES = Modifier.values().associateBy { it.name.lowercase() }

        fun fromString(name: String) = MAPPED_VALUES[name]
    }
}

/** Shout out to non-sealed for being the odd one out and needing [displayName], thanks Java. */
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
        private val MAPPED_VALUES = values().associateBy { it.displayName ?: it.name.lowercase() }

        fun fromString(name: String) = MAPPED_VALUES[name]
    }
}
