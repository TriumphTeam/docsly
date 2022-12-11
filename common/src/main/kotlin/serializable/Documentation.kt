package dev.triumphteam.doclopedia.serializable

import dev.triumphteam.doclopedia.component.Component
import dev.triumphteam.doclopedia.component.RootComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Documentation {

    val component: Component
}

sealed interface NamedDocumentation : Documentation {

    val name: String
}

@Serializable
@SerialName("DESCRIPTION")
data class DescriptionDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("AUTHOR")
data class AuthorDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("VERSION")
data class VersionDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("SINCE")
data class SinceDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("SEE")
data class SeeDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

@Serializable
@SerialName("RETURN")
data class ReturnDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("RECEIVE")
data class ReceiverDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("CONSTRUCTOR")
data class ConstructorDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("THROWS")
data class ThrowsDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

@Serializable
@SerialName("SAMPLE")
data class SampleDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

@Serializable
@SerialName("DEPRECATED")
data class DeprecatedDocumentation(override val component: RootComponent) : Documentation

@Serializable
@SerialName("CUSTOM")
data class CustomDocumentation(override val name: String, override val component: RootComponent) : NamedDocumentation

// data class Property(override val root: DocTag, override val name: String) : NamedDocumentation
// data class Suppress(override val root: DocTag) : TagWrapper()
