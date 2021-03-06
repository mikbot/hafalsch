package dev.schlaubi.hafalsch.bot.commands.traewelling

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.bot.command.traewelling.*
import dev.schlaubi.hafalsch.bot.config.Config
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.core.saveState
import dev.schlaubi.hafalsch.bot.database.CheckIn
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin
import dev.schlaubi.hafalsch.bot.database.findForJourney
import dev.schlaubi.hafalsch.bot.ui.asUIContext
import dev.schlaubi.hafalsch.bot.ui.format
import dev.schlaubi.hafalsch.marudor.entity.IrisMessage
import dev.schlaubi.hafalsch.traewelling.entity.CheckInRequest
import dev.schlaubi.hafalsch.traewelling.entity.User
import dev.schlaubi.mikbot.plugin.api.util.kord
import org.litote.kmongo.`in`

interface CheckInArguments {
    val station: Int
    val trip: JourneyInfo
    val exit: String
    val travelType: TravelType?
    val body: String?
    val tweet: Boolean
    val toot: Boolean
}

open class SimpleCheckInArguments : Arguments(), CheckInArguments {
    override val station by traewellingStation {
        name = TraewellingStationConverter.name
        description = "commands.traewelling.arguments.station.description"
    }

    override val trip by trip {
        name = TripConverter.name
        description = "commands.traewelling.arguments.trip.description"
    }

    override val exit by exit {
        name = "exit"
        description = "commands.traewelling.arguments.exit.description"
    }

    override val body: String? by optionalString {
        name = "body"
        description = "commands.traewelling.arguments.body.description"
    }

    override val tweet: Boolean by defaultingBoolean {
        name = "tweet"
        description = "commands.traewelling.arguments.tweet.description"
        defaultValue = false
    }

    override val toot: Boolean by defaultingBoolean {
        name = "toot"
        description = "commands.traewelling.arguments.toot.description"
        defaultValue = false
    }

    override val travelType: TravelType?
        get() = null
}

class ExtendedCheckInArguments : SimpleCheckInArguments() {
    override val travelType: TravelType? by enumChoice {
        name = TravelType.argumentName
        description = "commands.traewelling.arguments.travel_type.description"
        typeName = "TravelType"
    }

    init {
        // Move travel type to start
        args.add(0, args.last())
        args.removeAt(args.lastIndex)
    }
}

context(HafalschModule)
        suspend fun PublicSlashCommand<*>.simpleCheckInCommand() = checkInCommand("check-in", ::SimpleCheckInArguments)

context(HafalschModule)
        suspend fun PublicSlashCommand<*>.extendedCheckInCommand() =
    checkInCommand("extended-check-in", ::ExtendedCheckInArguments)

context(HafalschModule)
        @OptIn(KordUnsafe::class, KordExperimental::class)
        private suspend fun <A> PublicSlashCommand<*>.checkInCommand(name: String, argumentsBuilder: () -> A)
        where A : Arguments, A : CheckInArguments = ephemeralSubCommand(argumentsBuilder) {
    this.name = name
    description = "commands.check_in.description"

    action {
        val request = CheckInRequest(
            arguments.trip.jid,
            arguments.trip.lineName,
            arguments.trip.station,
            arguments.exit,
            arguments.body,
            arguments.tweet,
            arguments.toot
        )
        withToken {
            val checkIn = traewelling.trains.checkIn(request, token)

            respond {
                val dbCheckIn = CheckIn(
                    user = user.id,
                    journeyId = arguments.trip.jid,
                    start = arguments.trip.station,
                    end = arguments.exit
                )
                if (Database.checkIns.findForJourney(user.id, arguments.trip.jid) == null) {

                    Database.checkIns.save(dbCheckIn)
                }
                val state = dbCheckIn.saveState(marudor)
                embed {
                    title = translate("commands.traewelling.check-in.success.title")
                    description = translate(
                        "commands.traewelling.check-in.success.description",
                        arrayOf(arguments.trip.lineName, checkIn.distance, checkIn.points)
                    )

                    field {
                        this.name = translate("commands.traewelling.check-in.delay.title")

                        val currentDelay = state.delays[arguments.trip.station]
                        val delayAtExit = state.delays[arguments.exit]

                        value = translate("commands.traewelling.check-in.delay.description", arrayOf(currentDelay, delayAtExit))
                    }

                    val relevantMessages = state.messages
                        .filter { it.type == IrisMessage.Type.SERVICE_MESSAGE }
                    if (relevantMessages.isNotEmpty()) {
                        asUIContext {
                            field {
                                this.name = translate("commands.traewelling.check-in.messages.title")
                                value = relevantMessages.format()
                            }
                        }
                    }

                    if (checkIn.alsoOnThisConnection.isNotEmpty()) {
                        field {
                            this.name = translate("commands.traewelling.check-in.also_on_this_connection.title")
                            val foundAccounts = Database.traewellingLogins.find(
                                TraevellingUserLogin::userId `in` checkIn.alsoOnThisConnection.map(User::id)
                            ).toList().associateBy(TraevellingUserLogin::userId)

                            value = checkIn.alsoOnThisConnection
                                .joinToString("\n") {
                                    val foundAccount = foundAccounts[it.id]
                                    val mention = if (foundAccount != null) {
                                        kord.unsafe.user(foundAccount.id).mention
                                    } else {
                                        "[@${it.username}](${Config.TR??WELLING_API}/@${it.username})"
                                    }

                                    "- $mention (${it.points} pts - ${it.trainDistance / 1000} km)"
                                }
                        }
                    }
                }
            }
        }
    }
}
