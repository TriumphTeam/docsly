package dev.triumphteam.docsly.api

import kotlinx.serialization.Serializable

/**
 * Represents a guild setup request, containing a map of default projects and their versions for setup.
 *
 * @property defaults A map of default projects and their versions for setup, where the keys are strings and the values are lists of strings.
 */
@Serializable
public data class GuildSetupRequest(public val defaults: Map<String, Set<String>>)
