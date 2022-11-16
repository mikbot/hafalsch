package dev.schlaubi.hafalsch.bot.paginator

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.types.emoji
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.bot.ui.UIContext
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
suspend inline fun <T> UIContext.refreshableMultiButtonPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    initialData: T? = null,
    @BuilderInference crossinline block: RefreshablePaginatorBuilder<T>.() -> Unit
) = refreshableMultiButtonPaginator(
    response, bundle, defaultGroup, locale, initialData, block
)

@OptIn(ExperimentalTypeInference::class)
suspend inline fun <T> refreshableMultiButtonPaginator(
    interactionResponse: MessageInteractionResponseBehavior,
    bundle: String? = null,
    defaultGroup: String = "",
    locale: Locale? = null,
    initialData: T? = null,
    @BuilderInference crossinline block: RefreshablePaginatorBuilder<T>.() -> Unit
): MultiButtonPaginator {
    val builder = RefreshablePaginatorBuilder<T>().apply(block)
    lateinit var context: PaginatorContainer

    val paginator = multiButtonPaginator(interactionResponse, bundle, defaultGroup, locale) {
        builder.pageBuilder(this, initialData ?: builder.retriever())
        publicButton(2) {
            emoji(Emojis.repeat.unicode)

            action {
                val newBuilder = MultiButtonPaginatorBuilder(bundle, parent).apply {
                    builder.pageBuilder(this, builder.retriever())
                }
                context.paginator.updatePages(newBuilder.parent.pages)
            }
        }
    }

    context = PaginatorContainer(paginator)
    context.paginator.send()

    return paginator
}

@PublishedApi
internal data class PaginatorContainer(val paginator: MultiButtonPaginator)
