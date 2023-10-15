package dev.triumphteam.docsly.controller

import dev.triumphteam.docsly.database.entity.DocumentEntity
import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.project.DocumentSearchResult
import dev.triumphteam.docsly.project.IndexDocument
import dev.triumphteam.docsly.project.Projects
import dev.triumphteam.docsly.resource.GuildApi
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.plugin
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import org.jetbrains.exposed.sql.transactions.transaction

public fun Routing.apiGuild() {
    val defaults = plugin(Defaults)
    val projects = plugin(Projects)
    val meili = plugin(Meili)

    post<GuildApi.Guild.Setup> { api ->
        // If it goes well nothing will throw and it'll work well!
        projects.setup(api.parent.guild, defaults)

        // So we return "accepted"
        call.respond(HttpStatusCode.Accepted)
    }

    get<GuildApi.Guild.Projects> { api ->
        call.respond(projects.getProjects(api.parent.guild))
    }

    get<GuildApi.Guild.Search> { api ->
        val project = projects.getProject(api.parent.guild, api.project, api.version) ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val index = meili.client.index(Projects.indexKeyFor(api.parent.guild, project))

        val result = index.search<IndexDocument>(api.query, null)
            .map { DocumentSearchResult(it.references.first(), it.id) }
            .take(20)

        call.respond(result)
    }

    get<GuildApi.Guild.Document> { api ->
        val document = transaction {
            DocumentEntity[api.id]
        }

        call.respond<DocElement>(document.document)
    }
}
