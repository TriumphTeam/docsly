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
package dev.triumphteam.docsly.meilisearch

import dev.triumphteam.docsly.config.MeiliConfig
import io.ktor.http.URLProtocol
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

public class Meili(config: Configuration) {

    public val client: MeiliClient = config.createClient()

    public class Configuration {
        private var host: String = "0.0.0.0"
        private var port: Int = 7700
        private var apiKey: String = "masterKey"
        private var protocol: URLProtocol = URLProtocol.HTTP

        public fun from(config: MeiliConfig) {
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
    filter: String? = null,
): List<T> = with(context.application.plugin(Meili).client) {
    return index(index).search(query, filter)
}

public suspend inline fun PipelineContext<*, ApplicationCall>.index(index: String): MeiliClient.Index =
    context.application.plugin(Meili).client.index(index)
