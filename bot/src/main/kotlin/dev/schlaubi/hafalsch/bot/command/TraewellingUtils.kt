package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin
import dev.schlaubi.mikbot.plugin.api.util.discordError

@PublishedApi
internal const val errorMessage = "Please login to Träwelling for auto-complete"

suspend inline fun AutoCompleteInteraction.withToken(block: TraevellingUserLogin.() -> Unit) {
    val token = Database.traewellingLogins.findOneById(user.id)
    if (token == null) {
        suggestString {
            choice(errorMessage, "0")
        }

        return
    }

    block(token)
}

suspend inline fun <T> CommandContext.withToken(block: TraevellingUserLogin.() -> T): T {
    val user = getUser() ?: discordError(errorMessage)
    val token = Database.traewellingLogins.findOneById(user.id) ?: discordError(errorMessage)

    return block(token)
}

