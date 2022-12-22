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
package dev.triumphteam.docsly.renderer.ext

import dev.triumphteam.docsly.serializable.AuthorDocumentation
import dev.triumphteam.docsly.serializable.CustomDocumentation
import dev.triumphteam.docsly.serializable.DeprecatedDocumentation
import dev.triumphteam.docsly.serializable.DescriptionDocumentation
import dev.triumphteam.docsly.serializable.Documentation
import dev.triumphteam.docsly.serializable.ReceiverDocumentation
import dev.triumphteam.docsly.serializable.ReturnDocumentation
import dev.triumphteam.docsly.serializable.SampleDocumentation
import dev.triumphteam.docsly.serializable.SeeDocumentation
import dev.triumphteam.docsly.serializable.SinceDocumentation
import dev.triumphteam.docsly.serializable.ThrowsDocumentation
import dev.triumphteam.docsly.serializable.VersionDocumentation
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.Author
import org.jetbrains.dokka.model.doc.B
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.Constructor
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Deprecated
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.doc.I
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Receiver
import org.jetbrains.dokka.model.doc.Return
import org.jetbrains.dokka.model.doc.Sample
import org.jetbrains.dokka.model.doc.See
import org.jetbrains.dokka.model.doc.Since
import org.jetbrains.dokka.model.doc.Strikethrough
import org.jetbrains.dokka.model.doc.Strong
import org.jetbrains.dokka.model.doc.TagWrapper
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Throws
import org.jetbrains.dokka.model.doc.U
import org.jetbrains.dokka.model.doc.Version

/** Shortcut to get the [Documentable]'s main documentation. */
public val Documentable.description: DescriptionDocumentation?
    get() = document?.children?.filterIsInstance<Description>()?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

/** Shortcut to get the main documentation, specifically of a parameter. */
public val DParameter.description: DescriptionDocumentation?
    get() = document?.children?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

/** Shortcut to get all the extra documentation from a [Documentable]. */
public fun Documentable.getDocumentation(): List<Documentation> {
    return document?.children?.mapNotNull {
        it.toDocumentation()
    } ?: emptyList()
}

/** Shortcut to get the [Documentable]'s main [DocumentationNode]. */
private val Documentable.document: DocumentationNode?
    get() = documentation.values.firstOrNull()

/** Converting a [TagWrapper] into the correct serializable [Documentation]. */
private fun TagWrapper.toDocumentation(): Documentation? {
    return when (this) {
        is Author -> AuthorDocumentation(parseRoot(children))
        is Constructor -> AuthorDocumentation(parseRoot(children))
        is Deprecated -> DeprecatedDocumentation(parseRoot(children))
        is CustomTagWrapper -> CustomDocumentation(name, parseRoot(children))
        is Sample -> SampleDocumentation(name, parseRoot(children))
        is See -> SeeDocumentation(name, parseRoot(children))
        is Throws -> ThrowsDocumentation(name, parseRoot(children))
        is Receiver -> ReceiverDocumentation(parseRoot(children))
        is Return -> ReturnDocumentation(parseRoot(children))
        is Since -> SinceDocumentation(parseRoot(children))
        is Version -> VersionDocumentation(parseRoot(children))
        else -> null
    }
}

/** Entry point for parsing [DocTag] into [String] to be serialized. */
private fun parseRoot(tags: List<DocTag>) = tags.joinToString("", transform = ::parseTag).trim()

/** Recursive parsing of [DocTag]s. */
private fun parseTag(tag: DocTag): String = buildString {
    when (tag) {
        is B -> {
            append("**")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("**")
        }

        is I -> {
            append("*")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("*")
        }

        is P -> {
            append(tag.children.joinToString("", transform = ::parseTag))
            append("\n")
        }

        is U -> {
            append("__")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("__")
        }

        is Strong -> {
            append("**")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("**")
        }

        is Strikethrough -> {
            append("~~")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("~~")
        }

        is CodeBlock -> {
            append("```")
            tag.params["lang"]?.let { append(it) }
            append("\n")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("\n")
            append("```")
            append("\n")
        }

        is CodeInline -> {
            append("`")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("`")
        }

        is DocumentationLink -> {
            append("`")
            append(tag.children.joinToString("", transform = ::parseTag))
            append("`")
        }

        is Text -> {
            append(tag.body)
            append(tag.children.joinToString("", transform = ::parseTag))
        }

        Br -> append("\n")

        else -> append(tag.children.joinToString("", transform = ::parseTag))
    }
}
