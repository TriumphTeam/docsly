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

/** Any serializable that can be linked to a specific location of a language. */
@Serializable
sealed interface DocElement : WithName {
    /** The URL location of the serializable. */
    val location: String

    /** The virtual path of the serializable within the codebase. */
    val path: Path
}

/** A [DocElement] that also contains language details. */
@Serializable
sealed interface DocElementWithLanguage : DocElement, WithLanguage

/** Contains information about the [DocElement]'s path. */
@Serializable
data class Path(
    val packagePath: String?,
    val classPath: List<String>?,
)

@Serializable
@SerialName("PACKAGE")
data class SerializablePackage(
    override val location: String,
    override val path: Path,
    override val name: String,
    override val annotations: List<SerializableAnnotation>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : DocElement, WithDocumentation, WithExtraDocs, WithAnnotations

/** Possible documentation languages. */
@Serializable
enum class Language {
    KOTLIN, JAVA;
}

/** Possible visibilities for a serializable [WithVisibility]. */
@Serializable
enum class Visibility {
    PRIVATE, PROTECTED, PUBLIC, // Common
    INTERNAL, // KT only
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
