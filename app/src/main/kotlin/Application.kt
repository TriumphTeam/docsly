package dev.triumphteam.docsly

import com.meilisearch.sdk.Client
import com.meilisearch.sdk.Config
import com.meilisearch.sdk.SearchRequest
import com.zaxxer.hikari.HikariDataSource
import dev.triumphteam.docsly.config.createOrGetConfig
import dev.triumphteam.docsly.database.TestTable
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.meilisearch.annotation.PrimaryKey
import dev.triumphteam.docsly.serializable.SerializableAnnotation
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private val config = createOrGetConfig()

public fun main() {
    runBlocking {
        val client = Meili("http://localhost", 7700, "masterKey")
        val index = client.index("ass")

        /*while (true) {
            println("Query to search:")
            val query = readln()
            val result = measureTimedValue { index.search<Quote>(query) }

            println(result.value)
            println("Results in ${result.duration}.\n")
        }*/
        index.delete()
    }

    return
    val oldClient = Client(Config("http://localhost:7700", "masterKey"))
    oldClient.index("quotes").search(SearchRequest())
    oldClient.deleteIndex("movies")
    // embeddedServer(Netty, port = config.port, host = config.host, module = Application::module).start(wait = true)
}

@Serializable
public data class Test(
    public val boy: String,
    @PrimaryKey public val ass: String,
)

@Serializable
public data class Quote(public val id: Int, public val quote: String, public val author: String)

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
