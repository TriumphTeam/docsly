package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.serializable.DocElement

class ContentBuilder {

    private val content: MutableList<DocElement> = mutableListOf()

    fun append(element: DocElement) {
        content += element
    }

    fun build() = content.toList()
}
