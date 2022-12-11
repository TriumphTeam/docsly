package dev.triumphteam.doclopedia.component

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Component {

    val children: List<Component>
}

@Serializable
@SerialName("ROOT")
data class RootComponent(override val children: List<Component> = emptyList()) : Component

@Serializable
@SerialName("PARAGRAPH")
data class ParagraphComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("STRONG_EMPHASIS")
data class StrongEmphasisComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("EMPHASIS")
data class EmphasisComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("STRIKETHROUGH")
data class StrikethroughComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("UNDERLINE")
data class UnderlinedComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("CODE_INLINE")
data class CodeInlineComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("CODE_BLOCK")
data class CodeBlockComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("DOC_LINK")
data class DocumentationLinkComponent(override val children: List<Component>) : Component

@Serializable
@SerialName("TEXT")
data class TextComponent(val content: String, override val children: List<Component>) : Component

@Serializable
@SerialName("EMPTY")
object EmptyComponent : Component {

    override val children: List<Component> = emptyList()
}
