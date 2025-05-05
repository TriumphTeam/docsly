package dev.triumphteam.docsly.kord

import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.triumphteam.cmds.kord.KordCommandManager
import dev.triumphteam.docsly.kord.client.DocslyClient
import dev.triumphteam.docsly.kord.command.ImportCommand
import dev.triumphteam.docsly.kord.command.SearchCommand
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Application::class.java)

public class Application

public suspend fun main() {
    val kord = Kord(System.getenv("DISCORD_TOKEN"))

    logger.info("Logging in!")

    val commandManager = KordCommandManager(kord)

    /*commandManager.registerSuggestion(SuggestionKey.of("search")) { _ ->
        listOf("Search for a doc.")
    }*/

    kord.on<GuildCreateEvent> {
        commandManager.registerCommand(guild.id, SearchCommand())
        commandManager.registerCommand(guild.id, ImportCommand())
    }

    val docslyClient = DocslyClient()

    kord.login {
        this.intents = Intents(Intent.Guilds, Intent.DirectMessages, Intent.GuildMessages)
    }
}
