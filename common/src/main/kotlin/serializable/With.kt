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
