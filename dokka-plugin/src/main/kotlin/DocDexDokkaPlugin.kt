package dev.triumphteam.doclopedia

import dev.triumphteam.doclopedia.location.DocDexLocationProvider
import dev.triumphteam.doclopedia.renderer.BriefCommentPreprocessor
import dev.triumphteam.doclopedia.renderer.DocDexRenderer
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.renderers.PackageListCreator
import org.jetbrains.dokka.base.renderers.RootCreator
import org.jetbrains.dokka.base.resolvers.shared.RecognizedLinkFormat
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.transformers.pages.PageTransformer

class DocDexDokkaPlugin : DokkaPlugin() {

    val preprocessors by extensionPoint<PageTransformer>()

    private val dokkaBase by lazy { plugin<DokkaBase>() }


    val renderer by extending {
        CoreExtensions.renderer providing ::DocDexRenderer override dokkaBase.htmlRenderer
    }

    val locationProvider by extending {
        dokkaBase.locationProviderFactory providing DocDexLocationProvider::Factory override dokkaBase.locationProvider
    }

    val rootCreator by extending {
        preprocessors with RootCreator
    }

    val briefCommentPreprocessor by extending {
        preprocessors with BriefCommentPreprocessor()
    }

    val packageListCreator by extending {
        (preprocessors
                providing { PackageListCreator(it, RecognizedLinkFormat.DokkaGFM) }
                order { after(rootCreator) })
    }
}
