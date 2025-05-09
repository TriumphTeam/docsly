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

import dev.triumphteam.docsly.KOTLIN_EXTENSION
import dev.triumphteam.docsly.elements.Language
import dev.triumphteam.docsly.elements.Modifier
import dev.triumphteam.docsly.elements.Path
import dev.triumphteam.docsly.elements.Visibility
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.AdditionalModifiers
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.ExtraModifiers
import org.jetbrains.dokka.model.WithSources
import org.jetbrains.dokka.model.WithVisibility
import org.jetbrains.dokka.model.properties.WithExtraProperties
import kotlin.collections.flatten

/** Simple extension to turn Dokka modifiers into serializable ones. */
public fun Map<DokkaConfiguration.DokkaSourceSet, Set<ExtraModifiers>>.toSerialModifiers(): List<Modifier> =
    values.flatten().mapNotNull { Modifier.fromString(it.name) }

public val <T : Documentable> WithExtraProperties<T>.extraModifiers: List<Modifier>
    get() = extra[AdditionalModifiers]?.content?.values?.flatMap { set ->
        set.mapNotNull { Modifier.fromString(it.name) }
    } ?: emptyList()

/** Simple extension get the final visibility of a [WithVisibility] documentable. */
public val WithVisibility.finalVisibility: Visibility
    get() = visibility.values.firstOrNull()?.name?.let { Visibility.fromString(it) } ?: Visibility.PUBLIC

/** Small hack to get the language of the documentable we're currently serializing. */
public val WithSources.language: Language
    get() = if (sources.values.firstOrNull()?.path?.endsWith(
            KOTLIN_EXTENSION,
            true
        ) == true
    ) {
        Language.KOTLIN
    } else {
        Language.JAVA
    }

/** Gets the actual path from a [DRI] object. */
public fun DRI.toPath(): Path = Path(packageName, classNames?.split('.'))
