package dev.triumphteam.doclopedia.renderer

import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.pages.recursiveMapTransform
import org.jetbrains.dokka.transformers.pages.PageTransformer

class BriefCommentPreprocessor : PageTransformer {
    override fun invoke(input: RootPageNode) = input.transformContentPagesTree { contentPage ->
        contentPage.modified(content = contentPage.content.recursiveMapTransform<ContentGroup, ContentNode> {
            if (it.dci.kind == ContentKind.BriefComment) {
                it.copy(style = it.style + setOf(TextStyle.Block))
            } else {
                it
            }
        })
    }
}