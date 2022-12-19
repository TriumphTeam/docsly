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
package dev.triumphteam.docsly.serializable.component

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Very simple implementation of components for message stylizing. Used on [Documentation] */
@Serializable
public sealed interface Component {
    public val children: List<Component>
}

/** A root component is the entry point of a [Documentation]. */
@Serializable
@SerialName("ROOT")
public data class RootComponent(override val children: List<Component> = emptyList()) : Component

/** A paragraph component represents `<p>` in the docs. */
@Serializable
@SerialName("PARAGRAPH")
public data class ParagraphComponent(override val children: List<Component>) : Component

/** A strong emphasis component represents a bold message in the docs. */
@Serializable
@SerialName("STRONG_EMPHASIS")
public data class StrongEmphasisComponent(override val children: List<Component>) : Component

/** A emphasis component represents a italic message in the docs. */
@Serializable
@SerialName("EMPHASIS")
public data class EmphasisComponent(override val children: List<Component>) : Component

/** A strikethrough component represents a struck through message in the docs. */
@Serializable
@SerialName("STRIKETHROUGH")
public data class StrikethroughComponent(override val children: List<Component>) : Component

/** A underlined component represents an underlined message in the docs. */
@Serializable
@SerialName("UNDERLINE")
public data class UnderlinedComponent(override val children: List<Component>) : Component

/** A code inline component represents an inline code in the docs, like `this`. */
@Serializable
@SerialName("CODE_INLINE")
public data class CodeInlineComponent(override val children: List<Component>) : Component

/** A code block component represents an inline code in the docs, like ```this```. */
@Serializable
@SerialName("CODE_BLOCK")
public data class CodeBlockComponent(override val children: List<Component>) : Component

/** A doc link component represents a link to a specific element in the docs. */
@Serializable
@SerialName("DOC_LINK")
public data class DocumentationLinkComponent(override val children: List<Component>) : Component

/** The final point of a component, the component that has [content]. */
@Serializable
@SerialName("TEXT")
public data class TextComponent(public val content: String, override val children: List<Component>) : Component
