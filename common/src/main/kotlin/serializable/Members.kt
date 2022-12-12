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

@Serializable
sealed interface Member : WithLocation, Documentable, WithVisibility, WithExtraDocs, WithModifiers, WithGenerics,
    WithAnnotations, WithReceiver

/**
 * A property!
 * Java fields are also considered "properties" here, but fast simpler.
 */
@Serializable
@SerialName("PROPERTY")
data class Property(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    val setter: Function?,
    val getter: Function?,
    override val visibility: Visibility,
    @SerialName("class") override val type: Type,
    override val receiver: Type?,
    override val annotations: List<Annotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : Member, WithType<Type>

/**
 * A function!
 * Java methods are also considered "functions" here.
 * One of the main documentables of a language.
 */
@Serializable
@SerialName("FUNCTION")
data class Function(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    val parameters: List<Parameter> = emptyList(),
    override val visibility: Visibility,
    @SerialName("class") override val type: Type?,
    override val receiver: Type?,
    override val annotations: List<Annotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : Member, WithType<Type?>

/** A function parameter. */
@Serializable
data class Parameter(
    override val name: String,
    @SerialName("class") val type: Type,
    override val annotations: List<Annotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
) : WithAnnotations, WithModifiers, Documentable, Named
