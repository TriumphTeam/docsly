package dev.triumphteam.docsly.kord.command

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.Choice
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.GuildAutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.triumphteam.docsly.kord.client.DocslyClient
import dev.triumphteam.docsly.project.ProjectData
import dev.triumphteam.docsly.renderer.DiscordDocumentRenderer

public class OldSearchCommand(
    private val kord: Kord,
    private val docslyClient: DocslyClient,
) {

    private val projectsCache = Caffeine.newBuilder().build<Snowflake, List<ProjectData>>()
    private val renderer = DiscordDocumentRenderer()

    init {
        kord.on<GuildCreateEvent> {
            kord.createGuildChatInputCommand(guild.id, "search", "Search for a doc.") {
                string("project", "The project to search the docs for.") {
                    required = true
                    autocomplete = true
                }

                string("query", "Search query.") {
                    required = true
                    autocomplete = true
                }

                string("version", "The version of the project.") {
                    required = false
                    autocomplete = true
                }
            }
        }

        kord.on<GuildAutoCompleteInteractionCreateEvent> { onCommandSuggestion() }

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != "search") return@on
            onCommand()
        }
    }

    private suspend fun GuildAutoCompleteInteractionCreateEvent.onCommandSuggestion() {
        if (interaction.command.rootName != "search") return

        val guildId = interaction.guildId

        // Gets a list of projects and their version
        val projects = projectsCache.getIfPresent(guildId) ?: docslyClient.getProjects(guildId).also {
            projectsCache.put(guildId, it)
        }

        val focusedOption = interaction.command.options.entries.firstOrNull { it.value.focused }?.key
        val focusedValue = interaction.focusedOption.value

        when (focusedOption) {
            "project" -> {
                val list = projects.map {
                    Choice.StringChoice(
                        it.name,
                        Optional.Missing(),
                        it.name,
                    )
                }

                interaction.suggest(
                    if (focusedValue.isEmpty()) {
                        list
                    } else {
                        list.filter { it.name.startsWith(focusedValue) }
                    }
                )
            }

            "version" -> {
                val typedProject = interaction.command.options["project"]?.value as? String
                val versions = projects.find { it.name == typedProject }?.versions ?: emptyList()

                val list = versions.map {
                    Choice.StringChoice(it, Optional.Missing(), it)
                }

                interaction.suggest(
                    if (focusedValue.isEmpty()) {
                        list
                    } else {
                        list.filter { it.name.startsWith(focusedValue) }
                    }
                )
            }

            "query" -> {
                val typedProject = interaction.command.options["project"]?.value as? String ?: ""
                val typedVersion = interaction.command.options["version"]?.value as? String

                val list = docslyClient.search(guildId, typedProject, typedVersion, focusedValue).map { result ->
                    Choice.StringChoice(result.value, Optional.Missing(), result.id.toString())
                }

                interaction.suggest(list)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.onCommand() {
        val defer = interaction.deferPublicResponse()

        val document = interaction.command.options["query"]?.value as? String ?: ""

        val id = document.toLongOrNull() ?: run {
            defer.respond {
                content = "uh"
            }
            return
        }

        val docElement = docslyClient.getDocument(interaction.guildId, id)

        defer.respond {
            content = renderer.render(docElement)
        }
    }
}
