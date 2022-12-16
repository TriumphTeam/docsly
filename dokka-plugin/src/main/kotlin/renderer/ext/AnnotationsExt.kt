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
package dev.triumphteam.docsly.renderer.ext

import dev.triumphteam.docsly.serializable.AnnotationAnnotationArgument
import dev.triumphteam.docsly.serializable.AnnotationValueType
import dev.triumphteam.docsly.serializable.ArrayAnnotationArgument
import dev.triumphteam.docsly.serializable.LiteralAnnotationArgument
import dev.triumphteam.docsly.serializable.SerializableAnnotation
import dev.triumphteam.docsly.serializable.SerializableAnnotationArgument
import dev.triumphteam.docsly.serializable.TypedAnnotationArgument
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

/** Maps the [Annotations.Annotation] into the serializable [SerializableAnnotation]. */
private fun Annotations.Annotation.mapAnnotation(): SerializableAnnotation? {
    val type = dri.classNames ?: return null

    /** Local function to simplify recursion. */
    fun AnnotationParameterValue.mapParams(): SerializableAnnotationArgument? {
        return when (this) {
            is LiteralValue -> LiteralAnnotationArgument(text())
            is AnnotationValue -> annotation.mapAnnotation()?.let(::AnnotationAnnotationArgument)
            is ArrayValue -> ArrayAnnotationArgument(value.mapNotNull(AnnotationParameterValue::mapParams))
            is ClassValue -> TypedAnnotationArgument(className, AnnotationValueType.CLASS)
            is EnumValue -> TypedAnnotationArgument(enumName, AnnotationValueType.ENUM)
        }
    }

    val arguments = params.mapNotNull { (key, value) ->
        value.mapParams()?.let { key to it }
    }.toMap()

    return SerializableAnnotation(type, arguments)
}
