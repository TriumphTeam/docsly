package dev.triumphteam.docsly.kord.command

import dev.triumphteam.cmd.core.annotations.Command
import dev.triumphteam.cmd.core.annotations.Optional
import dev.triumphteam.cmds.kord.sender.CommandSender

@Command("import")
public class ImportCommand {

    @Command
    public fun execute(
        sender: CommandSender,
        project: String,
        version: String,
        @Optional latest: Boolean?,
    ) {

    }
}
