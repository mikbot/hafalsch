package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.command.station
import dev.schlaubi.hafalsch.bot.core.sendStation
import dev.schlaubi.hafalsch.bot.ui.asUIContext

class StationArguments : Arguments() {
    val station by station {
        name = "station"
        description = "command.station.arguments.station.description"
    }
}

suspend fun Extension.stationCommand() = publicSlashCommand(::StationArguments) {
    name = "commands.station.name"
    description = "command.station.description"

    action {
        val station = arguments.station

        respond {
            asUIContext {
                sendStation(station)
            }
        }
    }
}
