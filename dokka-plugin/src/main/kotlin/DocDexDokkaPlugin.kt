package dev.triumphteam.doclopedia

import dev.triumphteam.doclopedia.renderer.DocDexRenderer
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.plugability.DokkaPlugin

class DocDexDokkaPlugin : DokkaPlugin() {

    val renderer by extending {
        CoreExtensions.renderer providing ::DocDexRenderer
    }
}
