package dev.triumphteam.docsly.kord.command

import dev.triumphteam.cmd.core.annotations.Command
import dev.triumphteam.cmd.core.annotations.Suggestion
import dev.triumphteam.cmds.kord.sender.CommandSender

@Command("search")
public class SearchCommand {

    @Command
    public fun execute(sender: CommandSender, @Suggestion("search") query: String) {

    }
}
