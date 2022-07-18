package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.command.traewelling.status
import dev.schlaubi.hafalsch.bot.command.traewelling.withToken
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.database.CheckIn
import dev.schlaubi.hafalsch.bot.database.Database
import org.litote.kmongo.and
import org.litote.kmongo.eq

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
            Database.checkIns.deleteOne(
                and(
                    CheckIn::journeyId eq arguments.status.trainCheckin.tripId,
                    CheckIn::user eq user.id
                )
            )

            respond {
                content = translate("commands.traewelling.check_out.success", arrayOf(arguments.status.id))
            }
        }
    }
}