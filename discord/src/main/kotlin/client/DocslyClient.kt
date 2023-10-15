package dev.triumphteam.docsly.kord.client

import dev.kord.common.entity.Snowflake
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.project.DocumentSearchResult
import dev.triumphteam.docsly.project.ProjectData
import dev.triumphteam.docsly.resource.GuildApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

public class DocslyClient {

    public val client: HttpClient = HttpClient(CIO) {
        install(Resources)
        install(ContentNegotiation) { json() }
        /*install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }*/

        defaultRequest {
            this.host = "localhost"
            this.port = 8080

            contentType(ContentType.Application.Json)
        }
    }

    public suspend fun setup(guild: Snowflake): HttpResponse {
        return client.post<GuildApi.Guild.Setup>(GuildApi.Guild.Setup(GuildApi.Guild(guild = guild.value.toString())))
    }

    public suspend fun getProjects(guild: Snowflake): List<ProjectData> {
        return client.get<GuildApi.Guild.Projects>(GuildApi.Guild.Projects(GuildApi.Guild(guild = guild.value.toString())))
            .body<List<ProjectData>>()
    }

    public suspend fun search(guild: Snowflake, project: String, version: String?, query: String): List<DocumentSearchResult> {
        return client.get<GuildApi.Guild.Search>(
            GuildApi.Guild.Search(
                GuildApi.Guild(
                    guild = guild.value.toString(),
                ),
                project = project,
                query = query,
                version = version,
            )
        ).body<List<DocumentSearchResult>>()
    }

    public suspend fun getDocument(guild: Snowflake, id: Long): DocElement {
        return client.get<GuildApi.Guild.Document>(
            GuildApi.Guild.Document(
                GuildApi.Guild(guild = guild.value.toString()),
                id = id,
            )
        ).body<DocElement>()
    }
}
