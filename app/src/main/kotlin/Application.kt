package dev.triumphteam.docsly

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database

private val config = createOrGetConfig()

fun main() {
    embeddedServer(Netty, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

fun Application.module() {
    val postgres = config.postgres

    val hikari = HikariDataSource(
        HikariConfig().apply {
            dataSourceClassName = "com.impossibl.postgres.jdbc.PGDataSource"
            addDataSourceProperty("host", postgres.host)
            addDataSourceProperty("port", postgres.port)
            addDataSourceProperty("user", postgres.username)
            addDataSourceProperty("password", postgres.password)
            addDataSourceProperty("databaseName", postgres.database)
        }
    )

    Database.connect(hikari)
}
