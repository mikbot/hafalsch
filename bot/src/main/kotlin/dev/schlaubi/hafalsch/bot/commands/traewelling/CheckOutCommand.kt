package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.command.traewelling.status
import dev.schlaubi.hafalsch.bot.command.traewelling.withToken
import dev.schlaubi.hafalsch.bot.core.HafalschModule

class CheckOutArguments : Arguments() {
    val status by status {
        name = "status"
        description = "commands.traewelling.check_out.arguments.status.description"
    }
}

context(HafalschModule)
        suspend fun PublicSlashCommand<*>.checkOutCommand() = ephemeralSubCommand(::CheckOutArguments) {
    name = "check-out"
    description = "commands.traewelling.check_out.description"

    action {
        withToken {
            traewelling.statuses.delete(arguments.status.id, token)

            respond {
                content = translate("commands.traewelling.check_out.success", arrayOf(arguments.status.id))
            }
        }
    }
}