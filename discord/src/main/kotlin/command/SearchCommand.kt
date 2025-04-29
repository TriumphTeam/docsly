package dev.triumphteam.docsly.kord.command

import dev.triumphteam.cmd.core.annotations.Command
import dev.triumphteam.cmd.core.annotations.Suggestion
import dev.triumphteam.cmds.kord.sender.SlashSender

@Command("search")
public class SearchCommand {

    @Command
    public fun execute(sender: SlashSender, @Suggestion("search") query: String) {

    }
}
