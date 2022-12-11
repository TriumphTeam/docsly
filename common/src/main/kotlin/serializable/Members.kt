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
sealed interface Member : Linkable

@Serializable
@SerialName("PROPERTY")
data class Property(
    override val link: String,
    val name: String,
    // TODO
) : Member

@Serializable
@SerialName("FUNCTION")
data class Function(
    override val link: String,
    val name: String,
    val visibility: Visibility,
    val returnType: Type?,
    val parameters: List<Parameter> = emptyList(),
    override val annotations: List<Annotation> = emptyList(),
    override val generics: List<GenericType> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
) : Member, Annotated, Generic, Modifiable

@Serializable
data class Parameter(
    val name: String,
    @SerialName("class") val type: Type,
    override val annotations: List<Annotation>,
    override val modifiers: List<Modifier>,
) : Annotated, Modifiable
