package dev.triumphteam.docsly.controller

import dev.triumphteam.docsly.api.GuildSetupRequest
import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.project.Projects
import dev.triumphteam.docsly.resource.GuildApi
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.plugin
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing

public fun Routing.apiGuild() {

    val defaults = plugin(Defaults)
    val projects = plugin(Projects)

    post<GuildApi.Guild.Setup> { setup ->
        val guild = setup.parent.guild
        val setupDefaults = call.receive<GuildSetupRequest>().defaults

        val defaultProjects = defaults.defaultProjects()

        // Validate data
        setupDefaults.forEach { (project, versions) ->
            val defaultVersions = defaultProjects[project] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid default project '$project'.")
                return@post
            }

            versions.forEach { version ->
                // TODO: Figure better way to get version, and not use folder name
                val replaced = version.replace(".", "_")
                if (replaced !in defaultVersions) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid default project version '$version' for project '$project'.")
                    return@post
                }
            }
        }

        // If it goes well nothing will throw and it'll work well!
        projects.setupProjects(guild, setupDefaults)

        // So we return "accepted"
        call.respond(HttpStatusCode.Accepted)
    }
}
