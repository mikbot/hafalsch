package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

abstract class AutoCompletingArgument<T : Any>(validator: Validator<T> = null) :
    SingleConverter<T>(validator) {

    override fun withBuilder(builder: ConverterBuilder<T>): SingleConverter<T> {
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

    protected abstract suspend fun parseText(text: String, context: CommandContext): Boolean

    protected abstract suspend fun AutoCompleteInteraction.onAutoComplete()

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true; autocomplete = true }
}
