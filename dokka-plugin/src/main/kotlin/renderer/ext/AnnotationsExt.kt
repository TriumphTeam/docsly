package dev.triumphteam.doclopedia.renderer.ext

import dev.triumphteam.doclopedia.serializable.Annotation
import dev.triumphteam.doclopedia.serializable.AnnotationAnnotationArgument
import dev.triumphteam.doclopedia.serializable.AnnotationArgument
import dev.triumphteam.doclopedia.serializable.ArrayAnnotationArgument
import dev.triumphteam.doclopedia.serializable.LiteralAnnotationArgument
import dev.triumphteam.doclopedia.serializable.TypedAnnotationArgument
import dev.triumphteam.doclopedia.serializable.ValueType
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.AnnotationParameterValue
import org.jetbrains.dokka.model.AnnotationValue
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.ArrayValue
import org.jetbrains.dokka.model.ClassValue
import org.jetbrains.dokka.model.EnumValue
import org.jetbrains.dokka.model.LiteralValue

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
