package dev.triumphteam.docsly

import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.meilisearch.search
import dev.triumphteam.docsly.resource.Api
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

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
            search<String>(it.parent.index, "")
        }
    }
}
