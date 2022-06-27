package dev.schlaubi.hafalsch.bot.paginator

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import java.util.*

data class MultiButtonPaginatorBuilder(
    val bundle: String?,
    val parent: PaginatorBuilder,
    var buttons: MutableList<ComponentDescriptor> = mutableListOf()
) {
    fun publicButton(
        row: Int? = null,
        pages: List<Int>? = null,
        builder: suspend PublicInteractionButton.(paginator: MultiButtonPaginator) -> Unit
    ) {
        val configure: suspend ComponentContainer.(paginator: MultiButtonPaginator) -> InteractionButtonWithAction<*> =
            {
                publicButton(row) {
                    bundle = this@MultiButtonPaginatorBuilder.bundle
                    builder.invoke(this, it)
                }
            }

        buttons.add(ComponentDescriptor(configure, pages))
    }

    fun ephemeralButton(
        row: Int? = null,
        pages: List<Int>? = null,
        builder: suspend EphemeralInteractionButton.(paginator: MultiButtonPaginator) -> Unit
    ) {
        val configure: suspend ComponentContainer.(paginator: MultiButtonPaginator) -> InteractionButtonWithAction<*> =
            {
                ephemeralButton(row) {
                    bundle = this@MultiButtonPaginatorBuilder.bundle
                    builder.invoke(this, it)
                }
            }

        buttons.add(ComponentDescriptor(configure, pages))
    }
}

inline fun PublicSlashCommandContext<*>.multiButtonPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    block: MultiButtonPaginatorBuilder.() -> Unit
): MultiButtonPaginator {
    println(command.resolvedBundle)
    val builder = MultiButtonPaginatorBuilder(
        command.resolvedBundle,
        PaginatorBuilder(
            locale = locale,
            defaultGroup = defaultGroup
        )
    )
    block(builder)
    val (_, pages, components) = builder


    return MultiButtonPaginator(pages.pages, additionalButtons = components, interaction = interactionResponse)
}
