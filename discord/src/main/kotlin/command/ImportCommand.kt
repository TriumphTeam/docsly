package dev.triumphteam.docsly.kord.command

import dev.triumphteam.cmd.core.annotations.Command
import dev.triumphteam.cmd.core.annotations.Optional
import dev.triumphteam.cmds.kord.sender.SlashSender

@Command("import")
public class ImportCommand {

    @Command
    public fun execute(
        sender: SlashSender,
        project: String,
        version: String,
        @Optional latest: Boolean?,
    ) {

    }
}
