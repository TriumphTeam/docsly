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
import dev.triumphteam.docsly.serializable.component.CodeBlockComponent
import dev.triumphteam.docsly.serializable.component.CodeInlineComponent
import dev.triumphteam.docsly.serializable.component.Component
import dev.triumphteam.docsly.serializable.component.DocumentationLinkComponent
import dev.triumphteam.docsly.serializable.component.EmphasisComponent
import dev.triumphteam.docsly.serializable.component.ParagraphComponent
import dev.triumphteam.docsly.serializable.component.RootComponent
import dev.triumphteam.docsly.serializable.component.StrikethroughComponent
import dev.triumphteam.docsly.serializable.component.StrongEmphasisComponent
import dev.triumphteam.docsly.serializable.component.TextComponent
import dev.triumphteam.docsly.serializable.component.UnderlinedComponent
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.Author
import org.jetbrains.dokka.model.doc.B
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
val Documentable.description: DescriptionDocumentation?
    get() = document?.children?.filterIsInstance<Description>()?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

/** Shortcut to get the main documentation, specifically of a parameter. */
val DParameter.description: DescriptionDocumentation?
    get() = document?.children?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

/** Shortcut to get all the extra documentation from a [Documentable]. */
fun Documentable.getDocumentation(): List<Documentation> {
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

/** Entry point for parsing [DocTag] into [Component] to be serialized, always start as [RootComponent]. */
private fun parseRoot(tags: List<DocTag>) = RootComponent(tags.flatMap(::parseTag))

/** Recursive parsing of [DocTag]s into their correspondent [Component]. */
private fun parseTag(tag: DocTag): List<Component> = when (tag) {
    is B -> listOf(StrongEmphasisComponent(tag.children.flatMap(::parseTag)))
    is I -> listOf(EmphasisComponent(tag.children.flatMap(::parseTag)))
    is P -> listOf(ParagraphComponent(tag.children.flatMap(::parseTag)))
    is U -> listOf(UnderlinedComponent(tag.children.flatMap(::parseTag)))
    is Strong -> listOf(StrongEmphasisComponent(tag.children.flatMap(::parseTag)))
    is Strikethrough -> listOf(StrikethroughComponent(tag.children.flatMap(::parseTag)))
    is CodeBlock -> listOf(CodeBlockComponent(tag.children.flatMap(::parseTag)))
    is CodeInline -> listOf(CodeInlineComponent(tag.children.flatMap(::parseTag)))
    is DocumentationLink -> listOf(DocumentationLinkComponent(tag.children.flatMap(::parseTag)))
    is Text -> listOf(TextComponent(tag.body, tag.children.flatMap(::parseTag)))
    else -> tag.children.flatMap(::parseTag)
}
