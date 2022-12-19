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

/** A simple representation of an annotation with all its needed arguments. */
@Serializable
public data class SerializableAnnotation(
    @SerialName("class") public val type: String,
    public val arguments: Map<String, SerializableAnnotationArgument> = emptyMap(),
)

// ARGUMENTS

/** Represents an annotation argument. */
@Serializable
public sealed interface SerializableAnnotationArgument

/** An annotation holder .. annotation. */
@Serializable
@SerialName("ANNOTATION")
public data class AnnotationAnnotationArgument(val value: SerializableAnnotation) : SerializableAnnotationArgument

/** An array annotation which has a list of arguments. */
@Serializable
@SerialName("ARRAY")
public data class ArrayAnnotationArgument(val value: List<SerializableAnnotationArgument>) :
    SerializableAnnotationArgument

/** An argument of a type, normally either class type or enum type. */
@Serializable
@SerialName("TYPED")
public data class TypedAnnotationArgument(
    public val typeName: String,
    public val valueType: AnnotationValueType,
) : SerializableAnnotationArgument

/** An argument holding a constant value. */
@Serializable
@SerialName("LITERAL")
public data class LiteralAnnotationArgument(val typeName: String) : SerializableAnnotationArgument

/** The value type for a [TypedAnnotationArgument]. */
@Serializable
public enum class AnnotationValueType {
    CLASS,
    ENUM,
}
