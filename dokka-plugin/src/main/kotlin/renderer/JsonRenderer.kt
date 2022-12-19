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
package dev.triumphteam.docsly.renderer

import dev.triumphteam.docsly.DocslyDokkaPlugin
import dev.triumphteam.docsly.renderer.ext.description
import dev.triumphteam.docsly.renderer.ext.extraModifiers
import dev.triumphteam.docsly.renderer.ext.finalVisibility
import dev.triumphteam.docsly.renderer.ext.getDocumentation
import dev.triumphteam.docsly.renderer.ext.language
import dev.triumphteam.docsly.renderer.ext.returnType
import dev.triumphteam.docsly.renderer.ext.serialGenerics
import dev.triumphteam.docsly.renderer.ext.toPath
import dev.triumphteam.docsly.renderer.ext.toSerialAnnotations
import dev.triumphteam.docsly.renderer.ext.toSerialModifiers
import dev.triumphteam.docsly.renderer.ext.toSerialType
import dev.triumphteam.docsly.serializable.ClassKind
import dev.triumphteam.docsly.serializable.ClassLike
import dev.triumphteam.docsly.serializable.Language
import dev.triumphteam.docsly.serializable.Modifier
import dev.triumphteam.docsly.serializable.SerializableAnnotationClass
import dev.triumphteam.docsly.serializable.SerializableClass
import dev.triumphteam.docsly.serializable.SerializableEnum
import dev.triumphteam.docsly.serializable.SerializableEnumEntry
import dev.triumphteam.docsly.serializable.SerializableFunction
import dev.triumphteam.docsly.serializable.SerializableInterface
import dev.triumphteam.docsly.serializable.SerializableObject
import dev.triumphteam.docsly.serializable.SerializablePackage
import dev.triumphteam.docsly.serializable.SerializableParameter
import dev.triumphteam.docsly.serializable.SerializableProperty
import dev.triumphteam.docsly.serializable.SerializableTypeAlias
import dev.triumphteam.docsly.serializable.SuperType
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
import org.jetbrains.dokka.model.BooleanConstant
import org.jetbrains.dokka.model.ComplexExpression
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
import org.jetbrains.dokka.model.DefaultValue
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.DoubleConstant
import org.jetbrains.dokka.model.Expression
import org.jetbrains.dokka.model.FloatConstant
import org.jetbrains.dokka.model.InheritedMember
import org.jetbrains.dokka.model.IntegerConstant
import org.jetbrains.dokka.model.PrimaryConstructorExtra
import org.jetbrains.dokka.model.StringConstant
import org.jetbrains.dokka.model.WithAbstraction
import org.jetbrains.dokka.model.WithConstructors
import org.jetbrains.dokka.model.WithScope
import org.jetbrains.dokka.model.WithSupertypes
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PackagePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import kotlin.coroutines.CoroutineContext

public class JsonRenderer(context: DokkaContext) : Renderer, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }
    private val locationProviderFactory =
        context.plugin<DocslyDokkaPlugin>().querySingle { locationProviderFactory }

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
                    filteredFunctions.forEach { contentBuilder.append(it.render(locationProvider)) }
                    // Top level properties
                    filteredProperties.forEach { contentBuilder.append(it.render(locationProvider)) }
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
        filteredFunctions.forEach { contentBuilder.append(it.render(locationProvider, this is DInterface)) }
        filteredProperties.forEach { contentBuilder.append(it.render(locationProvider)) }
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
                // Adding all the entries from the ENUM
                entries.forEach {
                    contentBuilder.append(
                        SerializableEnumEntry(
                            location = locationProvider.expectedLocationForDri(it.dri),
                            path = dri.toPath(),
                            name = it.name,
                            annotations = it.annotations().toSerialAnnotations(),
                            modifiers = it.modifiers().toSerialModifiers().plus(it.extraModifiers).toSet(),
                            documentation = it.description,
                            extraDocumentation = it.getDocumentation(),
                        )
                    )
                }

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

            val test: String? = parameter.extra[DefaultValue]?.expression?.values?.firstOrNull()?.stringValue

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

    private val WithScope.filteredFunctions: List<DFunction>
        get() = functions.filterNot { it.isInherited() }

    private val WithScope.filteredProperties: List<DProperty>
        get() = properties.filterNot { it.isInherited() }

    private fun <T> T.isInherited(): Boolean where T : Documentable, T : WithExtraProperties<T> =
        sourceSets.all { sourceSet -> extra[InheritedMember]?.isInherited(sourceSet) == true }

    private val Expression.stringValue: String?
        get() = when (this) {
            is ComplexExpression -> value
            is StringConstant -> value
            is IntegerConstant -> value.toString()
            is DoubleConstant -> value.toString()
            is FloatConstant -> value.toString()
            is BooleanConstant -> value.toString()
            else -> null
        }
}
