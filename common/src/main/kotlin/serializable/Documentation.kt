package dev.triumphteam.doclopedia.serializable

import dev.triumphteam.doclopedia.component.Component
import dev.triumphteam.doclopedia.component.RootComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A [Documentation] simply has its display [Component]. */
@Serializable
sealed interface Documentation {
    val component: Component
}

/** A [Documentation] that also contains a name. */
sealed interface NamedDocumentation : Documentation, Named

/** Generally a description of a serializable. */
@Serializable
@SerialName("DESCRIPTION")
data class DescriptionDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@author` tag. */
@Serializable
@SerialName("AUTHOR")
data class AuthorDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@version` tag. */
@Serializable
@SerialName("VERSION")
data class VersionDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@since` tag. */
@Serializable
@SerialName("SINCE")
data class SinceDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@see` tag. */
@Serializable
@SerialName("SEE")
data class SeeDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@return` tag. */
@Serializable
@SerialName("RETURN")
data class ReturnDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@receiver` tag. */
@Serializable
@SerialName("RECEIVER")
data class ReceiverDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@constructor` tag. */
@Serializable
@SerialName("CONSTRUCTOR")
data class ConstructorDocumentation(override val component: RootComponent) : Documentation

/** Documentation of the `@throws` tag. */
@Serializable
@SerialName("THROWS")
data class ThrowsDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@sample` tag. */
@Serializable
@SerialName("SAMPLE")
data class SampleDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

/** Documentation of the `@deprecated` tag. */
@Serializable
@SerialName("DEPRECATED")
data class DeprecatedDocumentation(override val component: RootComponent) : Documentation

/** Documentation of a custom tag. */
@Serializable
@SerialName("CUSTOM")
data class CustomDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

// data class Property(override val root: DocTag, override val name: String) : NamedDocumentation
// data class Suppress(override val root: DocTag) : TagWrapper()
