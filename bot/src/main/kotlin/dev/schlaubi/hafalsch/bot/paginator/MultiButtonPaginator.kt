package dev.schlaubi.hafalsch.bot.paginator

import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import java.util.*

typealias ComponentDescriptorBuilder = suspend ComponentContainer.(paginator: MultiButtonPaginator) -> InteractionButtonWithAction<*>

data class ComponentDescriptor(val builder: ComponentDescriptorBuilder, val pages: List<Int>? = null)
private data class PageComponent(val component: InteractionButtonWithAction<*>, val pages: List<Int>? = null)

class MultiButtonPaginator(
    private var myPages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (myPages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    private val additionalButtons: List<ComponentDescriptor>,


    private val interaction: MessageInteractionResponseBehavior,
    private val doFollowUp: Boolean = false
) : BaseButtonPaginator(myPages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** Whether this paginator has been set up for the first time. **/
    private var isSetup: Boolean = false
    private lateinit var pageComponents: List<PageComponent>
    private var followUp: PublicFollowupMessage? = null

    override suspend fun setup() {
        super.setup()
        pageComponents = additionalButtons.map {
            PageComponent(it.builder.invoke(components, this), it.pages)
        }
    }

    suspend fun updatePages(pages: Pages) {
        this.myPages = pages
        goToPage(currentPageNum)
    }

    private suspend fun goToPage(page: Int, force: Boolean) {
        if (page == currentPageNum && !force) {
            return
        }

        if ((page < 0 || page > pages.groups[currentGroup]!!.size - 1) && !force) {
            return
        }

        currentPageNum = page
        currentPage = myPages.get(currentGroup, currentPageNum)

        send()
    }

    override suspend fun goToPage(page: Int) = goToPage(page, false)

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

        val edit: suspend MessageModifyBuilder.() -> Unit = {
            embed { applyPage() }

            with(this@MultiButtonPaginator.components) {
                applyToMessage()
            }
        }

        if (doFollowUp) {
            if (followUp == null) {
                followUp = interaction.createPublicFollowup {
                    embed {
                        title = "Loading ..."
                    }
                }
            }
            @Suppress("ReplaceNotNullAssertionWithElvisReturn")
            followUp!!.edit { edit() }
        } else {
            interaction.edit {
                edit()
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
            val edit: suspend MessageModifyBuilder.() -> Unit = {
                embed { applyPage() }

                this.components = mutableListOf()
            }

            if (followUp != null) {
                @Suppress("ReplaceNotNullAssertionWithElvisReturn")
                followUp!!.edit { edit() }
            } else {
                interaction.edit { edit() }
            }
        }

        super.destroy()
    }
}
