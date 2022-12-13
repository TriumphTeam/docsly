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

import dev.triumphteam.doclopedia.DoclopediaPlugin
import dev.triumphteam.doclopedia.renderer.ext.description
import dev.triumphteam.doclopedia.renderer.ext.extraModifiers
import dev.triumphteam.doclopedia.renderer.ext.finalVisibility
import dev.triumphteam.doclopedia.renderer.ext.getDocumentation
import dev.triumphteam.doclopedia.renderer.ext.language
import dev.triumphteam.doclopedia.renderer.ext.returnType
import dev.triumphteam.doclopedia.renderer.ext.serialGenerics
import dev.triumphteam.doclopedia.renderer.ext.toPath
import dev.triumphteam.doclopedia.renderer.ext.toSerialAnnotations
import dev.triumphteam.doclopedia.renderer.ext.toSerialModifiers
import dev.triumphteam.doclopedia.renderer.ext.toSerialType
import dev.triumphteam.doclopedia.serializable.ClassKind
import dev.triumphteam.doclopedia.serializable.ClassLike
import dev.triumphteam.doclopedia.serializable.Language
import dev.triumphteam.doclopedia.serializable.Modifier
import dev.triumphteam.doclopedia.serializable.SerializableAnnotationClass
import dev.triumphteam.doclopedia.serializable.SerializableClass
import dev.triumphteam.doclopedia.serializable.SerializableEnum
import dev.triumphteam.doclopedia.serializable.SerializableFunction
import dev.triumphteam.doclopedia.serializable.SerializableInterface
import dev.triumphteam.doclopedia.serializable.SerializableObject
import dev.triumphteam.doclopedia.serializable.SerializablePackage
import dev.triumphteam.doclopedia.serializable.SerializableParameter
import dev.triumphteam.doclopedia.serializable.SerializableProperty
import dev.triumphteam.doclopedia.serializable.SerializableTypeAlias
import dev.triumphteam.doclopedia.serializable.SuperType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.PrimaryConstructorExtra
import org.jetbrains.dokka.model.WithAbstraction
import org.jetbrains.dokka.model.WithConstructors
import org.jetbrains.dokka.model.WithSupertypes
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import kotlin.coroutines.CoroutineContext

class JsonRenderer(context: DokkaContext) : Renderer, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }
    private val locationProviderFactory = context.plugin<DoclopediaPlugin>().querySingle { locationProviderFactory }

    // Main json instance for serializing
    private val json = Json {
        prettyPrint = true
        explicitNulls = false
    }

    override fun render(root: RootPageNode) {
        // TODO: Preprocessors the right way

        runBlocking(Dispatchers.Default) {
            renderRoot(root)
        }
    }

    private suspend fun renderRoot(root: RootPageNode) {
        // For now only one module at a time, multi-module can be done later
        if (root !is ModulePageNode) return

        // The location provider, is used to gather the final HTML link a normal doc would produce
        val locationProvider = locationProviderFactory.getLocationProvider(root)

        coroutineScope {
            // Content builder for final module serialization
            val contentBuilder = ContentBuilder()

            // Gathering all packages from modules
            val packages = root.children.flatMap {
                when (it) {
                    is ModulePageNode -> it.children.filterIsInstance<PackagePageNode>()
                    is PackagePageNode -> listOf(it)
                    else -> emptyList()
                }
            }.mapNotNull { it.documentables.firstOrNull() as? DPackage }

            // Collecting content from packages
            packages.forEach { packageDoc ->
                with(packageDoc) {
                    // Main package classes
                    classlikes.forEach { contentBuilder.append(it.render(locationProvider, contentBuilder)) }
                    // Top level functions
                    functions.forEach { contentBuilder.append(it.render(locationProvider)) }
                    // Top level properties
                    properties.forEach { contentBuilder.append(it.render(locationProvider)) }
                    // Type aliases
                    typealiases.forEach { contentBuilder.append(it.render(locationProvider)) }

                    // Appending the package itself
                    contentBuilder.append(
                        SerializablePackage(
                            location = locationProvider.expectedLocationForDri(dri),
                            path = dri.toPath(),
                            name = name,
                            annotations = annotations().toSerialAnnotations(),
                            documentation = description,
                            extraDocumentation = getDocumentation(),
                        )
                    )
                }
            }

            outputWriter.write(root.name, json.encodeToString(contentBuilder.build()), ".json")
        }
    }

    /** Renders all of a [DTypeAlias] into the serializable [SerializableTypeAlias] type. */
    private fun DTypeAlias.render(locationProvider: LocationProvider): SerializableTypeAlias {
        // TODO: Better exceptions
        val type = type.toSerialType()
            ?: throw ClassNotFoundException("Could not resolve the correct type for typealias \"$name\".")

        val underlyingType = underlyingType.values.firstOrNull()?.toSerialType()
            ?: throw ClassNotFoundException("Could not resolve the correct type for typealias \"$name\".")

        return SerializableTypeAlias(
            location = locationProvider.expectedLocationForDri(dri),
            path = dri.toPath(),
            language = Language.KOTLIN, // Type alias is always Kotlin
            name = name,
            type = type,
            underlyingType = underlyingType,
            visibility = finalVisibility,
            annotations = annotations().toSerialAnnotations(),
            generics = serialGenerics,
            documentation = description,
            extraDocumentation = getDocumentation(),
        )
    }

    /** Renders all the [ClassLike] and return themselves, but meanwhile appends all its children into the [ContentBuilder]. */
    private fun DClasslike.render(locationProvider: LocationProvider, contentBuilder: ContentBuilder): ClassLike {
        // Append all the children before appending itself
        functions.forEach { contentBuilder.append(it.render(locationProvider, this is DInterface)) }
        properties.forEach { contentBuilder.append(it.render(locationProvider)) }
        classlikes.forEach { contentBuilder.append(it.render(locationProvider, contentBuilder)) }

        // Return itself to be appended
        return when (this) {
            is DClass -> {
                SerializableClass(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.toPath(),
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
                SerializableAnnotationClass(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.toPath(),
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
                SerializableEnum(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.toPath(),
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
                SerializableInterface(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.toPath(),
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
                SerializableObject(
                    location = locationProvider.expectedLocationForDri(dri),
                    path = dri.toPath(),
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

    /** Renders all of a [DProperty] into the serializable [SerializableProperty] type. */
    private fun DProperty.render(locationProvider: LocationProvider): SerializableProperty {
        // TODO: Better exception
        val propertyType = type.toSerialType()
            ?: throw ClassNotFoundException("Could not resolve the correct type for property \"$name\".")

        // Appending the new element to the final content list
        return SerializableProperty(
            location = locationProvider.expectedLocationForDri(dri),
            path = dri.toPath(),
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

    /** Renders all of a [DFunction] into the serializable [SerializableFunction] type. */
    private fun DFunction.render(
        locationProvider: LocationProvider,
        isParentInterface: Boolean = false,
    ): SerializableFunction {
        // Gathering all parameters for the function
        val parameters = parameters.mapNotNull { parameter ->
            val name = parameter.name ?: return@mapNotNull null
            val type = parameter.type.toSerialType() ?: return@mapNotNull null

            SerializableParameter(
                name = name,
                type = type,
                annotations = parameter.annotations().toSerialAnnotations(),
                modifiers = mainModifier.plus(parameter.modifiers().toSerialModifiers()).toSet(),
                documentation = parameter.description,
            )
        }

        val language = language
        val oldModifiers = mainModifier.plus(modifiers().toSerialModifiers().plus(this.extraModifiers)).toSet()

        // For Java an open method on an interface is a default method
        val modifiers = if (language == Language.JAVA && isParentInterface && oldModifiers.contains(Modifier.OPEN)) {
            oldModifiers.minus(Modifier.OPEN).plus(Modifier.DEFAULT)
        } else {
            oldModifiers
        }

        return SerializableFunction(
            location = locationProvider.expectedLocationForDri(dri),
            path = dri.toPath(),
            language = language,
            name = name,
            visibility = finalVisibility,
            type = returnType,
            receiver = receiver?.type?.toSerialType(),
            parameters = parameters,
            annotations = annotations().toSerialAnnotations(),
            generics = serialGenerics,
            modifiers = modifiers,
            documentation = description,
            extraDocumentation = getDocumentation(),
        )
    }

    /** Simpler way to get the constructors, the primary constructor will always be the first. */
    private fun WithConstructors.getConstructors(locationProvider: LocationProvider): List<SerializableFunction> =
        constructors
            .sortedBy { if (it.extra[PrimaryConstructorExtra] != null) 0 else 1 } // Make sure primary is always first
            .map { it.render(locationProvider) }

    /** Simpler way to get the super types. */
    private fun WithSupertypes.getSuperTypes() = supertypes.values.firstOrNull()?.mapNotNull {
        val type = it.typeConstructor.toSerialType() ?: return@mapNotNull null
        val kind = ClassKind.fromString(it.kind.toString()) ?: return@mapNotNull null

        SuperType(type, kind)
    } ?: emptyList()

    /** Simpler way to get the main modifier. */
    private val WithAbstraction.mainModifier
        get() = modifier.values.mapNotNull { Modifier.fromString(it.name) }
}
