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
import dev.triumphteam.docsly.controller.apiGuild
import dev.triumphteam.docsly.database.entity.DocsTable
import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.project.Projects
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

private val config = createOrGetConfig()

public fun main() {
    embeddedServer(CIO, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

public fun Application.module() {
    Database.connect(HikariDataSource(config.postgres.toHikariConfig()))

    transaction {
        SchemaUtils.create(DocsTable)
    }

    /*install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
    }*/

    install(Resources)
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Meili) { from(config.meili) }
    install(Defaults)
    install(Projects)

    routing {
        // Setup guild api/routing
        apiGuild()

        /*get<Api.Index.Search> {
            // Here you handle the "api/{index}/search" endpoint

            // Getting the index passed
            *//*val test = index("test").searchFull<String>("e", null)
            // val test = search<List<Test>>(it.parent.index, "e")
            println(test)*//*

            index("test").addDocuments(
                listOf(
                    Test("Hello"),
                    Test("there"),
                    Test("thing"),
                ),
                primaryKey = "boy",
            )

            call.respond(HttpStatusCode.Accepted)
        }*/
    }
}

@Serializable
public data class Test(val boy: String)
