package dev.triumphteam.docsly.kord

import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.triumphteam.docsly.kord.client.DocslyClient
import dev.triumphteam.docsly.kord.command.SearchCommand
import dev.triumphteam.docsly.kord.command.SetupCommand
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Application::class.java)

public class Application

public suspend fun main() {
    val kord = Kord(System.getenv("DISCORD_TOKEN"))

    logger.info("Logging in!")

    val docslyClient = DocslyClient()

    SetupCommand(kord, docslyClient)
    SearchCommand(kord, docslyClient)

    kord.login {
        this.intents = Intents(Intent.Guilds, Intent.DirectMessages, Intent.GuildMessages)
    }
}
