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

import dev.triumphteam.doclopedia.serializable.component.RootComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A [Documentation] simply has its display [RootComponent]. */
@Serializable
sealed interface Documentation {
    val component: RootComponent
}

/** A [Documentation] that also contains a name. */
sealed interface NamedDocumentation : Documentation, WithName

/** Generally a description of a serializable. */
@Serializable
@SerialName("DESCRIPTION")
data class DescriptionDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@author` tag. */
@Serializable
@SerialName("AUTHOR")
data class AuthorDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@version` tag. */
@Serializable
@SerialName("VERSION")
data class VersionDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@since` tag. */
@Serializable
@SerialName("SINCE")
data class SinceDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@see` tag. */
@Serializable
@SerialName("SEE")
data class SeeDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@return` tag. */
@Serializable
@SerialName("RETURN")
data class ReturnDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@receiver` tag. */
@Serializable
@SerialName("RECEIVER")
data class ReceiverDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@constructor` tag. */
@Serializable
@SerialName("CONSTRUCTOR")
data class ConstructorDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@throws` tag. */
@Serializable
@SerialName("THROWS")
data class ThrowsDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@sample` tag. */
@Serializable
@SerialName("SAMPLE")
data class SampleDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@deprecated` tag. */
@Serializable
@SerialName("DEPRECATED")
data class DeprecatedDocumentation(override val component: RootComponent) : Documentation

/** Documentation of a custom tag. */
@Serializable
@SerialName("CUSTOM")
data class CustomDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

// data class Property(override val root: DocTag, override val name: String) : NamedDocumentation
// data class Suppress(override val root: DocTag) : TagWrapper()
