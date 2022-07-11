package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import dev.schlaubi.mikbot.plugin.api.util.kord
import org.koin.core.component.inject

interface UIContext : KordExKoinComponent {
    val kord: Kord
    val marudor: Marudor
        get() = getKoin().get()
    val bundle: String?
    val response: MessageInteractionResponseBehavior

    suspend fun translate(key: String, vararg arguments: Any?): String
}

suspend inline fun UIContext.respond(builder: MessageCreateBuilder.() -> Unit) =
    response.createEphemeralFollowup(builder)

@PublishedApi
internal class SlashCommandUIContext(private val slashCommandContext: PublicSlashCommandContext<*>) : UIContext {
    override val bundle: String?
        get() = slashCommandContext.command.resolvedBundle
    override val response: MessageInteractionResponseBehavior
        get() = slashCommandContext.interactionResponse
    override val kord: Kord
        get() = slashCommandContext.kord

    @Suppress("UNCHECKED_CAST")
    override suspend fun translate(key: String, vararg arguments: Any?): String = slashCommandContext.translate(
        key, bundle, arguments as Array<Any?>
    )
}

inline fun PublicSlashCommandContext<*>.asUIContext(block: UIContext.() -> Unit) {
    SlashCommandUIContext(this).apply(block)
}

@PublishedApi
internal class ButtonUIContext(private val buttonContext: PublicInteractionButtonContext) : UIContext {
    override val bundle: String?
        get() = buttonContext.component.bundle
    override val response: MessageInteractionResponseBehavior
        get() = buttonContext.interactionResponse
    override val kord: Kord
        get() = buttonContext.getKoin().get()

    @Suppress("UNCHECKED_CAST")
    override suspend fun translate(key: String, vararg arguments: Any?): String = buttonContext.translate(
        key, bundle, arguments as Array<Any?>
    )
}

class DataUIContext(private val locale: String?) : UIContext {
    private val bot: ExtensibleBot by inject()
    override val kord: Kord by inject()
    override val bundle: String = dev.schlaubi.hafalsch.bot.util.bundle
    override val response: MessageInteractionResponseBehavior
        get() = throw UnsupportedOperationException("This feature is not supported")

    override suspend fun translate(key: String, vararg arguments: Any?): String {
        @Suppress("UNCHECKED_CAST")
        return pluginSystem.translate(key, bundle, locale, arguments as Array<Any?>)
    }

}

inline fun withUIContext(locale: String?, block: UIContext.() -> Unit) {
    DataUIContext(locale).apply(block)
}

inline fun PublicInteractionButtonContext.asUIContext(block: UIContext.() -> Unit) {
    ButtonUIContext(this).apply(block)
}
