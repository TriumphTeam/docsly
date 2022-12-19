package dev.triumphteam.docsly

import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import dev.triumphteam.docsly.database.TestTable
import dev.triumphteam.docsly.serializable.SerializableAnnotation
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private val config = createOrGetConfig()

public fun main() {
    embeddedServer(Netty, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

public fun Application.module() {
    Database.connect(HikariDataSource(config.postgres.toHikariConfig()))

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            TestTable
        )

        TestTable.insert {
            it[annotations] = listOf(SerializableAnnotation("Hello!"))
        }

        println(
            TestTable.selectAll().firstOrNull()?.get(TestTable.annotations)
        )
    }
}
