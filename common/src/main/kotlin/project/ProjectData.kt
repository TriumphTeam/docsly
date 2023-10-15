package dev.triumphteam.docsly.project

import kotlinx.serialization.Serializable

@Serializable
public data class ProjectData(public val name: String, public val versions: List<String>)

@Serializable
public data class DocumentSearchResult(public val value: String, public val id: Long)
