/**
 * MIT License
 *
 * Copyright (c) 2019-2022 TriumphTeam and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.renderer.ext.returnType
import dev.triumphteam.doclopedia.renderer.ext.toSerialAnnotations
import dev.triumphteam.doclopedia.renderer.ext.toSerialModifiers
import dev.triumphteam.doclopedia.renderer.ext.toSerialType
import dev.triumphteam.doclopedia.serializable.Function
import dev.triumphteam.doclopedia.serializable.GenericType
import dev.triumphteam.doclopedia.serializable.Parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.modifiers
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer

class JsonRenderer(context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        runBlocking(Dispatchers.Default) {
            renderModule(root)
        }
    }

    private suspend fun renderModule(root: PageNode) {
        if (root !is ModulePageNode) return

        coroutineScope {
            root.children.filterIsInstance<PackagePageNode>().forEach { packagePageNode ->

                val packageDoc = packagePageNode.documentables.firstOrNull() as? DPackage ?: return@forEach

                packageDoc.classlikes.forEach(::renderClass)

                packageDoc.functions.forEach {
                    println("TOP LEVEL FUN -> ${it.name}")
                    // TODO: 10/12/2022 RENDER TOP LEVEL FUNCS 
                }

                packageDoc.properties.forEach {
                    println("TOP LEVEL PROPERTY -> ${it.name}")
                    // TODO: 10/12/2022 RENDER TOP LEVEL PROPERTIES
                }
            }
        }
    }

    private fun render(node: MemberPageNode) {
        println("Rendering a top level function -> ${node.name}")
    }

    private fun renderClass(classDoc: DClasslike) {
        // TODO: 10/12/2022 RENDER REST OF CLASS THINGS
        classDoc.functions.forEach(::renderFunction)
    }

    private fun renderFunction(function: DFunction) {
        val annotations = function.annotations().toSerialAnnotations()

        val parameters = function.parameters.mapNotNull { parameter ->
            val name = parameter.name ?: return@mapNotNull null
            val type = parameter.type.toSerialType() ?: return@mapNotNull null

            Parameter(
                name = name,
                type = type,
                annotations = parameter.annotations().toSerialAnnotations(),
                modifiers = parameter.modifiers().toSerialModifiers(),
            )
        }

        if (function.name != "testInline") return

        val generics = function.generics.map {
            GenericType(
                name = it.name,
                constraints = it.bounds.mapNotNull(Bound::toSerialType),
                modifiers = it.modifiers().toSerialModifiers()
            )
        }

        val func = Function(
            link = "temp",
            name = function.name,
            annotations = annotations,
            parameters = parameters,
            returnType = function.returnType,
            generics = generics,
            modifiers = function.modifiers().toSerialModifiers()
        )

        println(
            Json.encodeToString(func)
        )
    }
}
