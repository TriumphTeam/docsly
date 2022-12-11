package dev.triumphteam.doclopedia

import dev.triumphteam.doclopedia.renderer.JsonRenderer
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

class DoclopediaPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val renderer by extending {
        CoreExtensions.renderer providing ::JsonRenderer override dokkaBase.htmlRenderer
    }
}
