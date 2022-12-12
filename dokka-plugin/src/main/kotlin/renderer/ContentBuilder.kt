package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.serializable.WithLocation

class ContentBuilder {

    private val content: MutableList<WithLocation> = mutableListOf()

    fun append(element: WithLocation) {
        content += element
    }

    fun build() = content.toList()
}
