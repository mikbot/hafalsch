package dev.schlaubi.hafalsch.bot.ui

import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.marudor.entity.Coach

fun Coach.Features.buildEmojiString() = buildString {
    appendIf(dining, Emojis.cannedFood)
    appendIf(wheelchair, Emojis.wheelchair)
    appendIf(bike, Emojis.bike)
    appendIf(disabled, Emojis.manInManualWheelchair)
    appendIf(quiet, Emojis.hushed)
    appendIf(info, Emojis.informationDeskPerson)
    appendIf(family, Emojis.family)
    appendIf(toddler, Emojis.baby)
    appendIf(wifi, Emojis.signalStrength)
    appendIf(comfort, Emojis.bed)
}

private fun Appendable.appendIf(condition: Boolean, value: Any?) {
    if (condition) {
        append(value.toString())
    }
}
