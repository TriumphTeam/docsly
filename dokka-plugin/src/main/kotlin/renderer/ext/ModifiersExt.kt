package dev.triumphteam.doclopedia.renderer.ext

import dev.triumphteam.doclopedia.serializable.Modifier
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.ExtraModifiers

fun Map<DokkaConfiguration.DokkaSourceSet, Set<ExtraModifiers.KotlinOnlyModifiers>>.toSerialModifiers() =
    values.flatten().mapNotNull { Modifier.fromString(it.name) }
