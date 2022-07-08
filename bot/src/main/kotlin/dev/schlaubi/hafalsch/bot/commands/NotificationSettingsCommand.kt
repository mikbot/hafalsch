package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.findOneByIdSafe

class NotificationSettingsArguments : Arguments() {
    val currentDelayMargin by optionalInt {
        name = "current-delay-margin"
        description = "commands.notification_settings.arguments.current_delay_margin.description"
    }
    val exitDelayMargin by optionalInt {
        name = "exit-delay-margin"
        description = "commands.notification_settings.arguments.exit_delay_margin.description"
    }
    val subscribeToMessages by optionalBoolean {
        name = "subscribe-to-messages"
        description = "commands.notification_settings.arguments.subscribe_to_messages.description"
    }
}

suspend fun Extension.notificationSettingsCommand() = ephemeralSlashCommand(::NotificationSettingsArguments) {
    name = "notification-settings"
    description = "commands.notification_settings.description"

    action {
        val current = Database.subscriptionSettings.findOneByIdSafe(user.id)

        val mergedSettings = current.copy(
            currentDelayMargin = arguments.currentDelayMargin ?: current.currentDelayMargin,
            exitDelayMargin = arguments.exitDelayMargin ?: current.exitDelayMargin,
            subscribeToMessages = arguments.subscribeToMessages ?: current.subscribeToMessages,
            locale = event.interaction.locale?.asJavaLocale()?.toLanguageTag()
        )

        Database.subscriptionSettings.save(mergedSettings)

        respond {
            content = translate("commands.notification_settings.success", arrayOf(mergedSettings))
        }
    }
}
