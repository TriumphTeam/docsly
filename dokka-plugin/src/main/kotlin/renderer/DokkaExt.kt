package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.DoclopediaDokkaPlugin
import dev.triumphteam.doclopedia.serializable.BasicType
import dev.triumphteam.doclopedia.serializable.FunctionType
import dev.triumphteam.doclopedia.serializable.GenericProjection
import dev.triumphteam.doclopedia.serializable.Nullability
import dev.triumphteam.doclopedia.serializable.StarType
import dev.triumphteam.doclopedia.serializable.Type
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.links.DriOfUnit
import org.jetbrains.dokka.model.AnnotationTarget
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DefinitelyNonNullable
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.Dynamic
import org.jetbrains.dokka.model.FunctionalTypeConstructor
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
import org.jetbrains.dokka.model.properties.WithExtraProperties

fun DFunction.returnType(): Type? = when {
    isConstructor -> null
    type is TypeConstructor && (type as TypeConstructor).dri == DriOfUnit -> null
    type is Void -> null
    else -> type.getSerialType()
}

fun Projection.getSerialType(
    projection: GenericProjection? = null,
    nullability: Nullability = Nullability.NOT_NULL,
): Type? {
    return when (this) {
        // Generic `in`
        is Contravariance<*> -> inner.getSerialType(GenericProjection.IN, nullability)
        // Generic `out`
        is Covariance<*> -> inner.getSerialType(GenericProjection.OUT, nullability)
        // Normal generic
        is Invariance<*> -> inner.getSerialType(nullability = nullability)
        // Wildcard
        Star -> StarType
        // A generic typed-type
        is GenericTypeConstructor -> extractGenericType(projection, nullability)
        // Functional type
        is FunctionalTypeConstructor -> extractFunctionalType(projection, nullability)
        // Normal type
        is TypeConstructor -> dri.classNames?.let {
            BasicType(it, projection = projection, nullability = nullability)
        }
        // Definitely not nullable `T & Any`
        is DefinitelyNonNullable -> inner.getSerialType(projection, Nullability.DEFINITELY_NOT_NULL)
        // JavaScript object type, not currently (probably never?) supported
        Dynamic -> throw IllegalArgumentException("${DoclopediaDokkaPlugin.NAME} does not currently support JS.")
        // Java Object is treated differently for whatever reason
        is JavaObject -> BasicType(
            if (projection == null) "Object" else "?",
            projection = projection,
            nullability = nullability,
            annotations = annotations
        )
        // Nullable types
        is Nullable -> inner.getSerialType(projection, Nullability.NULLABLE)
        // Primitives are never nullable so, simple not null class
        is PrimitiveJavaType -> BasicType(
            name,
            projection = projection,
            nullability = Nullability.NOT_NULL,
            annotations = annotations
        )

        is TypeAliased -> TODO()
        // Generic types `T`
        is TypeParameter -> BasicType(
            name,
            projection = projection,
            nullability = nullability,
            annotations = annotations
        )
        // TODO: Honestly I have no idea what to do with this one
        is UnresolvedBound -> null
        // TODO: Honestly I have no idea what to do with this one either
        Void -> null
    }
}

private fun GenericTypeConstructor.extractGenericType(projection: GenericProjection?, nullability: Nullability): Type? {
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.getSerialType() }
    return BasicType(type, params, projection, presentableName, nullability, annotations)
}

private fun FunctionalTypeConstructor.extractFunctionalType(
    projection: GenericProjection?,
    nullability: Nullability,
): Type? {
    val pkg = dri.packageName ?: return null
    if ("kotlin" in pkg) {
        // KT fun
        val receiver = if (isExtensionFunction) projections.first() else null
        val returnType = projections.last()
        val rest = projections.subList(1, projections.size - 1)

        return FunctionType(
            receiver = receiver?.getSerialType(projection),
            returnType = returnType.getSerialType(projection),
            params = rest.mapNotNull { it.getSerialType(projection) },
            name = presentableName,
            isSuspendable = isSuspendable,
            nullability = nullability,
            annotations = annotations
        )
    }

    // Normally a Java function
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.getSerialType() }
    return BasicType(type, params, projection, presentableName, nullability, annotations)
}

@Suppress("UNCHECKED_CAST")
val Documentable.annotations: List<String>
    get() = this.annotations().flatMapped()

val <T : AnnotationTarget> WithExtraProperties<T>.annotations: List<String>
    get() = this.annotations().flatMapped()

private fun Map<DokkaConfiguration.DokkaSourceSet, List<Annotations.Annotation>>.flatMapped() =
    values
        .flatten()
        .map { "@${it.dri.classNames}" }
