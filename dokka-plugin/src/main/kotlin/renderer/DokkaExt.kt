package dev.triumphteam.doclopedia.renderer

import dev.triumphteam.doclopedia.DoclopediaDokkaPlugin
import dev.triumphteam.doclopedia.serializable.Annotation
import dev.triumphteam.doclopedia.serializable.AnnotationAnnotationArgument
import dev.triumphteam.doclopedia.serializable.AnnotationArgument
import dev.triumphteam.doclopedia.serializable.ArrayAnnotationArgument
import dev.triumphteam.doclopedia.serializable.BasicType
import dev.triumphteam.doclopedia.serializable.FunctionType
import dev.triumphteam.doclopedia.serializable.GenericProjection
import dev.triumphteam.doclopedia.serializable.LiteralAnnotationArgument
import dev.triumphteam.doclopedia.serializable.Modifier
import dev.triumphteam.doclopedia.serializable.Nullability
import dev.triumphteam.doclopedia.serializable.StarType
import dev.triumphteam.doclopedia.serializable.Type
import dev.triumphteam.doclopedia.serializable.TypeAliasType
import dev.triumphteam.doclopedia.serializable.TypedAnnotationArgument
import dev.triumphteam.doclopedia.serializable.ValueType
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.links.DriOfUnit
import org.jetbrains.dokka.model.AnnotationParameterValue
import org.jetbrains.dokka.model.AnnotationValue
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.ArrayValue
import org.jetbrains.dokka.model.ClassValue
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DefinitelyNonNullable
import org.jetbrains.dokka.model.Dynamic
import org.jetbrains.dokka.model.EnumValue
import org.jetbrains.dokka.model.ExtraModifiers
import org.jetbrains.dokka.model.FunctionalTypeConstructor
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Invariance
import org.jetbrains.dokka.model.JavaObject
import org.jetbrains.dokka.model.LiteralValue
import org.jetbrains.dokka.model.Nullable
import org.jetbrains.dokka.model.PrimitiveJavaType
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.Star
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.model.TypeParameter
import org.jetbrains.dokka.model.UnresolvedBound
import org.jetbrains.dokka.model.Void

private const val OBJECT = "Object"
private const val WILD_CARD = "?"
private const val DEPRECATED = "Deprecated"

/** The return type of a function. */
val DFunction.returnType: Type?
    get() = when {
        isConstructor -> null
        type is TypeConstructor && (type as TypeConstructor).dri == DriOfUnit -> null
        type is Void -> null
        else -> type.toSerialType()
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
        Dynamic -> throw IllegalArgumentException("${DoclopediaDokkaPlugin.NAME} does not currently support JS.")
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
    if ("kotlin" in pkg) {
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

/** Flattens and maps the annotations into a string List. */
fun Map<DokkaConfiguration.DokkaSourceSet, List<Annotations.Annotation>>.toSerialAnnotations() =
    values.flatten().filter(Annotations.Annotation::mustBeDocumented).mapNotNull(Annotations.Annotation::mapAnnotation)

/** Maps the [Annotations.Annotation] into the serializable [Annotation]. */
private fun Annotations.Annotation.mapAnnotation(): Annotation? {
    val type = dri.classNames ?: return null

    /** Local function to simplify recursion. */
    fun AnnotationParameterValue.mapParams(): AnnotationArgument? {
        return when (this) {
            is LiteralValue -> LiteralAnnotationArgument(text())
            is AnnotationValue -> annotation.mapAnnotation()?.let(::AnnotationAnnotationArgument)
            is ArrayValue -> ArrayAnnotationArgument(value.mapNotNull(AnnotationParameterValue::mapParams))
            is ClassValue -> TypedAnnotationArgument(className, ValueType.CLASS)
            is EnumValue -> TypedAnnotationArgument(enumName, ValueType.ENUM)
        }
    }

    val arguments = params.mapNotNull { (key, value) ->
        value.mapParams()?.let { key to it }
    }.toMap()

    return Annotation(type, arguments)
}

fun Map<DokkaConfiguration.DokkaSourceSet, Set<ExtraModifiers.KotlinOnlyModifiers>>.toSerialModifiers() =
    values.flatten().mapNotNull { Modifier.fromString(it.name) }
