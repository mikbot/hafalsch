package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.actionRow
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.core.registerUser
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import io.ktor.http.*

context(HafalschModule)
        suspend fun PublicSlashCommand<*>.loginCommand() {
    ephemeralSubCommand {
        name = "login"
        description = "commands.traewelling.login.description"

        action {
            respond {
                content = translate("commands.traewelling.login.explainer")
                val id = registerUser(user.id)
                actionRow {
                    val url = buildBotUrl {
                        path("hafalsch", "oauth", "login")
                        parameters["id"] = id
                    }.toString()
                    linkButton(url) {
                        label = translate("commands.traewelling.login.login")
                    }
                }
            }
        }
    }
}
