package dev.schlaubi.hafalsch.bot.paginator

import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.modify.embed
import java.util.*

typealias ComponentDescriptorBuilder = suspend ComponentContainer.(paginator: MultiButtonPaginator) -> InteractionButtonWithAction<*>

data class ComponentDescriptor(val builder: ComponentDescriptorBuilder, val pages: List<Int>? = null)
private data class PageComponent(val component: InteractionButtonWithAction<*>, val pages: List<Int>? = null)

class MultiButtonPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    private val additionalButtons: List<ComponentDescriptor>,


    private val interaction: MessageInteractionResponseBehavior,
) : BaseButtonPaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** Whether this paginator has been set up for the first time. **/
    var isSetup: Boolean = false
    private lateinit var pageComponents: List<PageComponent>

    override suspend fun setup() {
        super.setup()
        pageComponents = additionalButtons.map {
            PageComponent(it.builder.invoke(components, this), it.pages)
        }
    }


    override suspend fun send() {
        if (!isSetup) {
            isSetup = true

            setup()
        } else {
            updateButtons()
            pageComponents.forEach {
                if (it.pages != null && currentPageNum !in it.pages) {
                    it.component.disable()
                } else if (it.pages != null && it.component.disabled && currentPageNum in it.pages) {
                    it.component.enable()
                }
            }
        }

        interaction.edit {
            embed { applyPage() }

            with(this@MultiButtonPaginator.components) {
                this@edit.applyToMessage()
            }
        }
    }

    override suspend fun destroy() = destroy(true)
    suspend fun destroy(removeButtons: Boolean) {
        if (!active) {
            return
        }

        active = false

        if (removeButtons) {
            interaction.edit {
                embed { applyPage() }

                this.components = mutableListOf()
            }
        }

        super.destroy()
    }
}
