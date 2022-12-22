package dev.triumphteam.docsly.resource

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

/** Resource location for "/api". */
@Serializable
@Resource("/api")
public class Api {

    /** Resource location for "/api/[index]". */
    @Serializable
    @Resource("{index}")
    public class Index(public val parent: Api = Api(), public val index: String) {

        /** Resource location for "/api/[index]/search". */
        @Serializable
        @Resource("/search")
        public class Search(public val parent: Index)
    }
}
