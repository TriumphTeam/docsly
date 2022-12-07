package dev.triumphteam.doclopedia.renderer

import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer

class DocDexRenderer(context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        if (root !is ModulePageNode) return
        println(root.name)
    }
}