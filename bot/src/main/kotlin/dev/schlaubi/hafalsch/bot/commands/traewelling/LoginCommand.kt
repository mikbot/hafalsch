package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.TextInputStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin

private val loginCommand = "träwelling_login"
private val loginModal = "träwelling_login_modal"
private val loginModalEmail = "träwelling_login_modal_email"
private val loginModalPassword = "träwelling_login_modal_password"

context(HafalschModule)
        suspend fun PublicSlashCommand<*>.loginCommand() {
    ephemeralSubCommand {
        name = "login"
        description = "commands.traewelling.login.description"

        action {
            respond {
                content = translate("commands.traewelling.login.explainer")

                actionRow {
                    interactionButton(ButtonStyle.Primary, loginCommand) {
                        label = translate("commands.traewelling.login.login")
                    }
                }
            }
        }
    }

    event<ButtonInteractionCreateEvent> {
        action {
            if (event.interaction.componentId == loginCommand) {
                event.interaction.modal(translate("commands.traewelling.login.title"), loginModal) {
                    actionRow {
                        textInput(
                            TextInputStyle.Short,
                            loginModalEmail,
                            translate("commands.traewelling.login.email")
                        )
                    }

                    actionRow {
                        textInput(
                            TextInputStyle.Short,
                            loginModalPassword,
                            translate("commands.traewelling.login.password")
                        )
                    }
                }
            }
        }
    }

    event<ModalSubmitInteractionCreateEvent> {
        action {
            if (event.interaction.modalId == loginModal) {
                val email = event.interaction.textInputs[loginModalEmail]?.value ?: return@action
                val password = event.interaction.textInputs[loginModalPassword]?.value ?: return@action

                val token = traewelling.auth.login(email, password)

                if (token != null) {
                    val user = traewelling.getUser(token.token)

                    Database.traewellingLogins.save(
                        TraevellingUserLogin(
                            event.interaction.user.id,
                            token.token,
                            token.expiresAt,
                            user.id
                        )
                    )
                    event.interaction.respondEphemeral {
                        content =
                            translate("commands.traewelling.login.success", arrayOf(token.expiresAt.toMessageFormat()))
                    }
                } else {
                    event.interaction.respondEphemeral {
                        content = translate("commands.traewelling.login.error")
                    }
                }
            }
        }
    }
}
