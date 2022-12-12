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

import dev.triumphteam.doclopedia.renderer.ext.description
import dev.triumphteam.doclopedia.renderer.ext.finalVisibility
import dev.triumphteam.doclopedia.renderer.ext.formattedPath
import dev.triumphteam.doclopedia.renderer.ext.getDocumentation
import dev.triumphteam.doclopedia.renderer.ext.language
import dev.triumphteam.doclopedia.renderer.ext.returnType
import dev.triumphteam.doclopedia.renderer.ext.serialGenerics
import dev.triumphteam.doclopedia.renderer.ext.toSerialAnnotations
import dev.triumphteam.doclopedia.renderer.ext.toSerialModifiers
import dev.triumphteam.doclopedia.renderer.ext.toSerialType
import dev.triumphteam.doclopedia.serializable.Function
import dev.triumphteam.doclopedia.serializable.Parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.modifiers
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer

class JsonRenderer(private val context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        runBlocking(Dispatchers.Default) {
            renderModule(root)
        }
    }

    private suspend fun renderModule(root: PageNode) {
        if (root !is ModulePageNode) return

        val locationProvider =
            context.plugin<DokkaBase>().querySingle { locationProviderFactory }.getLocationProvider(root)

        coroutineScope {
            root.children.filterIsInstance<PackagePageNode>().forEach { packagePageNode ->

                val packageDoc = packagePageNode.documentables.firstOrNull() as? DPackage ?: return@forEach

                packageDoc.classlikes.forEach { renderClass(it, locationProvider) }

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

    private fun renderClass(classDoc: DClasslike, locationProvider: LocationProvider) {
        // TODO: 10/12/2022 RENDER REST OF CLASS THINGS
        classDoc.functions.forEach {
            println(Json.encodeToString(renderFunction(it, locationProvider)))
        }
    }

    /** Renders all of a [DFunction] into the serializable [Function] type. */
    private fun renderFunction(function: DFunction, locationProvider: LocationProvider): Function {
        // Gathering all parameters for the function
        val parameters = function.parameters.mapNotNull { parameter ->
            val name = parameter.name ?: return@mapNotNull null
            val type = parameter.type.toSerialType() ?: return@mapNotNull null

            Parameter(
                name = name,
                type = type,
                annotations = parameter.annotations().toSerialAnnotations(),
                modifiers = parameter.modifiers().toSerialModifiers(),
                documentation = parameter.description,
            )
        }

        return Function(
            location = locationProvider.expectedLocationForDri(function.dri),
            path = function.dri.formattedPath(),
            language = function.language,
            name = function.name,
            visibility = function.finalVisibility,
            returnType = function.returnType,
            receiver = function.receiver?.type?.toSerialType(),
            parameters = parameters,
            annotations = function.annotations().toSerialAnnotations(),
            generics = function.serialGenerics,
            modifiers = function.modifiers().toSerialModifiers(),
            documentation = function.description,
            extraDocumentation = function.getDocumentation(),
        )
    }
}
