package dev.triumphteam.docsly.kord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.triumphteam.docsly.kord.client.DocslyClient
import io.ktor.http.HttpStatusCode

public class SetupCommand(
    kord: Kord,
    private val docslyClient: DocslyClient,
) {

    init {

        kord.on<GuildCreateEvent> {
            kord.createGuildChatInputCommand(guild.id, "setup", "Temporary setup command.")
        }

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != "setup") return@on
            onCommand()
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.onCommand() {
        val defer = interaction.deferPublicResponse()
        interaction.respondPublic {

        }

        val response = docslyClient.setup(interaction.guildId)

        defer.respond {
            content = when (response.status) {
                HttpStatusCode.Accepted -> "Done!"
                else -> "OOPSIE WOOPSIE!! Uwu we made a fucky wucky."
            }
        }
    }
}
