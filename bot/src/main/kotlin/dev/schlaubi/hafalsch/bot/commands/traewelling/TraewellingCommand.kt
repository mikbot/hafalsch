package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.schlaubi.hafalsch.bot.core.HafalschModule

suspend fun HafalschModule.traewellingCommand() = publicSlashCommand {
    name = "träwelling"
    description = "<unused>"

    loginCommand()
    logoutCommand()
}
