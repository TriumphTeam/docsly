package dev.triumphteam.doclopedia.renderer

import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer

class DocDexRenderer(context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        collectRecursive(root)
    }

    fun collectRecursive(node: PageNode) {
        when {
            node is ModulePageNode -> node.children.forEach(::collectRecursive)
            node is ClasslikePageNode -> {
                println("rendering class -> ${node.name}")
                node.children.filterIsInstance<MemberPageNode>().forEach { funs ->
                    println("    this is a fun -> ${funs.name}")
                    funs.content.children.forEach {
                        test(it, 8)
                    }
                }
            }

            node is PackagePageNode -> node.children.forEach(::collectRecursive)
        }
    }

    fun test(content: ContentNode, indentationSize: Int) {
        println("${" ".repeat(indentationSize)} $content")
        if (content.children.isEmpty()) {
            return
        }

        content.children.forEach {
            test(it, indentationSize + 4)
        }
    }
}