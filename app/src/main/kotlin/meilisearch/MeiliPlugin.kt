package dev.triumphteam.docsly.meilisearch

import dev.triumphteam.docsly.config.MeiliConfig
import io.ktor.http.URLProtocol
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.application
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

public class Meili(config: Configuration) {

    public val client: MeiliClient = config.createClient()

    public class Configuration {
        public var host: String = "0.0.0.0"
        public var port: Int = 7700
        public var apiKey: String = "masterKey"
        public var protocol: URLProtocol = URLProtocol.HTTP

        public fun config(config: MeiliConfig) {
            host = config.host
            port = config.port
            apiKey = config.apiKey
            protocol = config.protocol
        }

        internal fun createClient() = MeiliClient(host, port, apiKey, protocol)
    }

    public companion object Plugin : BaseApplicationPlugin<Application, Configuration, Meili> {

        override val key: AttributeKey<Meili> = AttributeKey("Meili")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): Meili {
            return Meili(Configuration().apply(configure))
        }
    }
}


public suspend inline fun <reified T> PipelineContext<*, ApplicationCall>.search(
    index: String,
    query: String,
    filter: Map<String, String> = emptyMap(),
): List<T> = with(this.application.plugin(Meili).client) {
    return index(index).search(query, filter)
}
