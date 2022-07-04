package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.hafalsch.bot.command.optionalDate
import dev.schlaubi.hafalsch.bot.command.optionalStation
import dev.schlaubi.hafalsch.bot.command.profile
import dev.schlaubi.hafalsch.bot.ui.JourneySource
import dev.schlaubi.hafalsch.bot.ui.asUIContext
import dev.schlaubi.hafalsch.bot.ui.journey
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.inject

class JourneyArguments : Arguments(), KordExKoinComponent, JourneySource {
    private val marudor by inject<Marudor>()

    override val name by string {
        name = "name"
        description = "commands.journey.arguments.name.description"

        autoComplete {
            val input = focusedOption.safeInput

            coroutineScope {
                val matches =
                    runCatching { marudor.hafas.journeyMatch(input.substringBefore(' ')) }.getOrElse { emptyList() }
                val results = matches.sortedBy {
                    it.train.name.withIndex().count { (index, value) -> input.getOrNull(index) == value }
                }

                suggestString {
                    results.take(25).forEach {
                        choice("${it.train.name} -> ${it.lastStop.station.title}", it.train.name)
                    }
                }
            }

        }
    }

    override val station by optionalStation {
        name = "station"
        description = "commands.journey.arguments.station.description"
    }

    override val date by optionalDate {
        name = "date"
        description = "commands.journey.arguments.date.description"
    }

    override val profile by profile()
}

suspend fun Extension.journeyCommand() = publicSlashCommand(::JourneyArguments) {
    name = "commands.journey.name"
    description = "commands.journey.description"

    action {
        asUIContext {
            journey(arguments)
        }
    }
}

