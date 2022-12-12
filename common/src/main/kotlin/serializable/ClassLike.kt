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

interface WithConstructors {

    val constructors: List<Function>
}

interface WithCompanion {

    val companion: ClassLike?
}

interface WithSuperTypes {

    val superTypes: List<SuperType>
}

@Serializable
sealed interface ClassLike : WithLocation, Named, Documentable, WithVisibility, WithAnnotations,
    WithModifiers, WithExtraDocs

@Serializable
@SerialName("CLASS")
data class ClassClassLike(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<Function>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<Annotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithConstructors, WithCompanion, WithGenerics, WithSuperTypes

@Serializable
@SerialName("ANNOTATION")
data class AnnotationClassLike(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<Function>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<Annotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : ClassLike, WithConstructors, WithCompanion, WithGenerics

@Serializable
@SerialName("Enum")
data class EnumClassLike(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<Function>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<Annotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithConstructors, WithCompanion, WithSuperTypes

@Serializable
@SerialName("OBJECT")
data class ObjectClassLike(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val visibility: Visibility,
    override val annotations: List<Annotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithSuperTypes

@Serializable
@SerialName("INTERFACE")
data class InterfaceClassLike(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<Annotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithCompanion, WithGenerics, WithSuperTypes

@Serializable
data class SuperType(@SerialName("name") val type: Type, val kind: ClassKind)

enum class ClassKind {
    CLASS, INTERFACE, ENUM_CLASS, ENUM_ENTRY, ANNOTATION_CLASS, // Common
    OBJECT; // Kt only

    companion object {
        private val MAPPED_VALUES = ClassKind.values().associateBy { it.name }

        fun fromString(name: String) = MAPPED_VALUES[name]
    }
}
