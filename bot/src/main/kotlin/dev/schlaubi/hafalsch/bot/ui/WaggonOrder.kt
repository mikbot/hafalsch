package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButtonContext
import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence

@Suppress("ConvertLambdaToReference")
suspend fun EphemeralInteractionButtonContext.sendWaggonOrder(coachSequence: CoachSequence) {
    editingPaginator {
        coachSequence.sequence.groups.forEach { group ->
            group.coaches.forEach { (_, category, closed, uic, type, identificationNumber, _, features, seats) ->
                page {
                    val rawTitle = translate("coach_sequence.title", arrayOf(identificationNumber))
                    title = if (closed) {
                        translate("journey.wannabe", arrayOf(rawTitle))
                    } else {
                        rawTitle
                    }

                    description = features.buildEmojiString()

                    if (group.trainName != null) {
                        field {
                            name = translate("coach.train_name")
                            value = group.trainName.toString()
                        }
                    }

                    field {
                        name = translate("coach_sequence.model")
                        value = group.model.name
                    }

                    field {
                        name = translate("coach_sequence.jibberish")
                        val explainer = "https://lib.finalrewind.org/dbdb/db_wagen/${uic?.substring(4, 9)}.png"
                        val typeText = if ("WAGEN" in category) {
                            "[$type]($explainer)"
                        } else {
                            type
                        }
                        value = "$category aka $typeText"
                    }

                    if (seats?.comfort != null) {
                        field {
                            name = translate("coach_sequence.seats.comfort")
                            value = seats.comfort.toString()
                        }
                    }

                    if (seats?.family != null) {
                        field {
                            name = translate("coach_sequence.seats.family")
                            value = seats.family.toString()
                        }
                    }

                    if (seats?.disabled != null) {
                        field {
                            name = translate("coach_sequence.seats.disabled")
                            value = seats.disabled.toString()
                        }
                    }
                }
            }
        }
    }.send()
}
