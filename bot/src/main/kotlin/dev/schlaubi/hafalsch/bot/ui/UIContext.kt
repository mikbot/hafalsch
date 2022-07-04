package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.schlaubi.hafalsch.marudor.Marudor

interface UIContext : KordExKoinComponent {
    val marudor: Marudor
        get() = getKoin().get()
    val bundle: String?
    val response: MessageInteractionResponseBehavior

    suspend fun translate(key: String, vararg arguments: Any?): String
}

@PublishedApi
internal class SlashCommandUIContext(private val slashCommandContext: PublicSlashCommandContext<*>) : UIContext {
    override val bundle: String?
        get() = slashCommandContext.command.resolvedBundle
    override val response: MessageInteractionResponseBehavior
        get() = slashCommandContext.interactionResponse

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

    @Suppress("UNCHECKED_CAST")
    override suspend fun translate(key: String, vararg arguments: Any?): String = buttonContext.translate(
        key, bundle, arguments as Array<Any?>
    )
}

inline fun PublicInteractionButtonContext.asUIContext(block: UIContext.() -> Unit) {
    ButtonUIContext(this).apply(block)
}
