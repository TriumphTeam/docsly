package dev.triumphteam.doclopedia.renderer.ext

import dev.triumphteam.doclopedia.component.CodeBlockComponent
import dev.triumphteam.doclopedia.component.CodeInlineComponent
import dev.triumphteam.doclopedia.component.Component
import dev.triumphteam.doclopedia.component.DocumentationLinkComponent
import dev.triumphteam.doclopedia.component.EmphasisComponent
import dev.triumphteam.doclopedia.component.EmptyComponent
import dev.triumphteam.doclopedia.component.ParagraphComponent
import dev.triumphteam.doclopedia.component.RootComponent
import dev.triumphteam.doclopedia.component.StrikethroughComponent
import dev.triumphteam.doclopedia.component.StrongEmphasisComponent
import dev.triumphteam.doclopedia.component.TextComponent
import dev.triumphteam.doclopedia.component.UnderlinedComponent
import dev.triumphteam.doclopedia.serializable.AuthorDocumentation
import dev.triumphteam.doclopedia.serializable.CustomDocumentation
import dev.triumphteam.doclopedia.serializable.DeprecatedDocumentation
import dev.triumphteam.doclopedia.serializable.DescriptionDocumentation
import dev.triumphteam.doclopedia.serializable.Documentation
import dev.triumphteam.doclopedia.serializable.ReceiverDocumentation
import dev.triumphteam.doclopedia.serializable.ReturnDocumentation
import dev.triumphteam.doclopedia.serializable.SampleDocumentation
import dev.triumphteam.doclopedia.serializable.SeeDocumentation
import dev.triumphteam.doclopedia.serializable.SinceDocumentation
import dev.triumphteam.doclopedia.serializable.ThrowsDocumentation
import dev.triumphteam.doclopedia.serializable.VersionDocumentation
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

private val Documentable.document: DocumentationNode?
    get() = documentation.values.firstOrNull()

val Documentable.description: DescriptionDocumentation?
    get() = document?.children?.filterIsInstance<Description>()?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

val DParameter.description: DescriptionDocumentation?
    get() = document?.children?.firstOrNull()?.let {
        DescriptionDocumentation(parseRoot(it.children))
    }

fun Documentable.getDocumentation(): List<Documentation> {
    return document?.children?.mapNotNull {
        it.toDocumentation()
    } ?: emptyList()
}

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

private fun parseRoot(tags: List<DocTag>) = RootComponent(tags.flatMap(::parseTag))

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
    else -> tag.children.flatMap(::parseTag).filterNot { it is EmptyComponent }
}
