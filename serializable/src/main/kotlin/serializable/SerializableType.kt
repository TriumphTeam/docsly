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

/** Represents a jvm type. */
@Serializable
sealed interface SerializableType

/** Covers most types, contains type parameters and their projects if needed. */
@Serializable
@SerialName("BASIC")
data class BasicType(
    @SerialName("class") val type: String,
    val parameters: List<SerializableType> = emptyList(),
    val projection: GenericProjection? = null,
    val name: String? = null,
    val nullability: Nullability,
    override val annotations: List<SerializableAnnotation> = emptyList(),
) : SerializableType, WithAnnotations

/** Kotlin function type, Java's function are saved as normal generic type aka [BasicType]. */
@Serializable
@SerialName("FUNCTION")
data class FunctionType(
    val params: List<SerializableType> = emptyList(),
    override val receiver: SerializableType? = null,
    val returnType: SerializableType? = null,
    val isSuspendable: Boolean = false,
    val name: String? = null,
    val nullability: Nullability,
    override val annotations: List<SerializableAnnotation> = emptyList(),
) : SerializableType, WithAnnotations, WithReceiver

/** A type alias type simply holds the [alias] type and the [original] type. */
@Serializable
@SerialName("TYPE_ALIAS")
data class TypeAliasType(
    val alias: SerializableType,
    val original: SerializableType,
    override val annotations: List<SerializableAnnotation> = emptyList(),
) : SerializableType, WithAnnotations

/** Represents a generic type. */
@Serializable
@SerialName("GENERIC")
data class GenericType(
    val name: String,
    val constraints: List<SerializableType> = emptyList(),
    override val modifiers: Set<Modifier> = emptySet(),
) : SerializableType, WithModifiers

/** A start type, or Java wildcard. */
@Serializable
object StarType : SerializableType

/** The type of projection to be used. */
@Serializable
enum class GenericProjection(val kotlin: String, val java: String) {
    OUT("out", "extends"),
    IN("in", "super");
}

/** The type of nullability for the type. */
@Serializable
enum class Nullability {
    NOT_NULL,
    NULLABLE,
    DEFINITELY_NOT_NULL
}
