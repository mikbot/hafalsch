package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.Station
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import org.koin.core.component.inject

@Converter(
    "station",

    types = [ConverterType.SINGLE, ConverterType.OPTIONAL]
)
class StationConverter(validator: Validator<Station> = null) : SingleConverter<Station>(validator) {
    val marudor by inject<Marudor>()
    override val signatureTypeString: String = "Station"

    override fun withBuilder(builder: ConverterBuilder<Station>): SingleConverter<Station> {
        val builderWithAutoComplete = builder.apply { autoComplete { onAutoComplete() } }
        return super.withBuilder(builderWithAutoComplete)
    }

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val text = parser?.parseNext()?.data ?: return false

        return parseText(text, context)
    }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false

        return parseText(optionValue, context)
    }

    private suspend fun parseText(text: String, context: CommandContext): Boolean {
        val station = if (text.startsWith("eva:")) {
            val eva = text.substringAfter("eva:")
            marudor.stopPlace.byEva(eva)
        } else {
            marudor.stopPlace.search(text, 1).firstOrNull()
        }

        if (station == null) {
            discordError(context.translate("arguments.station.not_found", arrayOf(text)))
        } else {
            parsed = station
        }

        return true
    }

    private suspend fun AutoCompleteInteraction.onAutoComplete() {
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

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true; autocomplete = true }
}
