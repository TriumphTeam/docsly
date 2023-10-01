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
package dev.triumphteam.docsly

import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.resource.Api
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

private val config = createOrGetConfig()

public fun main() {
    embeddedServer(Netty, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

public fun Application.module() {
    Database.connect(HikariDataSource(config.postgres.toHikariConfig()))

    install(Meili) { config(config.meili) }
    install(Resources)

    routing {
        get<Api.Index.Search> {
            // Here you handle the "api/{index}/search" endpoint

            // Getting the index passed
            // search<String>(it.parent.index, "")
            transaction {
                // DocDao.find { DocsTable.name eq "Ass" }.firstOrNull()
            }
        }
    }
}
