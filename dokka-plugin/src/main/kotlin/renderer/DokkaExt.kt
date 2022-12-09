package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.serializable.BasicType
import dev.triumphteam.doclopedia.serializable.FunctionType
import dev.triumphteam.doclopedia.serializable.GenericProjection
import dev.triumphteam.doclopedia.serializable.StarType
import dev.triumphteam.doclopedia.serializable.Type
import org.jetbrains.dokka.links.DriOfUnit
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DefinitelyNonNullable
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

fun DFunction.returnType(): Type? = when {
    isConstructor -> null
    type is TypeConstructor && (type as TypeConstructor).dri == DriOfUnit -> null
    type is Void -> null
    else -> type.getSerialType()
}

fun Projection.getSerialType(projection: GenericProjection? = null, nullable: Boolean = false): Type? {
    return when (this) {
        // Generic `in`
        is Contravariance<*> -> inner.getSerialType(GenericProjection.IN, nullable)
        // Generic `out`
        is Covariance<*> -> inner.getSerialType(GenericProjection.OUT, nullable)
        // Normal generic
        is Invariance<*> -> inner.getSerialType(nullable = nullable)
        // Wildcard
        Star -> StarType
        // A generic typed-type
        is GenericTypeConstructor -> extractGenericType(projection, nullable)
        is FunctionalTypeConstructor -> extractFunctionalType(projection, nullable)
        is TypeConstructor -> dri.classNames?.let { BasicType(it, nullable = nullable) }
        is DefinitelyNonNullable -> TODO()
        Dynamic -> TODO()
        is JavaObject -> TODO()
        is Nullable -> inner.getSerialType(projection, true)
        is PrimitiveJavaType -> TODO()
        is TypeAliased -> TODO()
        is TypeParameter -> TODO()
        is UnresolvedBound -> TODO()
        Void -> null
    }
}

private fun GenericTypeConstructor.extractGenericType(projection: GenericProjection?, nullable: Boolean): Type? {
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.getSerialType() }
    return BasicType(type, params, projection, presentableName, nullable)
}

private fun FunctionalTypeConstructor.extractFunctionalType(projection: GenericProjection?, nullable: Boolean): Type? {
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
            nullable = nullable
        )
    }

    // Normally a Java function
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.getSerialType() }
    return BasicType(type, params, projection, presentableName, nullable)
}