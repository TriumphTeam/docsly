package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.serializable.ClassType
import dev.triumphteam.doclopedia.serializable.Function
import dev.triumphteam.doclopedia.serializable.GenericProjection
import dev.triumphteam.doclopedia.serializable.StarType
import dev.triumphteam.doclopedia.serializable.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.renderers.sourceSets
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.links.DriOfUnit
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DefinitelyNonNullable
import org.jetbrains.dokka.model.Dynamic
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Invariance
import org.jetbrains.dokka.model.JavaObject
import org.jetbrains.dokka.model.Nullable
import org.jetbrains.dokka.model.PrimitiveJavaType
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.Star
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.model.TypeParameter
import org.jetbrains.dokka.model.UnresolvedBound
import org.jetbrains.dokka.model.Void
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
        val annotations = function
            .annotations()
            .values
            .flatten()
            .map { "@${it.dri.classNames}" }

        println(function.returnType())
        /*when (type) {
            is GenericTypeConstructor -> type
        }*/

        function.parameters.forEach { parameter ->
            println("Type for ${parameter.name} is -> ${parameter.type.getSerialType()} -> ${parameter.type::class.java}")
        }

        Function(
            link = "temp",
            name = function.name,
            annotations = annotations
        )
    }

    fun collectRecursive(node: PageNode) {
        when {
            node is ModulePageNode -> node.children.forEach(::collectRecursive)
            node is ClasslikePageNode -> {
                println("Doing thingies for class -> ${node.name}")
                node.children.filterIsInstance<MemberPageNode>().forEach { member ->
                    val documentable = member.documentables.firstOrNull() ?: return@forEach

                    when (documentable) {
                        is DFunction -> {
                            val annotations = documentable
                                .annotations()
                                .values
                                .flatten()
                                .map { "@${it.dri.classNames}" }

                            member.sourceSets().forEach { set ->
                                println("please -> ${set.name}")
                            }
                            documentable.parameters.forEach { }

                            Function(
                                link = "temp",
                                name = documentable.name,
                                annotations = annotations
                            )
                        }

                        else -> {}
                    }
                }

                node.documentables.filterIsInstance<DClasslike>().forEach { doc ->
                    doc.functions.forEach { function ->
                    }
                }
            }

            node is PackagePageNode -> node.children.forEach(::collectRecursive)

        }
    }
}

private fun DFunction.returnType(): Type? = when {
    isConstructor -> null
    type is TypeConstructor && (type as TypeConstructor).dri == DriOfUnit -> null
    type is Void -> null
    else -> type.getSerialType()
}

private fun Projection.getSerialType(projection: GenericProjection = GenericProjection.NONE): Type? {
    return when (this) {
        // Generic `in`
        is Contravariance<*> -> inner.getSerialType(GenericProjection.IN)
        // Generic `out`
        is Covariance<*> -> inner.getSerialType(GenericProjection.OUT)
        // Normal generic
        is Invariance<*> -> inner.getSerialType()
        // Wildcard
        Star -> StarType
        is GenericTypeConstructor -> {
            val type = dri.classNames ?: return null
            val params = projections.mapNotNull { it.getSerialType() }
            ClassType(type, params, projection)
        }

        is TypeConstructor -> dri.classNames?.let { ClassType(it, projection = GenericProjection.NONE) }
        is DefinitelyNonNullable -> TODO()
        Dynamic -> TODO()
        is JavaObject -> TODO()
        is Nullable -> null
        is PrimitiveJavaType -> TODO()
        is TypeAliased -> TODO()
        is TypeParameter -> TODO()
        is UnresolvedBound -> TODO()
        Void -> null
    }
}
