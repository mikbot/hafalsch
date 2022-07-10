package dev.schlaubi.hafalsch.bot.ui

import dev.schlaubi.hafalsch.bot.paginator.refreshableMultiButtonPaginator
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.Stop

@Suppress("ConvertLambdaToReference")
suspend fun UIContext.sendWaggonOrder(
    journey: JourneyInformation,
    stop: Stop,
    coachSequence: CoachSequence
) = refreshableMultiButtonPaginator(initialData = coachSequence) {
    retriever {
        with(marudor) { journey.fetchCoachSequence(stop) ?: coachSequence }
    }
    pageBuilder {
        doFollowUp = true

        coachSequence.sequence.groups.forEach { (coaches, groupName, _, destinationName, trainName, _, model) ->
            coaches.forEach { (_, category, closed, uic, type, identificationNumber, _, features, seats) ->
                parent.page {
                    val rawTitle = translate("coach_sequence.title", identificationNumber)
                    title = if (closed) {
                        translate("journey.wannabe", rawTitle)
                    } else {
                        rawTitle
                    }

                    description = features.buildEmojiString()

                    if (trainName != null) {
                        field {
                            name = translate("coach.train_name")
                            value = trainName.toString()
                        }
                    }

                    if (coachSequence.multipleTrainNumbers) {
                        field {
                            name = translate("coach_sequence.train_number")
                            value = groupName
                        }
                    }

                    if (coachSequence.multipleDestinations) {
                        field {
                            name = translate("coach_sequence.destination")
                            value = destinationName
                        }
                    }

                    if (model != null) {
                        field {
                            name = translate("coach_sequence.model")
                            value = model.name
                        }
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
    }
}
