package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.serializable.Function
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer

class DocDexRenderer(context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        runBlocking(Dispatchers.Default) {
            renderModule(root)
        }
    }

    private suspend fun renderModule(root: PageNode) {
        if (root !is ModulePageNode) return

        coroutineScope {
            root.children.filterIsInstance<PackagePageNode>().forEach { packagePageNode ->
                packagePageNode.children.forEach {
                    if (it is MemberPageNode) launch { render(it) }
                    if (it is ClasslikePageNode) launch { render(it) }
                }
            }
        }
    }

    private fun render(node: MemberPageNode) {
        println("Rendering a top level function -> ${node.name}")
    }

    private fun render(node: ClasslikePageNode) {
        println("Collecting for class -> ${node.name}")

        node.children.filterIsInstance<MemberPageNode>().forEach { member ->
            val documentable = member.documentables.firstOrNull() ?: return@forEach

            when (documentable) {
                is DFunction -> renderFunction(documentable)
            }
        }
    }

    private fun renderFunction(function: DFunction) {
        val actual = function.annotations()
        val annotations = function.annotations().flatMapped()

        val returnType = function.returnType()

        function.parameters.forEach { parameter ->
            println("Type for ${parameter.name} is -> ${parameter.type.getSerialType()} -> ${parameter.type::class.java}")
        }

        val func = Function(
            link = "temp",
            name = function.name,
            annotations = annotations,
            returnType = returnType
        )

        println(
            Json.encodeToString(func)
        )
    }
}
