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
import dev.triumphteam.doclopedia.renderer.ext.extraModifiers
import dev.triumphteam.doclopedia.renderer.ext.finalVisibility
import dev.triumphteam.doclopedia.renderer.ext.formattedPath
import dev.triumphteam.doclopedia.renderer.ext.getDocumentation
import dev.triumphteam.doclopedia.renderer.ext.language
import dev.triumphteam.doclopedia.renderer.ext.returnType
import dev.triumphteam.doclopedia.renderer.ext.serialGenerics
import dev.triumphteam.doclopedia.renderer.ext.toSerialAnnotations
import dev.triumphteam.doclopedia.renderer.ext.toSerialModifiers
import dev.triumphteam.doclopedia.renderer.ext.toSerialType
import dev.triumphteam.doclopedia.serializable.AnnotationClassLike
import dev.triumphteam.doclopedia.serializable.ClassClassLike
import dev.triumphteam.doclopedia.serializable.ClassKind
import dev.triumphteam.doclopedia.serializable.ClassLike
import dev.triumphteam.doclopedia.serializable.EnumClassLike
import dev.triumphteam.doclopedia.serializable.Function
import dev.triumphteam.doclopedia.serializable.InterfaceClassLike
import dev.triumphteam.doclopedia.serializable.Modifier
import dev.triumphteam.doclopedia.serializable.ObjectClassLike
import dev.triumphteam.doclopedia.serializable.Parameter
import dev.triumphteam.doclopedia.serializable.Property
import dev.triumphteam.doclopedia.serializable.SuperType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.modifiers
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.PrimaryConstructorExtra
import org.jetbrains.dokka.model.WithAbstraction
import org.jetbrains.dokka.model.WithConstructors
import org.jetbrains.dokka.model.WithSupertypes
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import kotlin.coroutines.CoroutineContext

class JsonRenderer(private val context: DokkaContext) : Renderer, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override fun render(root: RootPageNode) {
        runBlocking(Dispatchers.Default) {
            renderModule(root)
        }
    }

    private suspend fun renderModule(root: PageNode) {
        if (root !is ModulePageNode) return

        val locationProvider =
            context.plugin<DokkaBase>().querySingle { locationProviderFactory }.getLocationProvider(root)
        val contentBuilder = ContentBuilder()

        coroutineScope {
            root.children.filterIsInstance<PackagePageNode>().mapNotNull { packagePageNode ->

                val packageDoc = packagePageNode.documentables.firstOrNull() as? DPackage ?: return@mapNotNull null

                // Main package classes
                packageDoc.classlikes.forEach { contentBuilder.append(it.render(locationProvider, contentBuilder)) }
                // Top level functions
                packageDoc.functions.forEach { contentBuilder.append(it.render(locationProvider)) }
                // Top level properties
                packageDoc.properties.forEach { contentBuilder.append(it.render(locationProvider)) }
            }

            println(Json.encodeToString(contentBuilder.build().associateBy { it.name }))
        }
    }

    /** Renders all the [ClassLike] and return themselves, but meanwhile appends all its children into the [ContentBuilder]. */
    private fun DClasslike.render(locationProvider: LocationProvider, contentBuilder: ContentBuilder): ClassLike {
        // Append all the children before appending itself
        functions.forEach { contentBuilder.append(it.render(locationProvider)) }
        properties.forEach { contentBuilder.append(it.render(locationProvider)) }
        classlikes.forEach { contentBuilder.append(it.render(locationProvider, contentBuilder)) }

        // Return itself to be appended
        return when (this) {
            is DClass -> {
                ClassClassLike(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.formattedPath(),
                    language = language,
                    name = name,
                    constructors = getConstructors(locationProvider),
                    companion = companion?.render(locationProvider, contentBuilder),
                    visibility = finalVisibility,
                    annotations = annotations().toSerialAnnotations(),
                    generics = serialGenerics,
                    modifiers = mainModifier.plus(modifiers().toSerialModifiers().plus(extraModifiers)).toSet(),
                    documentation = description,
                    extraDocumentation = getDocumentation(),
                    superTypes = getSuperTypes(),
                )
            }

            is DAnnotation -> {
                AnnotationClassLike(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.formattedPath(),
                    language = language,
                    name = name,
                    constructors = getConstructors(locationProvider),
                    companion = companion?.render(locationProvider, contentBuilder),
                    visibility = finalVisibility,
                    annotations = annotations().toSerialAnnotations(),
                    generics = serialGenerics,
                    modifiers = modifiers().toSerialModifiers().plus(extraModifiers).toSet(),
                    documentation = description,
                    extraDocumentation = getDocumentation(),
                )
            }

            is DEnum -> {
                EnumClassLike(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.formattedPath(),
                    language = language,
                    name = name,
                    constructors = getConstructors(locationProvider),
                    companion = companion?.render(locationProvider, contentBuilder),
                    visibility = finalVisibility,
                    annotations = annotations().toSerialAnnotations(),
                    modifiers = modifiers().toSerialModifiers().plus(extraModifiers).toSet(),
                    documentation = description,
                    extraDocumentation = getDocumentation(),
                    superTypes = getSuperTypes(),
                )
            }

            is DInterface -> {
                InterfaceClassLike(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.formattedPath(),
                    language = language,
                    name = name,
                    companion = companion?.render(locationProvider, contentBuilder),
                    visibility = finalVisibility,
                    annotations = annotations().toSerialAnnotations(),
                    generics = serialGenerics,
                    modifiers = modifiers().toSerialModifiers().plus(extraModifiers).toSet(),
                    documentation = description,
                    extraDocumentation = getDocumentation(),
                    superTypes = getSuperTypes(),
                )
            }

            is DObject -> {
                ObjectClassLike(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.formattedPath(),
                    language = language,
                    name = name ?: "Unknown",
                    visibility = finalVisibility,
                    annotations = annotations().toSerialAnnotations(),
                    modifiers = modifiers().toSerialModifiers().plus(extraModifiers).toSet(),
                    documentation = description,
                    extraDocumentation = getDocumentation(),
                    superTypes = getSuperTypes(),
                )
            }
        }
    }

    private fun DProperty.render(locationProvider: LocationProvider): Property {
        // TODO: Better exception
        val propertyType = type.toSerialType()
            ?: throw ClassNotFoundException("Could not resolve the correct type for property \"$name\".")

        // Appending the new element to the final content list
        return Property(
            location = locationProvider.expectedLocationForDri(dri),
            path = dri.formattedPath(),
            language = language,
            name = name,
            visibility = finalVisibility,
            type = propertyType,
            getter = this.getter?.render(locationProvider),
            setter = this.setter?.render(locationProvider),
            receiver = receiver?.type?.toSerialType(),
            annotations = annotations().toSerialAnnotations(),
            generics = serialGenerics,
            modifiers = mainModifier.plus(modifiers().toSerialModifiers().plus(extraModifiers)).toSet(),
            documentation = description,
            extraDocumentation = getDocumentation(),
        )
    }

    /** Renders all of a [DFunction] into the serializable [Function] type. */
    private fun DFunction.render(locationProvider: LocationProvider): Function {
        // Gathering all parameters for the function
        val parameters = parameters.mapNotNull { parameter ->
            val name = parameter.name ?: return@mapNotNull null
            val type = parameter.type.toSerialType() ?: return@mapNotNull null

            Parameter(
                name = name,
                type = type,
                annotations = parameter.annotations().toSerialAnnotations(),
                modifiers = mainModifier.plus(parameter.modifiers().toSerialModifiers()).toSet(),
                documentation = parameter.description,
            )
        }

        return Function(
            location = locationProvider.expectedLocationForDri(dri),
            path = dri.formattedPath(),
            language = language,
            name = name,
            visibility = finalVisibility,
            type = returnType,
            receiver = receiver?.type?.toSerialType(),
            parameters = parameters,
            annotations = annotations().toSerialAnnotations(),
            generics = serialGenerics,
            modifiers = mainModifier.plus(modifiers().toSerialModifiers().plus(this.extraModifiers)).toSet(),
            documentation = description,
            extraDocumentation = getDocumentation(),
        )
    }

    private fun WithConstructors.getConstructors(locationProvider: LocationProvider): List<Function> = constructors
        .sortedBy { if (it.extra[PrimaryConstructorExtra] != null) 0 else 1 } // Make sure primary is always first
        .map { it.render(locationProvider) }

    private fun WithSupertypes.getSuperTypes() = supertypes.values.firstOrNull()?.mapNotNull {
        val type = it.typeConstructor.toSerialType() ?: return@mapNotNull null
        val kind = ClassKind.fromString(it.kind.toString()) ?: return@mapNotNull null

        SuperType(type, kind)
    } ?: emptyList()

    private val WithAbstraction.mainModifier
        get() = modifier.values.mapNotNull { Modifier.fromString(it.name) }
}

context(CoroutineScope)
private suspend inline fun <T, R> Iterable<T>.asyncMap(crossinline transform: (T) -> R) =
    map { async { transform(it) } }.map { it.await() }
