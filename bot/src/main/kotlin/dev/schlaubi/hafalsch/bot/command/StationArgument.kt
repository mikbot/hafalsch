package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.Station
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import org.koin.core.component.inject

@Converter(
    "station",

    types = [ConverterType.SINGLE, ConverterType.OPTIONAL]
)
class StationConverter(validator: Validator<Station> = null) : AutoCompletingArgument<Station>(validator) {
    val marudor by inject<Marudor>()
    override val signatureTypeString: String = "Station"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        val station = if (text.startsWith("eva:")) {
            val eva = text.substringAfter("eva:")
            marudor.stopPlace.byEva(eva)
        } else {
            marudor.stopPlace.search(text, 1).firstOrNull()
        }

        if (station == null) {
            discordError(context.translate("arguments.station.not_found", arrayOf(text)))
        }
        parsed = station

        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        val safeInput = focusedOption.safeInput

        if (safeInput.isEmpty()) {
            suggest<String>(emptyList())
        } else {
            val response = marudor.stopPlace.search(safeInput, 25)

            suggestString {
                response.forEach {
                    choice(it.name, "eva:${it.eva}")
                }
            }
        }
    }
}
