package dev.schlaubi.hafalsch.bot.command.traewelling

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.schlaubi.hafalsch.bot.command.AutoCompletingArgument
import dev.schlaubi.hafalsch.bot.command.autoCompleteInjection
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.hafalsch.traewelling.entity.Status
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.datetime.toJavaInstant
import org.koin.core.component.inject
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val format = DateTimeFormatter.ofPattern("HH:mm")

@Converter(
    "status",

    types = [ConverterType.SINGLE],
    builderBuildFunctionStatements = [autoCompleteInjection]
)
class StatusConverter(validator: Validator<Status> = null) : AutoCompletingArgument<Status>(validator) {
    private val traewelling by inject<Traewelling>()
    override val signatureTypeString: String = "Status"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        val id = text.toIntOrNull() ?: discordError(
            context.translate(
                "arguments.traewelling.status.invalid_id",
                arrayOf(text)
            )
        )
        context.withToken {
            parsed = traewelling.statuses.fetch(id, token)
                ?: discordError(context.translate("arguments.traewelling.status.not_found", arrayOf(text)))
        }

        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        withToken {
            val user = traewelling.getUser(token)
            val statuses = traewelling.user.listEnroute(user.username, token)

            suggestString {
                statuses.take(25).forEach {
                    val departure = format.format(it.trainCheckin.departure.toJavaInstant().atOffset(ZoneOffset.UTC))
                    choice(
                        "${it.trainCheckin.origin.name} -> ${it.trainCheckin.destination.name} ($departure)",
                        it.id.toString()
                    )
                }
            }
        }
    }
}
