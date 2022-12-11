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
