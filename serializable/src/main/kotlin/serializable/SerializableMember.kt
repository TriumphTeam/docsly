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
package dev.triumphteam.docsly.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A serializable member of a [ClassLike] element. */
@Serializable
public sealed interface SerializableMember :
    DocElementWithLanguage,
    WithDocumentation,
    WithVisibility,
    WithExtraDocs,
    WithGenerics,
    WithAnnotations

/**
 * A property!
 * Java fields are also considered "properties" here, but fast simpler.
 */
@Serializable
@SerialName("PROPERTY")
public data class SerializableProperty(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    public val setter: SerializableFunction?,
    public val getter: SerializableFunction?,
    override val visibility: Visibility,
    @SerialName("class") override val type: SerializableType,
    override val receiver: SerializableType?,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : SerializableMember, WithType<SerializableType>, WithReceiver, WithModifiers

/**
 * A function!
 * Java methods are also considered "functions" here.
 * One of the main documentables of a language.
 */
@Serializable
@SerialName("FUNCTION")
public data class SerializableFunction(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    public val parameters: List<SerializableParameter> = emptyList(),
    override val visibility: Visibility,
    @SerialName("class") override val type: SerializableType?,
    override val receiver: SerializableType?,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : SerializableMember, WithType<SerializableType?>, WithReceiver, WithModifiers

/** A function parameter. */
@Serializable
public data class SerializableParameter(
    override val name: String,
    @SerialName("class") public val type: SerializableType,
    override val annotations: List<SerializableAnnotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
) : WithAnnotations, WithModifiers, WithDocumentation, WithName

/** Serializable class to represent Kotlin's `typealias`. */
@Serializable
@SerialName("TYPE_ALIAS")
public data class SerializableTypeAlias(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    @SerialName("class") override val type: SerializableType,
    @SerialName("underlyingClass") public val underlyingType: SerializableType,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : SerializableMember, WithType<SerializableType>

/** Serializable class with the data of an enum entry. */
@Serializable
@SerialName("ENUM_ENTRY")
public data class SerializableEnumEntry(
    override val location: String,
    override val path: Path,
    override val name: String,
    override val annotations: List<SerializableAnnotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : DocElement, WithDocumentation, WithExtraDocs, WithAnnotations, WithModifiers
