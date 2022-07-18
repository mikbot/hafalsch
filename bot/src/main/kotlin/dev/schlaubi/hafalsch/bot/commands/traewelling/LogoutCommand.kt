package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.database.Database
import kotlinx.datetime.Clock


context(HafalschModule)
        suspend fun PublicSlashCommand<*>.logoutCommand() = ephemeralSubCommand {
    name = "logout"
    description = "commands.traewelling.logout.description"

    action {
        val token = Database.traewellingLogins.findOneById(user.id)

        if (token != null) {
            Database.traewellingLogins.deleteOneById(user.id)
            if (token.expiresAt > Clock.System.now()) {
                traewelling.auth.logout(token.token)
            }

            respond {
                content = translate("commands.traewelling.logout.success")
            }
        } else {
            respond {
                content = translate("traewelling.not_logged_in")
            }
        }
    }
}
