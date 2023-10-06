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
package dev.triumphteam.docsly.elements

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A [ClassLike] represents any element that behaves like a Class. */
@Serializable
public sealed interface ClassLike :
    DocElementWithLanguage,
    WithDocumentation,
    WithVisibility,
    WithAnnotations,
    WithModifiers,
    WithExtraDocs {

    override fun createReferences(): List<String> {
        return listOf(
            "${path.packagePath}.$name",
            name,
        )
    }
}

/** A serializable object that contains all information needed to describe a class. */
@Serializable
@SerialName("CLASS")
public data class SerializableClass(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<SerializableFunction>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithConstructors, WithCompanion, WithGenerics, WithSuperTypes

/** A serializable object that contains all information needed to describe an annotation class. */
@Serializable
@SerialName("ANNOTATION")
public data class SerializableAnnotationClass(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<SerializableFunction>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
) : ClassLike, WithConstructors, WithCompanion, WithGenerics

/** A serializable object that contains all information needed to describe an enum. */
@Serializable
@SerialName("ENUM")
public data class SerializableEnum(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val constructors: List<SerializableFunction>,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithConstructors, WithCompanion, WithSuperTypes

/** A serializable object that contains all information needed to describe an object. */
@Serializable
@SerialName("OBJECT")
public data class SerializableObject(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithSuperTypes

/** A serializable object that contains all information needed to describe an interface. */
@Serializable
@SerialName("INTERFACE")
public data class SerializableInterface(
    override val location: String,
    override val path: Path,
    override val language: Language,
    override val name: String,
    override val companion: ClassLike?,
    override val visibility: Visibility,
    override val annotations: List<SerializableAnnotation>,
    override val generics: List<GenericType>,
    override val modifiers: Set<Modifier>,
    override val documentation: DescriptionDocumentation?,
    override val extraDocumentation: List<Documentation>,
    override val superTypes: List<SuperType>,
) : ClassLike, WithCompanion, WithGenerics, WithSuperTypes

/** Due to how super types work, it is also needed to save the [kind] of supertype it is. */
@Serializable
public data class SuperType(@SerialName("name") val type: SerializableType, val kind: ClassKind)

/** Which kind of type is being used as supertype of a [WithSuperTypes]. */
@Serializable
public enum class ClassKind {
    CLASS, INTERFACE, ENUM_CLASS, ENUM_ENTRY, ANNOTATION_CLASS, // Common
    OBJECT; // Kt only

    public companion object {
        private val MAPPED_VALUES = ClassKind.values().associateBy { it.name }

        public fun fromString(name: String): ClassKind? = MAPPED_VALUES[name]
    }
}
