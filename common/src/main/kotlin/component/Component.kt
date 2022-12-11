package dev.triumphteam.doclopedia.component

import dev.triumphteam.doclopedia.serializable.Documentation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Very simple implementation of components for message stylizing. Used on [Documentation] */
@Serializable
sealed interface Component {
    val children: List<Component>
}

/** A root component is the entry point of a [Documentation]. */
@Serializable
@SerialName("ROOT")
data class RootComponent(override val children: List<Component> = emptyList()) : Component

/** A paragraph component represents `<p>` in the docs. */
@Serializable
@SerialName("PARAGRAPH")
data class ParagraphComponent(override val children: List<Component>) : Component

/** A strong emphasis component represents a bold message in the docs. */
@Serializable
@SerialName("STRONG_EMPHASIS")
data class StrongEmphasisComponent(override val children: List<Component>) : Component

/** A emphasis component represents a italic message in the docs. */
@Serializable
@SerialName("EMPHASIS")
data class EmphasisComponent(override val children: List<Component>) : Component

/** A strikethrough component represents a struck through message in the docs. */
@Serializable
@SerialName("STRIKETHROUGH")
data class StrikethroughComponent(override val children: List<Component>) : Component

/** A underlined component represents an underlined message in the docs. */
@Serializable
@SerialName("UNDERLINE")
data class UnderlinedComponent(override val children: List<Component>) : Component

/** A code inline component represents an inline code in the docs, like `this`. */
@Serializable
@SerialName("CODE_INLINE")
data class CodeInlineComponent(override val children: List<Component>) : Component

/** A code block component represents an inline code in the docs, like ```this```. */
@Serializable
@SerialName("CODE_BLOCK")
data class CodeBlockComponent(override val children: List<Component>) : Component

/** A doc link component represents a link to a specific element in the docs. */
@Serializable
@SerialName("DOC_LINK")
data class DocumentationLinkComponent(override val children: List<Component>) : Component

/** The final point of a component, the component that has [content]. */
@Serializable
@SerialName("TEXT")
data class TextComponent(val content: String, override val children: List<Component>) : Component
