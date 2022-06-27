package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.text.DateFormat
import java.text.ParseException
import java.util.*

@Converter(
    "date",

    types = [ConverterType.SINGLE, ConverterType.OPTIONAL]
)
class DateConverter(validator: Validator<Instant> = null) : SingleConverter<Instant>(validator) {
    override val signatureTypeString: String = "Date"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val text = parser?.parseNext()?.data ?: return false

        return parseText(text, context)
    }


    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false

        return parseText(optionValue, context)
    }

    private suspend fun parseText(text: String, context: CommandContext): Boolean {
        val event = context.eventObj as InteractionCreateEvent
        val locale = event.interaction.locale?.asJavaLocale()
            ?: event.interaction.guildLocale?.asJavaLocale()
            ?: Locale.getDefault()
        val format = DateFormat.getDateInstance(DateFormat.SHORT, locale)

        try {
            parsed = format.parse(text).toInstant().toKotlinInstant()
        } catch (e: ParseException) {
            discordError(context.translate("arguments.date.invalid", arrayOf(text)))
        }

        return true
    }
}
