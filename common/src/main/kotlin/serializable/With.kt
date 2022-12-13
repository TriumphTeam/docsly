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

/** Any serializable that has [SerializableAnnotation]s. */
interface WithAnnotations {
    val annotations: List<SerializableAnnotation>
}

/** Any serializable that has [GenericType]s. */
interface WithGenerics {
    val generics: List<GenericType>
}

/** Any serializable that has [Modifier]s. */
interface WithModifiers {
    val modifiers: Set<Modifier>
}

/** Any serializable that has extra [Documentation]s. */
interface WithExtraDocs {
    val extraDocumentation: List<Documentation>
}

/** Any serializable that has constructors. */
interface WithConstructors {
    val constructors: List<SerializableFunction>
}

/** Any serializable that can have a companion. */
interface WithCompanion {
    val companion: ClassLike?
}

/** Any serializable that can have super types. */
interface WithSuperTypes {
    val superTypes: List<SuperType>
}

/** Any serializable that has a [Visibility]. */
interface WithVisibility {
    val visibility: Visibility
}

/** Any serializable that can have a [receiver]. */
interface WithReceiver {
    val receiver: SerializableType?
}

/** Any serializable that can have a [type]. */
interface WithType<T : SerializableType?> {
    val type: T
}

/** A serializable that has a name. */
interface WithName {
    val name: String
}

/** Any serializable that is documentable. */
interface WithDocumentation {
    val documentation: DescriptionDocumentation?
}
