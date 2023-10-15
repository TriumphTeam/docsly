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
package dev.triumphteam.docsly.elements

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Any serializable that can be linked to a specific location of a language. */
@Serializable
public sealed interface DocElement : WithName {
    /** The URL location of the serializable. */
    public val location: String

    /** The virtual path of the serializable within the codebase. */
    public val path: Path

    /** Create a list of possible search references for the doc element. */
    public fun createReferences(): List<String>
}

/** A [DocElement] that also contains language details. */
@Serializable
public sealed interface DocElementWithLanguage : DocElement, WithLanguage

/** Contains information about the [DocElement]'s path. */
@Serializable
public data class Path(
    public val packagePath: String?,
    public val classPath: List<String>?,
)

@Serializable
@SerialName("PACKAGE")
public data class SerializablePackage(
    override val location: String,
    override val path: Path,
    override val name: String,
    override val annotations: List<SerializableAnnotation>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : DocElement, WithDocumentation, WithExtraDocs, WithAnnotations {

    override fun createReferences(): List<String> {
        return listOf(name)
    }
}

/** Possible documentation languages. */
@Serializable
public enum class Language {
    KOTLIN, JAVA;
}

/** Possible visibilities for a serializable [WithVisibility]. */
@Serializable
public enum class Visibility {
    PRIVATE, PROTECTED, PUBLIC, // Common
    INTERNAL, // KT only
    PACKAGE; // Java only

    public companion object {
        private val MAPPED_VALUES = entries.associateBy { it.name.lowercase() }

        /** If the type is empty it'll be Java's [PACKAGE], else we take it from the mapped values. */
        public fun fromString(name: String): Visibility? = if (name.isEmpty()) PACKAGE else MAPPED_VALUES[name]
    }
}

/** Shout out to non-sealed for being the odd one out and needing [displayName], thanks Java. */
@Serializable
public enum class Modifier(public val order: Int, public val displayName: String? = null) {
    // COMMON

    OPEN(0), FINAL(0), ABSTRACT(0), SEALED(0),

    // KOTLIN ONLY

    REIFIED(0), CROSSINLINE(0), NOINLINE(0),

    CONST(0),
    EXTERNAL(1),
    OVERRIDE(2),
    LATEINIT(3),
    TAILREC(4),
    VARARG(5),
    SUSPEND(6),
    INNER(7),

    FUN(8), ENUM(8), ANNOTATION(8),

    COMPANION(9),
    INLINE(10), VALUE(10),
    INFIX(11),
    DATA(13), OPERATOR(12),

    // JAVA ONLY

    STATIC(0), NATIVE(0), SYNCHRONIZED(0), STRICTFP(0),
    TRANSIENT(0), VOLATILE(0), TRANSITIVE(0), RECORD(0),
    NONSEALED(0, "non-sealed"), DEFAULT(0)

    ;

    public companion object {
        private val MAPPED_VALUES = entries.associateBy { it.displayName ?: it.name.lowercase() }

        public fun fromString(name: String): Modifier? = MAPPED_VALUES[name]
    }
}
