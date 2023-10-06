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

/** The language the serializable was written in. */
public interface WithLanguage {
    public val language: Language
}

/** Any serializable that has [SerializableAnnotation]s. */
public interface WithAnnotations {
    public val annotations: List<SerializableAnnotation>
}

/** Any serializable that has [GenericType]s. */
public interface WithGenerics {
    public val generics: List<GenericType>
}

/** Any serializable that has [Modifier]s. */
public interface WithModifiers {
    public val modifiers: Set<Modifier>
}

/** Any serializable that has extra [Documentation]s. */
public interface WithExtraDocs {
    public val extraDocumentation: List<Documentation>
}

/** Any serializable that has constructors. */
public interface WithConstructors {
    public val constructors: List<SerializableFunction>
}

/** Any serializable that can have a companion. */
public interface WithCompanion {
    public val companion: ClassLike?
}

/** Any serializable that can have super types. */
public interface WithSuperTypes {
    public val superTypes: List<SuperType>
}

/** Any serializable that has a [Visibility]. */
public interface WithVisibility {
    public val visibility: Visibility
}

/** Any serializable that can have a [receiver]. */
public interface WithReceiver {
    public val receiver: SerializableType?
}

/** Any serializable that can have a [type]. */
public interface WithType<T : SerializableType?> {
    public val type: T
}

/** A serializable that has a name. */
public interface WithName {
    public val name: String
}

/** Any serializable that is documentable. */
public interface WithDocumentation {
    public val documentation: DescriptionDocumentation?
}
