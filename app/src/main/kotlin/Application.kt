package dev.triumphteam.docsly

import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database

private val config = createOrGetConfig()

public fun main() {
    embeddedServer(Netty, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

public fun Application.module() {
    Database.connect(HikariDataSource(config.postgres.toHikariConfig()))
}
