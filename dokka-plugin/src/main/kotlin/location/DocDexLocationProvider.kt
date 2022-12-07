package dev.triumphteam.doclopedia.location

import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext

class DocDexLocationProvider(
    pageGraphRoot: RootPageNode,
    dokkaContext: DokkaContext,
) : DokkaLocationProvider(pageGraphRoot, dokkaContext, ".json") {

    class Factory(private val context: DokkaContext) : LocationProviderFactory {
        override fun getLocationProvider(pageNode: RootPageNode) =
            DocDexLocationProvider(pageNode, context)
    }
}