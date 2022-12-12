/**
 * MIT License
 *
 * Copyright (c) 2019-2022 TriumphTeam and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.doclopedia.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Any serializable that has [Annotation]s. */
interface WithAnnotations {
    val annotations: List<Annotation>
}

/** Any serializable that has [GenericType]s. */
interface WithGenerics {
    val generics: List<GenericType>
}

/** Any serializable that has [Modifier]s. */
interface WithModifiers {
    val modifiers: List<Modifier>

}

/** Any serializable that has extra [Documentation]s. */
interface WithExtraDocs {
    val extraDocumentation: List<Documentation>
}

/** Any serializable that can be linked to a specific location of a language. */
@Serializable
sealed interface WithLocation {
    /** The URL location of the serializable. */
    val location: String

    /** The virtual path of the serializable within the codebase. */
    val path: Path

    /** The language the serializable was written in. */
    val language: Language
}

/** Any serializable that has a [Visibility]. */
interface WithVisibility {
    val visibility: Visibility
}

/** Any serializable that can have a [receiver]. */
interface WithReceiver {
    val receiver: Type?
}

/** Any serializable that can have a [type]. */
interface WithType<T : Type?> {
    val type: T
}

/** Any serializable that is documentable. */
interface Documentable {
    val documentation: DescriptionDocumentation?
}

/** A serializable that has a name. */
interface Named {
    val name: String
}

@Serializable
data class Path(
    @SerialName("package") val packagePath: String?,
    @SerialName("class") val classPath: String?,
)

@Serializable
data class Package(
    override val location: String,
    override val path: Path,
    val objects: List<Object>,
    override val language: Language,
    // TODO
) : WithLocation

/** Possible documentation languages. */
@Serializable
enum class Language {
    KOTLIN, JAVA;
}

/** Possible visibilities for a serializable [WithVisibility]. */
@Serializable
enum class Visibility {
    PRIVATE, PROTECTED, PUBLIC, INTERNAL, // KT only
    PACKAGE; // Java only

    companion object {
        private val MAPPED_VALUES = values().associateBy { it.name.lowercase() }

        /** If the type is empty it'll be Java's [PACKAGE], else we take it from the mapped values. */
        fun fromString(name: String) = if (name.isEmpty()) PACKAGE else MAPPED_VALUES[name]
    }
}

/** Shout out to non-sealed for being the odd one out and needing [displayName], thanks Java. */
@Serializable
enum class Modifier(val displayName: String? = null) {
    // KOTLIN ONLY
    INLINE, VALUE, INFIX, EXTERNAL, SUSPEND, REIFIED, CROSSINLINE, NOINLINE, OVERRIDE, DATA, CONST, INNER, LATEINIT, OPERATOR, TAILREC, VARARG,

    // JAVA ONLY
    STATIC, NATIVE, SYNCHRONIZED, STRICTFP, TRANSIENT, VOLATILE, TRANSITIVE, RECORD, NONSEALED("non-sealed"),

    // COMMON
    OPEN, FINAL, ABSTRACT, SEALED;

    companion object {
        private val MAPPED_VALUES = values().associateBy { it.displayName ?: it.name.lowercase() }

        fun fromString(name: String) = MAPPED_VALUES[name]
    }
}
