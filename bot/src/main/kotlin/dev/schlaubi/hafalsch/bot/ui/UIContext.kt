package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
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
internal abstract class SlashCommandUIContext : UIContext {
    abstract val context: ApplicationCommandContext
    override val bundle: String?
        get() = context.command.resolvedBundle
    override val kord: Kord
        get() = context.kord

    @Suppress("UNCHECKED_CAST")
    override suspend fun translate(key: String, vararg arguments: Any?): String = context.translate(
        key, bundle, arguments as Array<Any?>
    )
}

@PublishedApi
internal class PublicSlashCommandUIContext(override val context: PublicSlashCommandContext<*>) :
    SlashCommandUIContext() {
    override val response: MessageInteractionResponseBehavior
        get() = context.interactionResponse
}

@PublishedApi
internal class EphemeralSlashCommandUIContext(override val context: EphemeralSlashCommandContext<*>) :
    SlashCommandUIContext() {
    override val response: MessageInteractionResponseBehavior
        get() = context.interactionResponse
}

inline fun PublicSlashCommandContext<*>.asUIContext(block: UIContext.() -> Unit) {
    PublicSlashCommandUIContext(this).apply(block)
}

inline fun EphemeralSlashCommandContext<*>.asUIContext(block: UIContext.() -> Unit) {
    EphemeralSlashCommandUIContext(this).apply(block)
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
