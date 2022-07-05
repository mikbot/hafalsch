package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

suspend fun Extension.traewellingCommand() = publicSlashCommand {
    name = "träwelling"
    description = "<unused>"

    loginCommand()
    logoutCommand()
    simpleCheckInCommand()
    extendedCheckInCommand()
}
