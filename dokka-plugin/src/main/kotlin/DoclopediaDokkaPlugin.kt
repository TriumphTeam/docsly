package dev.triumphteam.doclopedia

import dev.triumphteam.doclopedia.renderer.DocDexRenderer
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

class DoclopediaDokkaPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val renderer by extending {
        CoreExtensions.renderer providing ::DocDexRenderer override dokkaBase.htmlRenderer
    }

    companion object {
        const val NAME = "Doclopedia"
    }
}
