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
package dev.triumphteam.doclopedia.renderer.ext

import dev.triumphteam.doclopedia.KOTLIN
import dev.triumphteam.doclopedia.OBJECT
import dev.triumphteam.doclopedia.PLUGIN_NAME
import dev.triumphteam.doclopedia.WILD_CARD
import dev.triumphteam.doclopedia.serializable.BasicType
import dev.triumphteam.doclopedia.serializable.FunctionType
import dev.triumphteam.doclopedia.serializable.GenericProjection
import dev.triumphteam.doclopedia.serializable.GenericType
import dev.triumphteam.doclopedia.serializable.Nullability
import dev.triumphteam.doclopedia.serializable.StarType
import dev.triumphteam.doclopedia.serializable.Type
import dev.triumphteam.doclopedia.serializable.TypeAliasType
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.modifiers
import org.jetbrains.dokka.links.DriOfUnit
import org.jetbrains.dokka.model.Bound
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
import org.jetbrains.dokka.model.WithGenerics

/** The return type of the function. */
val DFunction.returnType: Type?
    get() = when {
        isConstructor -> null
        type is TypeConstructor && (type as TypeConstructor).dri == DriOfUnit -> null
        type is Void -> null
        else -> type.toSerialType()
    }

/** Gets the generics of a [WithGenerics] documentable mapped to the serial type. */
val WithGenerics.serialGenerics: List<GenericType>
    get() = generics.map {
        GenericType(
            name = it.name,
            constraints = it.bounds.mapNotNull(Bound::toSerialType),
            modifiers = it.modifiers().toSerialModifiers(),
        )
    }

/** Turns the projection type into a serial type recursively. */
fun Projection.toSerialType(
    projection: GenericProjection? = null,
    nullability: Nullability = Nullability.NOT_NULL,
): Type? {
    return when (this) {
        // Generic `in`
        is Contravariance<*> -> inner.toSerialType(GenericProjection.IN, nullability)
        // Generic `out`
        is Covariance<*> -> inner.toSerialType(GenericProjection.OUT, nullability)
        // Normal generic
        is Invariance<*> -> inner.toSerialType(nullability = nullability)
        // Wildcard
        Star -> StarType
        // A generic typed-type
        is GenericTypeConstructor -> toBasicType(projection, nullability)
        // Functional type
        is FunctionalTypeConstructor -> toFunctionalType(projection, nullability)
        // Normal type
        is TypeConstructor -> dri.classNames?.let {
            BasicType(it, projection = projection, nullability = nullability)
        }
        // Definitely not nullable `T & Any`
        is DefinitelyNonNullable -> inner.toSerialType(projection, Nullability.DEFINITELY_NOT_NULL)
        // JavaScript object type, not currently (probably never?) supported
        Dynamic -> throw IllegalArgumentException("$PLUGIN_NAME does not currently support JS.")
        // Java Object is treated differently for whatever reason
        is JavaObject -> BasicType(
            if (projection == null) OBJECT else WILD_CARD,
            projection = projection,
            nullability = nullability,
        )
        // Nullable types
        is Nullable -> inner.toSerialType(projection, Nullability.NULLABLE)
        // Primitives are never nullable so, simple not null class
        is PrimitiveJavaType -> BasicType(
            name,
            projection = projection,
            nullability = Nullability.NOT_NULL,
            annotations = annotations().toSerialAnnotations()
        )
        // Type alias contains both the alias and original type
        is TypeAliased -> {
            val alias = typeAlias.toSerialType(projection, nullability) ?: return null
            val type = inner.toSerialType(projection, nullability) ?: return null
            TypeAliasType(alias, type)
        }
        // Generic types `T`
        is TypeParameter -> BasicType(
            name,
            projection = projection,
            nullability = nullability,
            annotations = annotations().toSerialAnnotations()
        )
        // TODO: Honestly I have no idea what to do with this one
        is UnresolvedBound -> null
        // TODO: Honestly I have no idea what to do with this one either
        Void -> null
    }
}

/** Turns the [GenericTypeConstructor] into a [BasicType]. */
private fun GenericTypeConstructor.toBasicType(projection: GenericProjection?, nullability: Nullability): Type? {
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.toSerialType() }
    return BasicType(
        type,
        params,
        projection,
        presentableName,
        nullability,
        annotations().toSerialAnnotations()
    )
}

/** Functional type are treated slightly different between Kotlin and Java. */
private fun FunctionalTypeConstructor.toFunctionalType(
    projection: GenericProjection?,
    nullability: Nullability,
): Type? {
    val pkg = dri.packageName ?: return null
    if (KOTLIN in pkg) {
        // KT fun
        val receiver = if (isExtensionFunction) projections.first() else null
        val returnType = projections.last()
        val rest = projections.subList(if (receiver == null) 0 else 1, projections.size - 1)

        return FunctionType(
            receiver = receiver?.toSerialType(projection),
            returnType = returnType.toSerialType(projection),
            params = rest.mapNotNull { it.toSerialType(projection) },
            name = presentableName,
            isSuspendable = isSuspendable,
            nullability = nullability,
            annotations = annotations().toSerialAnnotations()
        )
    }

    // Normally a Java function
    val type = dri.classNames ?: return null
    val params = projections.mapNotNull { it.toSerialType() }
    return BasicType(
        type,
        params,
        projection,
        presentableName,
        nullability,
        annotations().toSerialAnnotations()
    )
}
