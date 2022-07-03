package dev.schlaubi.hafalsch.bot.paginator

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.types.emoji
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.x.emoji.Emojis
import java.util.*
import kotlin.experimental.ExperimentalTypeInference

typealias SuspendingDataFetcher<T> = suspend () -> T

typealias DataDependentPageBuilder<T> = MultiButtonPaginatorBuilder.(data: T) -> Unit


class RefreshablePaginatorBuilder<T> {

    @PublishedApi
    internal lateinit var retriever: SuspendingDataFetcher<T>

    @PublishedApi
    internal lateinit var pageBuilder: DataDependentPageBuilder<T>

    @PublishedApi
    internal var paginatorConfigurator: (MultiButtonPaginator.() -> Unit)? = null

    fun retriever(fetcher: SuspendingDataFetcher<T>) {
        retriever = fetcher
    }

    fun pageBuilder(builder: DataDependentPageBuilder<T>) {
        pageBuilder = builder
    }

    fun paginatorConfigurator(builder: MultiButtonPaginator.() -> Unit) {
        paginatorConfigurator = builder
    }
}

private typealias Resend = suspend (initial: Boolean) -> MultiButtonPaginator

@OptIn(ExperimentalTypeInference::class)
suspend inline fun <T> PublicSlashCommandContext<*>.refreshableMultiButtonPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    initialData: T? = null,
    @BuilderInference crossinline block: RefreshablePaginatorBuilder<T>.() -> Unit
) = refreshableMultiButtonPaginator(
    interactionResponse, command.resolvedBundle, defaultGroup, locale, initialData, block
)

@OptIn(ExperimentalTypeInference::class)
suspend inline fun <T> PublicInteractionButtonContext.refreshableMultiButtonPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    initialData: T? = null,
    @BuilderInference crossinline block: RefreshablePaginatorBuilder<T>.() -> Unit
) = refreshableMultiButtonPaginator(
    interactionResponse, component.bundle, defaultGroup, locale, initialData, block
)

@OptIn(ExperimentalTypeInference::class)
suspend inline fun <T> refreshableMultiButtonPaginator(
    interactionResponse: MessageInteractionResponseBehavior,
    bundle: String? = null,
    defaultGroup: String = "",
    locale: Locale? = null,
    initialData: T? = null,
    @BuilderInference crossinline block: RefreshablePaginatorBuilder<T>.() -> Unit
) {
    val builder = RefreshablePaginatorBuilder<T>().apply(block)
    lateinit var context: PaginatorContainer

    val send: Resend = { initial ->
        val data = if (initial && initialData != null) initialData else builder.retriever()
        multiButtonPaginator(interactionResponse, bundle, defaultGroup, locale) {
            builder.pageBuilder(this, data)
            publicButton(2) {
                emoji(Emojis.repeat.unicode)

                action {
                    context.paginator.destroy(false)
                    val newPaginator = context.resend(false)
                    context = context.copy(newPaginator)
                    newPaginator.send()
                }
            }
        }.apply {
            builder.paginatorConfigurator?.invoke(this)
        }
    }

    context = PaginatorContainer(send(true), send)
    context.paginator.send()
}

@PublishedApi
internal data class PaginatorContainer(
    val paginator: MultiButtonPaginator,
    val resend: Resend
)
